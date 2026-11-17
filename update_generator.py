import re

with open('app/src/main/java/com/example/utils/CodeGenerator.kt', 'r') as f:
    text = f.read()

# Replace BodyShape
old_enum = "enum class BodyShape { SQUARE, CIRCLE, ROUNDED, DIAMOND, CROSS, STAR, DOT, HEXAGON, HEART, TRIANGLE, PENTAGON, OCTAGON }"
new_enum = "enum class BodyShape { SQUARE, CIRCLE, ROUNDED, DIAMOND, CROSS, STAR, DOT, HEXAGON, HEART, TRIANGLE, PENTAGON, OCTAGON, EMOJI }"
text = text.replace(old_enum, new_enum)

# generateBitmapOrThrow
old_fun1 = """        logoSizeRatio: Float = 0.22f,
        logoBadgeShape: LogoBadgeShape = LogoBadgeShape.ROUNDED_SQUARE
    ): Bitmap {"""
new_fun1 = """        logoSizeRatio: Float = 0.22f,
        logoBadgeShape: LogoBadgeShape = LogoBadgeShape.ROUNDED_SQUARE,
        customEmoji: String = "😀"
    ): Bitmap {"""
text = text.replace(old_fun1, new_fun1)

# generateBitmap
old_fun2 = """        logoSizeRatio: Float = 0.22f,
        logoBadgeShape: LogoBadgeShape = LogoBadgeShape.ROUNDED_SQUARE
    ): Bitmap? {"""
new_fun2 = """        logoSizeRatio: Float = 0.22f,
        logoBadgeShape: LogoBadgeShape = LogoBadgeShape.ROUNDED_SQUARE,
        customEmoji: String = "😀"
    ): Bitmap? {"""
text = text.replace(old_fun2, new_fun2)

# internal call
old_call1 = """                errorCorrectionLevel, logoSizeRatio, logoBadgeShape
            )"""
new_call1 = """                errorCorrectionLevel, logoSizeRatio, logoBadgeShape, customEmoji
            )"""
text = text.replace(old_call1, new_call1)

with open('app/src/main/java/com/example/utils/CodeGenerator.kt', 'w') as f:
    f.write(text)

