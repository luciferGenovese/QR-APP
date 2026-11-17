package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.TransferMeta
import com.example.ui.screens.UltraOpticalEngine
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UltraOpticalTransferTest {

    @Test
    fun testCompressionAndDecompressionLossless() {
        val originalText = "OMNIBEAM_TURBO_RGB_TEST_PAYLOAD_VERIFICATION_".repeat(200)
        val originalBytes = originalText.toByteArray(Charsets.UTF_8)

        val compressed = UltraOpticalEngine.compressData(originalBytes)
        assertTrue("Compressed data should be smaller than raw text", compressed.size < originalBytes.size)

        val decompressed = UltraOpticalEngine.decompressData(compressed)
        assertArrayEquals("Decompressed bytes must match original byte-for-byte", originalBytes, decompressed)
    }

    @Test
    fun testMonochromeFrameGeneration() {
        val payload = "Test payload for 1X optical transfer".repeat(50).toByteArray(Charsets.UTF_8)
        val bitmaps = UltraOpticalEngine.generateTurboFrames(
            fileBytes = payload,
            fileName = "sample.txt",
            mimeType = "text/plain",
            chunkSize = 200
        )

        assertTrue("Should generate at least metadata and 1 data frame", bitmaps.size >= 2)
        assertEquals(512, bitmaps[0].width)
        assertEquals(512, bitmaps[0].height)
    }

    @Test
    fun testRgb3XMultiplexingAndPlanarDecoding() {
        val testData = "RGB_TURBO_STREAM_".repeat(120).toByteArray(Charsets.UTF_8)
        val bitmaps = UltraOpticalEngine.generateRgbTurboFrames(
            fileBytes = testData,
            fileName = "rgb_video.mp4",
            mimeType = "video/mp4",
            chunkSize = 250
        )

        assertTrue("Should generate RGB frames", bitmaps.isNotEmpty())

        // The first data frame (index 1) is a 3-channel composite
        if (bitmaps.size > 1) {
            val compositeBitmap = bitmaps[1]
            val decodedChannels = UltraOpticalEngine.decodeRgbPlanesFromBitmap(compositeBitmap)
            assertTrue("Should decode multiple channels from the RGB composite bitmap", decodedChannels.isNotEmpty())
            for (packet in decodedChannels) {
                assertTrue("Decoded packet should have OB3 header prefix", packet.startsWith("OB3:"))
            }
        }
    }

    @Test
    fun testFullRgbFileTransferAssembly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val originalText = "Hello Optical RGB 3X World! Testing full end-to-end stream assembly.".repeat(40)
        val originalBytes = originalText.toByteArray(Charsets.UTF_8)

        val compressed = UltraOpticalEngine.compressData(originalBytes)
        val chunkSize = 300
        val totalChunks = (compressed.size + chunkSize - 1) / chunkSize

        val chunksMap = mutableMapOf<Int, String>()
        for (i in 0 until totalChunks) {
            val start = i * chunkSize
            val end = (start + chunkSize).coerceAtMost(compressed.size)
            val slice = compressed.copyOfRange(start, end)
            chunksMap[i] = android.util.Base64.encodeToString(slice, android.util.Base64.NO_WRAP)
        }

        val meta = TransferMeta(
            id = "123456",
            name = "test_assembled.txt",
            rawSize = originalBytes.size.toLong(),
            compressedSize = compressed.size.toLong(),
            mimeType = "text/plain",
            totalChunks = totalChunks,
            isRgbMode = true
        )

        val (assembledBytes, assembledFile) = UltraOpticalEngine.assembleTurboFile(
            chunks = chunksMap,
            meta = meta,
            context = context
        )

        assertNotNull("Assembled file should not be null", assembledFile)
        assertArrayEquals("Assembled bytes must match original byte-for-byte", originalBytes, assembledBytes)
        assertEquals(originalText, String(assembledBytes, Charsets.UTF_8))
    }
}
