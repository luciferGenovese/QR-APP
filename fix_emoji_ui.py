import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

old_click = ".clickable { selectedBodyShape = shapeEnum }"
new_click = """.clickable {
                                                    if (shapeEnum == com.example.utils.BodyShape.EMOJI && !isPro) {
                                                        showPaywallDialog = true
                                                    } else {
                                                        selectedBodyShape = shapeEnum
                                                    }
                                                }"""
text = text.replace(old_click, new_click)


old_chunked_loop = """                                chunkedShapes.forEach { rowShapes ->"""
new_chunked_loop = """                                if (selectedBodyShape == com.example.utils.BodyShape.EMOJI) {
                                    androidx.compose.material3.OutlinedTextField(
                                        value = customEmojiText,
                                        onValueChange = { if (it.length <= 2) customEmojiText = it },
                                        label = { Text("Escribe tu Emoji") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                    )
                                }
                                chunkedShapes.forEach { rowShapes ->"""
text = text.replace(old_chunked_loop, new_chunked_loop)


# Fix the preview for EMOJI
old_preview_close = """                com.example.utils.BodyShape.OCTAGON -> {
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
            }"""

new_preview_close = """                com.example.utils.BodyShape.OCTAGON -> {
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

text = text.replace(old_preview_close, new_preview_close)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

