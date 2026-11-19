package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.MonoTransferSender
import com.example.ui.screens.MonoTransferReceiver
import com.example.ui.screens.renderMonoTransferQr
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.util.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MonoTransferTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun reassemblesOutOfOrderWithDuplicatesAndRejectsCorruptOrForeignPackets() {
        val bytes = ByteArray(2100).also { Random(42).nextBytes(it) }
        val source = File(context.cacheDir, "source-test.bin").apply { writeBytes(bytes) }
        val otherFile = File(context.cacheDir, "other-test.bin").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        MonoTransferSender.prepare(context, Uri.fromFile(source)).use { sender ->
            MonoTransferSender.prepare(context, Uri.fromFile(otherFile)).use { other ->
                MonoTransferReceiver(context).use { receiver ->
                    assertFalse(receiver.accept(sender.dataPacket(0)))
                    assertTrue(receiver.accept(sender.metadataPacket()))
                    assertFalse(receiver.accept(other.metadataPacket()))
                    assertFalse(receiver.accept(other.dataPacket(0)))
                    val packet = sender.dataPacket(0)
                    val altered = packet.split(':').toMutableList().apply { this[4] = "0" }.joinToString(":")
                    assertFalse(receiver.accept(altered))
                    for (index in (0 until sender.info.totalChunks).reversed()) {
                        assertTrue(receiver.accept(sender.dataPacket(index)))
                        assertFalse(receiver.accept(sender.dataPacket(index)))
                    }
                    assertEquals(sender.info.totalChunks, receiver.receivedCount)
                    assertArrayEquals(bytes, receiver.finish().readBytes())
                }
            }
        }
        source.delete()
        otherFile.delete()
    }

    @Test
    fun qrFramesAreMonochromeAndDecodeExactly() {
        val source = File(context.cacheDir, "qr-frame-test.bin").apply { writeText("Contenido de prueba") }
        MonoTransferSender.prepare(context, Uri.fromFile(source)).use { sender ->
            for (packet in listOf(sender.metadataPacket(), sender.dataPacket(0))) {
                val bitmap = renderMonoTransferQr(packet)
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                assertTrue(pixels.all { it == android.graphics.Color.BLACK || it == android.graphics.Color.WHITE })
                val sourceImage = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
                assertEquals(packet, QRCodeReader().decode(BinaryBitmap(HybridBinarizer(sourceImage))).text)
            }
        }
        source.delete()
    }

    @Test
    fun emptyFileTransfersAndVerifies() {
        val source = File(context.cacheDir, "empty-test.bin").apply { writeBytes(byteArrayOf()) }
        MonoTransferSender.prepare(context, Uri.fromFile(source)).use { sender ->
            MonoTransferReceiver(context).use { receiver ->
                assertTrue(receiver.accept(sender.metadataPacket()))
                assertTrue(receiver.accept(sender.dataPacket(0)))
                assertEquals(0L, receiver.finish().length())
            }
        }
        source.delete()
    }

    @Test
    fun rejectsFileWhenWholeFileHashDoesNotMatch() {
        val source = File(context.cacheDir, "hash-test.bin").apply { writeText("Verificación") }
        MonoTransferSender.prepare(context, Uri.fromFile(source)).use { sender ->
            MonoTransferReceiver(context).use { receiver ->
                val metadata = sender.metadataPacket().replace(sender.info.sha256, "0".repeat(64))
                assertTrue(receiver.accept(metadata))
                for (index in 0 until sender.info.totalChunks) {
                    assertTrue(receiver.accept(sender.dataPacket(index)))
                }
                assertThrows(IllegalStateException::class.java) { receiver.finish() }
            }
        }
        source.delete()
    }
}
