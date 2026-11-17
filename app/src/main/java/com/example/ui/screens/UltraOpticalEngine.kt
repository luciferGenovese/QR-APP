package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

data class TransferMeta(
    val id: String,
    val name: String,
    val rawSize: Long,
    val compressedSize: Long,
    val mimeType: String,
    val totalChunks: Int,
    val isRgbMode: Boolean = false,
    val checksum: Long = 0L
)

enum class OpticalMode {
    MONOCHROME_1X,
    RGB_CHROMATIC_3X
}

object UltraOpticalEngine {

    val QR_HINTS = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
        EncodeHintType.MARGIN to 1,
        EncodeHintType.CHARACTER_SET to "ISO-8859-1"
    )

    val QR_DECODE_HINTS = mapOf(
        com.google.zxing.DecodeHintType.TRY_HARDER to true,
        com.google.zxing.DecodeHintType.CHARACTER_SET to "ISO-8859-1"
    )

    /**
     * Compresses the payload using Deflate and returns compressed bytes.
     */
    fun compressData(bytes: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.BEST_SPEED, true)
        val baos = ByteArrayOutputStream()
        val deflaterStream = DeflaterOutputStream(baos, deflater)
        deflaterStream.write(bytes)
        deflaterStream.finish()
        deflaterStream.close()
        return baos.toByteArray()
    }

    /**
     * Decompresses Deflate-compressed bytes.
     */
    fun decompressData(compressed: ByteArray): ByteArray {
        val rawBaos = ByteArrayOutputStream()
        try {
            val inflater = java.util.zip.Inflater(true)
            val inflaterStream = InflaterInputStream(ByteArrayInputStream(compressed), inflater)
            val buffer = ByteArray(4096)
            var len: Int
            while (inflaterStream.read(buffer).also { len = it } > 0) {
                rawBaos.write(buffer, 0, len)
            }
            inflaterStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return if (rawBaos.size() > 0) rawBaos.toByteArray() else compressed
    }

    /**
     * Generates Monochromatic (1X) Turbo QR Bitmaps for the entire file.
     */
    fun generateTurboFrames(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        chunkSize: Int,
        onProgress: (Float) -> Unit = {}
    ): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val compressed = compressData(fileBytes)
        val totalChunks = (compressed.size + chunkSize - 1) / chunkSize
        val fileId = System.currentTimeMillis().toString().takeLast(6)
        val checksum = fileBytes.size.toLong()

        val writer = QRCodeWriter()

        // Metadata frame
        val metaHeader = "OB2:M:$fileId:$fileName:${fileBytes.size}:${compressed.size}:$mimeType:$totalChunks:$checksum"
        bitmaps.add(renderQrBitmap(writer, metaHeader, 512, 512))

        // Data frames
        for (i in 0 until totalChunks) {
            val start = i * chunkSize
            val end = (start + chunkSize).coerceAtMost(compressed.size)
            val slice = compressed.copyOfRange(start, end)
            val chunkBase64 = Base64.encodeToString(slice, Base64.NO_WRAP)

            val packet = "OB2:D:$fileId:$i:$totalChunks:$chunkBase64"
            bitmaps.add(renderQrBitmap(writer, packet, 512, 512))

            onProgress((i + 1f) / totalChunks.toFloat())
        }

        return bitmaps
    }

    /**
     * Generates Chromatic RGB 3X Turbo Bitmaps multiplexing 3 channels (R, G, B) simultaneously.
     */
    fun generateRgbTurboFrames(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        chunkSize: Int,
        onProgress: (Float) -> Unit = {}
    ): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val compressed = compressData(fileBytes)
        val totalChunks = (compressed.size + chunkSize - 1) / chunkSize
        val fileId = System.currentTimeMillis().toString().takeLast(6)
        val checksum = fileBytes.size.toLong()

        val writer = QRCodeWriter()

        // Metadata Beacon Frame (High contrast)
        val metaHeader = "OB3:M:$fileId:$fileName:${fileBytes.size}:${compressed.size}:$mimeType:$totalChunks:$checksum"
        bitmaps.add(renderQrBitmap(writer, metaHeader, 512, 512))

        // Group chunks in triplets (R, G, B channels)
        var chunkIdx = 0
        while (chunkIdx < totalChunks) {
            // Red Channel
            val rIdx = chunkIdx
            val rStart = rIdx * chunkSize
            val rEnd = (rStart + chunkSize).coerceAtMost(compressed.size)
            val rSlice = compressed.copyOfRange(rStart, rEnd)
            val rPacket = "OB3:D:$fileId:$rIdx:$totalChunks:${Base64.encodeToString(rSlice, Base64.NO_WRAP)}"
            val matrixR = writer.encode(rPacket, BarcodeFormat.QR_CODE, 512, 512, QR_HINTS)

            // Green Channel
            var matrixG: BitMatrix? = null
            if (chunkIdx + 1 < totalChunks) {
                val gIdx = chunkIdx + 1
                val gStart = gIdx * chunkSize
                val gEnd = (gStart + chunkSize).coerceAtMost(compressed.size)
                val gSlice = compressed.copyOfRange(gStart, gEnd)
                val gPacket = "OB3:D:$fileId:$gIdx:$totalChunks:${Base64.encodeToString(gSlice, Base64.NO_WRAP)}"
                matrixG = writer.encode(gPacket, BarcodeFormat.QR_CODE, 512, 512, QR_HINTS)
            }

            // Blue Channel
            var matrixB: BitMatrix? = null
            if (chunkIdx + 2 < totalChunks) {
                val bIdx = chunkIdx + 2
                val bStart = bIdx * chunkSize
                val bEnd = (bStart + chunkSize).coerceAtMost(compressed.size)
                val bSlice = compressed.copyOfRange(bStart, bEnd)
                val bPacket = "OB3:D:$fileId:$bIdx:$totalChunks:${Base64.encodeToString(bSlice, Base64.NO_WRAP)}"
                matrixB = writer.encode(bPacket, BarcodeFormat.QR_CODE, 512, 512, QR_HINTS)
            }

            val matrixGVal = if (matrixG != null) matrixG else matrixR
            val matrixBVal = if (matrixB != null) matrixB else matrixR

            // Synthesize RGB Composite
            val rgbBmp = renderRgbCompositeBitmap(matrixR, matrixG, matrixB, 512, 512)
            bitmaps.add(rgbBmp)

            chunkIdx += 3
            onProgress((chunkIdx.toFloat() / totalChunks.toFloat()).coerceAtMost(1f))
        }

        return bitmaps
    }

    /**
     * Renders a single monochrome QR Bitmap using fast pixel array buffers.
     */
    fun renderQrBitmap(
        writer: QRCodeWriter,
        content: String,
        width: Int,
        height: Int
    ): Bitmap {
        val matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, QR_HINTS)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.RGB_565)
    }

    /**
     * Combines three distinct BitMatrices (R, G, B) into a single 24-bit RGB bitmap.
     * Generates saturated 8-color modules (Red, Green, Blue, Cyan, Magenta, Yellow, Black, White).
     */
    fun renderRgbCompositeBitmap(
        matrixR: BitMatrix,
        matrixG: BitMatrix?,
        matrixB: BitMatrix?,
        width: Int,
        height: Int
    ): Bitmap {
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                // In QR codes, true = dark module (active), false = light module (inactive)
                val rDark = matrixR[x, y]
                val gDark = matrixG?.get(x, y) ?: false
                val bDark = matrixB?.get(x, y) ?: false

                // Active bit in each channel creates color modulation
                val rVal = if (rDark) 0 else 255
                val gVal = if (gDark) 0 else 255
                val bVal = if (bDark) 0 else 255

                pixels[offset + x] = (0xFF shl 24) or (rVal shl 16) or (gVal shl 8) or bVal
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    /**
     * Slices an RGB frame into 3 separate monochrome channels (Red, Green, Blue) and decodes QR codes from all 3.
     */
    fun decodeRgbPlanesFromBitmap(bitmap: Bitmap): List<String> {
        val decodedList = mutableListOf<String>()
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val reader = QRCodeReader()

        // 1. Red Plane Slicing
        try {
            val rPixels = IntArray(width * height)
            for (i in pixels.indices) {
                val r = (pixels[i] shr 16) and 0xFF
                rPixels[i] = (0xFF shl 24) or (r shl 16) or (r shl 8) or r
            }
            val rSrc = com.google.zxing.RGBLuminanceSource(width, height, rPixels)
            val binBmpR = BinaryBitmap(HybridBinarizer(rSrc))
            val resR = reader.decode(binBmpR, QR_DECODE_HINTS)
            decodedList.add(resR.text)
        } catch (_: Exception) {}

        // 2. Green Plane Slicing
        try {
            val gPixels = IntArray(width * height)
            for (i in pixels.indices) {
                val g = (pixels[i] shr 8) and 0xFF
                gPixels[i] = (0xFF shl 24) or (g shl 16) or (g shl 8) or g
            }
            val gSrc = com.google.zxing.RGBLuminanceSource(width, height, gPixels)
            val binBmpG = BinaryBitmap(HybridBinarizer(gSrc))
            val resG = reader.decode(binBmpG, QR_DECODE_HINTS)
            decodedList.add(resG.text)
        } catch (_: Exception) {}

        // 3. Blue Plane Slicing
        try {
            val bPixels = IntArray(width * height)
            for (i in pixels.indices) {
                val b = pixels[i] and 0xFF
                bPixels[i] = (0xFF shl 24) or (b shl 16) or (b shl 8) or b
            }
            val bSrc = com.google.zxing.RGBLuminanceSource(width, height, bPixels)
            val binBmpB = BinaryBitmap(HybridBinarizer(bSrc))
            val resB = reader.decode(binBmpB, QR_DECODE_HINTS)
            decodedList.add(resB.text)
        } catch (_: Exception) {}

        return decodedList
    }

    /**
     * Parses incoming transmission packets (OB2 / OB3).
     */
    fun processIncomingPacket(
        raw: String,
        onMeta: (TransferMeta) -> Unit,
        onChunk: (idx: Int, total: Int, payload: String) -> Unit
    ) {
        try {
            val isRgb = raw.startsWith("OB3:")
            val parts = raw.split(":")
            if (parts.size >= 2) {
                when (parts[1]) {
                    "M" -> { // Metadata frame: OB2/OB3:M:fileId:name:rawSize:compSize:mime:totalChunks:checksum
                        val id = parts.getOrNull(2) ?: ""
                        val name = parts.getOrNull(3) ?: "archivo"
                        val rawSize = parts.getOrNull(4)?.toLongOrNull() ?: 0L
                        val compSize = parts.getOrNull(5)?.toLongOrNull() ?: 0L
                        val mime = parts.getOrNull(6) ?: "application/octet-stream"
                        val totalChunks = parts.getOrNull(7)?.toIntOrNull() ?: 1
                        val checksum = parts.getOrNull(8)?.toLongOrNull() ?: 0L
                        onMeta(TransferMeta(id, name, rawSize, compSize, mime, totalChunks, isRgb, checksum))
                    }
                    "D" -> { // Data frame: OB2/OB3:D:fileId:idx:totalChunks:dataBase64
                        val idx = parts.getOrNull(3)?.toIntOrNull() ?: 0
                        val total = parts.getOrNull(4)?.toIntOrNull() ?: 1
                        val base64 = parts.getOrNull(5) ?: ""
                        if (base64.isNotEmpty()) {
                            onChunk(idx, total, base64)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Reconstructs the original file bytes from all received Base64 chunks.
     */
    fun assembleTurboFile(
        chunks: Map<Int, String>,
        meta: TransferMeta?,
        context: Context
    ): Pair<ByteArray, File?> {
        val totalChunks = meta?.totalChunks ?: chunks.size
        val compBaos = ByteArrayOutputStream()

        for (i in 0 until totalChunks) {
            val b64 = chunks[i] ?: continue
            val chunkBytes = Base64.decode(b64, Base64.NO_WRAP)
            compBaos.write(chunkBytes)
        }

        val compressed = compBaos.toByteArray()
        val finalBytes = decompressData(compressed)
        val fileName = meta?.name ?: "video_transferido_${System.currentTimeMillis()}.mp4"

        val tempFile = File(context.cacheDir, fileName)
        try {
            FileOutputStream(tempFile).use { it.write(finalBytes) }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Pair(finalBytes, tempFile)
    }

    /**
     * Saves the reconstructed file to public Downloads directory.
     */
    fun saveFileToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    Toast.makeText(context, "Guardado en Descargas: $fileName", Toast.LENGTH_LONG).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, fileName)
                FileOutputStream(destFile).use { it.write(bytes) }
                Toast.makeText(context, "Guardado en Descargas: $fileName", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error guardando: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
