package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.CodeEntity
import com.example.data.CodeRepository
import com.example.utils.CodeGenerator
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QrWorkflowTest {
    @Test
    fun scannedCodeRemainsInHistoryAfterDatabaseReopens() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "qr-workflow-test.db"
        val scannedCode = CodeEntity(content = "https://example.com/scan", format = "QR_CODE", type = "SCAN")

        val databaseBuilder = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
        try {
            val writer = databaseBuilder.build()
            try {
                CodeRepository(writer.codeDao()).insert(scannedCode)
            } finally {
                writer.close()
            }

            val reader = databaseBuilder.build()
            try {
                val restored = CodeRepository(reader.codeDao()).allCodes.first().single()
                assertTrue(restored.id > 0)
                assertEquals(scannedCode, restored.copy(id = 0))
            } finally {
                reader.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun generatedQrCanBeDecodedFromItsPng() {
        val content = "QR OFICIAL - prueba de exportacion"
        val generated = CodeGenerator.generateBitmapOrThrow(content, BarcodeFormat.QR_CODE)
        val png = ByteArrayOutputStream().use { output ->
            assertTrue(generated.compress(Bitmap.CompressFormat.PNG, 100, output))
            output.toByteArray()
        }

        val decoded = BitmapFactory.decodeByteArray(png, 0, png.size)
        val pixels = IntArray(decoded.width * decoded.height)
        decoded.getPixels(pixels, 0, decoded.width, 0, 0, decoded.width, decoded.height)
        val source = RGBLuminanceSource(decoded.width, decoded.height, pixels)
        val result = QRCodeReader().decode(BinaryBitmap(HybridBinarizer(source)))

        assertEquals(content, result.text)
    }
}
