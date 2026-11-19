package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.CodeEntity
import com.example.data.CodeRepository
import com.example.ui.MainViewModel
import com.example.ui.screens.DataSettingsScreen
import com.example.ui.screens.escapeWifiField
import com.example.ui.screens.parseWifiForEditor
import com.example.ui.screens.parseWifiSsidAndPassword
import com.example.utils.CodeGenerator
import com.example.utils.SecretQrHelper
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BugFixesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun watermarkedQrRemainsScannable() {
        val payload = "https://qroficial.example/verified-document"
        val raw = CodeGenerator.generateBitmapOrThrow(
            content = payload,
            format = BarcodeFormat.QR_CODE,
            width = 512,
            height = 512,
            errorCorrectionLevel = ErrorCorrectionLevel.H
        )
        val watermarked = CodeGenerator.applyWatermarkOverlay(raw, "OFICIAL")
        assertEquals(512, watermarked.width)
        assertEquals(512, watermarked.height)

        // Ensure center badge actually drew pixels onto the QR canvas
        assertFalse(raw.sameAs(watermarked))

        val pixels = IntArray(watermarked.width * watermarked.height)
        watermarked.getPixels(pixels, 0, watermarked.width, 0, 0, watermarked.width, watermarked.height)
        val source = RGBLuminanceSource(watermarked.width, watermarked.height, pixels)
        val decoded = QRCodeReader().decode(BinaryBitmap(HybridBinarizer(source)))
        assertEquals(payload, decoded.text)

        raw.recycle()
        watermarked.recycle()
    }

    @Test
    fun secretQrRoundtripAndWrongPinRejected() {
        val secret = "Clave bancaria 987654"
        val pin = "4321"
        val encrypted = SecretQrHelper.encrypt(secret, pin)
        assertTrue(SecretQrHelper.isSecretQr(encrypted))
        assertEquals(secret, SecretQrHelper.decrypt(encrypted, pin))
        assertNull(SecretQrHelper.decrypt(encrypted, "0000"))
    }

    @Test
    fun wifiEscapeAndParseRoundtrip() {
        val ssid = "Cafe;WiFi:Guest\\1"
        val password = "pass;word:12,3\\4"
        val encoded = "WIFI:S:${escapeWifiField(ssid)};T:WPA2;P:${escapeWifiField(password)};;"

        val (parsedSsid, parsedPass, parsedSec) = parseWifiForEditor(encoded)
        assertEquals(ssid, parsedSsid)
        assertEquals(password, parsedPass)
        assertEquals("WPA2", parsedSec)

        val (scanSsid, scanPass) = parseWifiSsidAndPassword(encoded)
        assertEquals(ssid, scanSsid)
        assertEquals(password, scanPass)
    }

    @Test
    fun deleteCodeAndDeleteAllCleanUpFiles() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = CodeRepository(db.codeDao())
        val prefs = context.getSharedPreferences("bugfixes_test_prefs", Context.MODE_PRIVATE)
        val viewModel = MainViewModel(context, repo, prefs)

        try {
            val id1 = repo.insert(CodeEntity(content = "item-1", format = "QR_CODE", type = "CREATE"))
            val id2 = repo.insert(CodeEntity(content = "item-2", format = "QR_CODE", type = "CREATE"))
            val file1 = File(context.filesDir, "qr_$id1.png").apply { writeText("png1") }
            val file2 = File(context.filesDir, "qr_$id2.png").apply { writeText("png2") }
            assertTrue(file1.exists())
            assertTrue(file2.exists())

            viewModel.deleteCode(id1.toInt())
            waitUntil { !file1.exists() && repo.allCodes.first().size == 1 }
            assertFalse(file1.exists())
            assertTrue(file2.exists())

            viewModel.deleteAllCodes()
            waitUntil { !file2.exists() && repo.allCodes.first().isEmpty() }
            assertFalse(file2.exists())
        } finally {
            File(context.filesDir, "qr_1.png").delete()
            File(context.filesDir, "qr_2.png").delete()
            db.close()
        }
    }

    @Test
    fun dataSettingsScreenRequiresConfirmationBeforeWipe() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = CodeRepository(db.codeDao())
        val prefs = context.getSharedPreferences("bugfixes_settings_prefs", Context.MODE_PRIVATE)
        val viewModel = MainViewModel(context, repo, prefs)

        runBlocking {
            repo.insert(CodeEntity(content = "keep-me", format = "QR_CODE", type = "CREATE"))
        }

        try {
            composeTestRule.setContent {
                DataSettingsScreen(viewModel = viewModel, onBack = {})
            }
            composeTestRule.onNodeWithText(context.getString(R.string.btn_delete_all)).performClick()
            composeTestRule.onNodeWithText(context.getString(R.string.dialog_title_clear_history)).assertIsDisplayed()

            composeTestRule.onNodeWithText(context.getString(R.string.btn_cancel)).performClick()
            composeTestRule.waitForIdle()

            val remaining = runBlocking { repo.allCodes.first() }
            assertNotNull(remaining.singleOrNull())
        } finally {
            db.close()
        }
    }

    private suspend fun waitUntil(timeoutMs: Long = 3000L, predicate: suspend () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (predicate()) return
            kotlinx.coroutines.delay(40)
        }
    }
}
