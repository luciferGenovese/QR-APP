package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import android.graphics.Bitmap
import android.graphics.Color
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.BitSet
import java.util.UUID
import java.util.zip.CRC32

private const val CHUNK_SIZE = 450
private const val PREFIX = "QRF1"

data class MonoTransferInfo(
    val id: String,
    val name: String,
    val mimeType: String,
    val size: Long,
    val totalChunks: Int,
    val sha256: String
)

private fun encodeText(value: String): String =
    Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

private fun decodeText(value: String): String =
    String(Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING), Charsets.UTF_8)

private fun ByteArray.hex(): String = joinToString("") { "%02x".format(it.toInt() and 0xff) }

class MonoTransferSender private constructor(val info: MonoTransferInfo, private val file: File) : AutoCloseable {
    companion object {
        fun prepare(context: Context, uri: Uri): MonoTransferSender {
            val resolver = context.contentResolver
            val name = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: "archivo"
            val safeName = name.substringAfterLast('/').substringAfterLast('\\').take(120).ifBlank { "archivo" }
            val mime = resolver.getType(uri) ?: "application/octet-stream"
            val cache = File.createTempFile("qr-send-", ".bin", context.cacheDir)
            try {
                val digest = MessageDigest.getInstance("SHA-256")
                var size = 0L
                val input = resolver.openInputStream(uri) ?: error("No se pudo abrir el archivo")
                input.use { source ->
                    cache.outputStream().buffered().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val count = source.read(buffer)
                            if (count < 0) break
                            if (count == 0) continue
                            size += count
                            require(size <= Int.MAX_VALUE.toLong() * CHUNK_SIZE) { "Archivo demasiado grande" }
                            digest.update(buffer, 0, count)
                            output.write(buffer, 0, count)
                        }
                    }
                }
                val chunks = maxOf(1, ((size + CHUNK_SIZE - 1) / CHUNK_SIZE).toInt())
                return MonoTransferSender(MonoTransferInfo(UUID.randomUUID().toString().replace("-", ""), safeName, mime.take(120), size, chunks, digest.digest().hex()), cache)
            } catch (e: Exception) {
                cache.delete()
                throw e
            }
        }
    }

    fun metadataPacket(): String =
        "$PREFIX:M:${info.id}:${info.size}:${info.totalChunks}:${info.sha256}:${encodeText(info.name)}:${encodeText(info.mimeType)}"

    fun dataPacket(index: Int): String {
        require(index in 0 until info.totalChunks)
        val length = minOf(CHUNK_SIZE.toLong(), info.size - index.toLong() * CHUNK_SIZE).toInt()
        val bytes = ByteArray(length)
        RandomAccessFile(file, "r").use { input ->
            input.seek(index.toLong() * CHUNK_SIZE)
            input.readFully(bytes)
        }
        val crc = CRC32().apply { update(bytes) }.value.toString(16)
        return "$PREFIX:D:${info.id}:$index:$crc:${Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)}"
    }

    override fun close() { file.delete() }
}

class MonoTransferReceiver(private val context: Context) : AutoCloseable {
    var info: MonoTransferInfo? = null
        private set
    var receivedCount: Int = 0
        private set
    private var received = BitSet()
    private var file: File? = null

    fun accept(raw: String): Boolean {
        val parts = raw.split(':')
        if (parts.size < 2 || parts[0] != PREFIX) return false
        try {
            if (parts[1] == "M" && parts.size == 8) {
                val id = parts[2]
                val size = parts[3].toLong()
                val total = parts[4].toInt()
                val hash = parts[5]
                require(id.matches(Regex("[0-9a-f]{32}")))
                require(size >= 0 && size <= Int.MAX_VALUE.toLong() * CHUNK_SIZE)
                require(total == maxOf(1, ((size + CHUNK_SIZE - 1) / CHUNK_SIZE).toInt()))
                require(hash.matches(Regex("[0-9a-f]{64}")))
                val name = decodeText(parts[6]).substringAfterLast('/').substringAfterLast('\\').take(120).ifBlank { "archivo" }
                val mime = decodeText(parts[7]).take(120).ifBlank { "application/octet-stream" }
                val incoming = MonoTransferInfo(id, name, mime, size, total, hash)
                if (info == null) {
                    info = incoming
                    file = File.createTempFile("qr-received-", ".bin", context.cacheDir)
                }
                return info == incoming
            }
            val current = info ?: return false
            if (parts[1] != "D" || parts.size != 6 || parts[2] != current.id) return false
            val index = parts[3].toInt()
            if (index !in 0 until current.totalChunks || received[index]) return false
            val bytes = Base64.decode(parts[5], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val expectedLength = minOf(CHUNK_SIZE.toLong(), current.size - index.toLong() * CHUNK_SIZE).toInt()
            if (bytes.size != expectedLength || CRC32().apply { update(bytes) }.value != parts[4].toLong(16)) return false
            RandomAccessFile(file ?: return false, "rw").use { output ->
                output.seek(index.toLong() * CHUNK_SIZE)
                output.write(bytes)
            }
            received.set(index)
            receivedCount++
            return true
        } catch (_: IllegalArgumentException) {
            return false
        }
    }

    fun finish(): File {
        val current = requireNotNull(info)
        check(receivedCount == current.totalChunks) { "Faltan fragmentos" }
        val result = requireNotNull(file)
        check(result.length() == current.size) { "El tamaño del archivo no coincide" }
        val digest = MessageDigest.getInstance("SHA-256")
        result.inputStream().buffered().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (count > 0) digest.update(buffer, 0, count)
            }
        }
        check(digest.digest().hex() == current.sha256) { "El archivo recibido no superó la verificación" }
        return result
    }

    override fun close() { file?.delete(); file = null }
}

fun renderMonoTransferQr(packet: String): Bitmap {
    val matrix = QRCodeWriter().encode(packet, BarcodeFormat.QR_CODE, 512, 512, mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 4
    ))
    val pixels = IntArray(matrix.width * matrix.height) { index ->
        if (matrix[index % matrix.width, index / matrix.width]) Color.BLACK else Color.WHITE
    }
    return Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, matrix.width, 0, 0, matrix.width, matrix.height)
    }
}
