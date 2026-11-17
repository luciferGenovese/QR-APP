import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

old_octagon = """                com.example.utils.BodyShape.OCTAGON -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        for (i in 0 until 8) {
                            val angle = (i * 2.0 * Math.PI / 8.0) - (Math.PI / 8.0)
                            val px = cx + (r * kotlin.math.cos(angle)).toFloat()
                            val py = cy + (r * kotlin.math.sin(angle)).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path, color = tint)
                }
            }"""

new_octagon = """                com.example.utils.BodyShape.OCTAGON -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        for (i in 0 until 8) {
                            val angle = (i * 2.0 * Math.PI / 8.0) - (Math.PI / 8.0)
                            val px = cx + (r * kotlin.math.cos(angle)).toFloat()
                            val py = cy + (r * kotlin.math.sin(angle)).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path, color = tint)
                }
                com.example.utils.BodyShape.EMOJI -> {
                    val textPaint = android.graphics.Paint().apply {
                        color = tint.toArgb()
                        textSize = modSize * 1.1f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val textBounds = android.graphics.Rect()
                    textPaint.getTextBounds("😀", 0, 1, textBounds)
                    drawContext.canvas.nativeCanvas.drawText("😀", cx, cy - textBounds.exactCenterY(), textPaint)
                }
                else -> {}
            }"""

text = text.replace(old_octagon, new_octagon)

if "import androidx.compose.ui.graphics.toArgb" not in text:
    text = text.replace("import androidx.compose.ui.graphics.Color", "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.toArgb")

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

