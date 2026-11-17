import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

# Fix the first when(shape) inside the canvas drawing (not the ShapeMiniPreview when)
# Wait, let's just add it to the ShapeMiniPreview when.
# Let's check exactly where the error is: line 3991
# It says "when expression must be exhaustive"
old_when = """                com.example.utils.BodyShape.OCTAGON -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        for (i in 0 until 8) {
                            val angle = (i * Math.PI * 2 / 8.0) - (Math.PI / 2.0) + (Math.PI / 8.0)
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
                        color = android.graphics.Color.BLACK
                        textSize = modSize * 1.2f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    val textBounds = android.graphics.Rect()
                    textPaint.getTextBounds("😀", 0, 1, textBounds)
                    drawContext.canvas.nativeCanvas.drawText("😀", cx, cy - textBounds.exactCenterY(), textPaint)
                }
            }"""

new_when = """                com.example.utils.BodyShape.OCTAGON -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        for (i in 0 until 8) {
                            val angle = (i * Math.PI * 2 / 8.0) - (Math.PI / 2.0) + (Math.PI / 8.0)
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
text = text.replace(old_when, new_when)

if "import androidx.compose.ui.graphics.toArgb" not in text:
    text = text.replace("import androidx.compose.ui.graphics.Color", "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.toArgb")

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

