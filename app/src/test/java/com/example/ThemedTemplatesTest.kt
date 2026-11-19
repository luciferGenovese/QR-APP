package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.utils.TemplateCustomization
import com.example.utils.TemplateRenderer
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
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
class ThemedTemplatesTest {
    @Test
    fun everyTemplateContainsAReadableQr() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val content = "https://example.com/template-check"
        val templates = TemplateRenderer.AVAILABLE_TEMPLATES
        assertTrue(templates.map { it.category }.distinct().size >= 5)
        assertEquals(templates.size, templates.map { it.id }.distinct().size)
        for (template in templates) {
            val bitmap = TemplateRenderer.renderTemplateBitmap(
                context,
                TemplateCustomization(
                    templateDef = template,
                    qrContent = content,
                    titleText = template.defaultText,
                    subtext = template.defaultSubtext,
                    textColor = template.defaultTextColor
                ),
                512,
                640
            )
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val luminance = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
            val decoded = QRCodeReader().decode(BinaryBitmap(HybridBinarizer(luminance)))
            assertEquals(template.name, content, decoded.text)
            bitmap.recycle()
        }
    }
}
