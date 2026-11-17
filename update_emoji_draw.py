import re

with open('app/src/main/java/com/example/utils/CodeGenerator.kt', 'r') as f:
    text = f.read()

old_octagon = """                                BodyShape.OCTAGON -> {
                                    val radius = Math.min(vWidth, vHeight) / 2f
                                    val path = Path()
                                    for (i in 0 until 8) {
                                        val angle = (i * Math.PI * 2 / 8) - Math.PI / 2 + Math.PI / 8
                                        val px = (cx + radius * Math.cos(angle)).toFloat()
                                        val py = (cy + radius * Math.sin(angle)).toFloat()
                                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                    }
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }"""

new_octagon = """                                BodyShape.OCTAGON -> {
                                    val radius = Math.min(vWidth, vHeight) / 2f
                                    val path = Path()
                                    for (i in 0 until 8) {
                                        val angle = (i * Math.PI * 2 / 8) - Math.PI / 2 + Math.PI / 8
                                        val px = (cx + radius * Math.cos(angle)).toFloat()
                                        val py = (cy + radius * Math.sin(angle)).toFloat()
                                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                    }
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }
                                BodyShape.EMOJI -> {
                                    val textPaint = android.graphics.Paint().apply {
                                        color = foregroundColor
                                        textSize = vHeight * 1.2f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    val textBounds = android.graphics.Rect()
                                    textPaint.getTextBounds(customEmoji, 0, customEmoji.length, textBounds)
                                    canvas.drawText(customEmoji, cx, cy - textBounds.exactCenterY(), textPaint)
                                }"""
text = text.replace(old_octagon, new_octagon)

with open('app/src/main/java/com/example/utils/CodeGenerator.kt', 'w') as f:
    f.write(text)

