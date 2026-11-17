import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

new_imports = """package com.example.utils

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.example.R
import com.example.utils.SmartQrFeatures.BodyShape
import com.example.utils.SmartQrFeatures.ErrorCorrectionLevel
import com.example.utils.SmartQrFeatures.LogoBadgeShape
import com.google.zxing.BarcodeFormat"""

old_imports = """package com.example.utils

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.example.R
import com.google.zxing.BarcodeFormat"""

text = text.replace(old_imports, new_imports)

old_data_class = """    // Added missing properties back
    val qrBodyShape: String = "SQUARE",
    val qrLogoBitmap: Bitmap? = null,
    val qrErrorCorrectionLevel: String = "Q",
    val qrLogoSizeRatio: Float = 0.2f,
    val qrLogoBadgeShape: String = "SQUARE",
    val isBold: Boolean = true,
    val isUppercase: Boolean = true,
    val showSubtext: Boolean = true,
    val qrScale: Float = 1.0f,
    val qrOffsetY: Float = 0f
)"""

new_data_class = """    // Added missing properties back
    val qrBodyShape: BodyShape = BodyShape.SQUARE,
    val qrLogoBitmap: Bitmap? = null,
    val qrErrorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.Q,
    val qrLogoSizeRatio: Float = 0.2f,
    val qrLogoBadgeShape: LogoBadgeShape = LogoBadgeShape.SQUARE,
    val isBold: Boolean = true,
    val isUppercase: Boolean = true,
    val showSubtext: Boolean = true,
    val qrScale: Float = 1.0f,
    val qrOffsetY: Float = 0f
)"""

text = text.replace(old_data_class, new_data_class)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

