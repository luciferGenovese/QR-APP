import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

old_heart = """                com.example.utils.BodyShape.HEART -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx, cy + r * 0.7f)
                        cubicTo(pos.x, cy, pos.x, pos.y, cx, pos.y + r * 0.4f)
                        cubicTo(pos.x + modSize, pos.y, pos.x + modSize, cy, cx, cy + r * 0.7f)
                        close()
                    }
                    drawPath(path, color = tint)
                }"""

new_heart = """                com.example.utils.BodyShape.HEART -> {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        val mw = modSize
                        val mh = modSize
                        moveTo(cx, pos.y + mh * 0.9f)
                        cubicTo(pos.x - mw * 0.15f, pos.y + mh * 0.3f, cx - mw * 0.15f, pos.y - mh * 0.25f, cx, pos.y + mh * 0.2f)
                        cubicTo(cx + mw * 0.15f, pos.y - mh * 0.25f, pos.x + mw * 1.15f, pos.y + mh * 0.3f, cx, pos.y + mh * 0.9f)
                        close()
                    }
                    drawPath(path, color = tint)
                }"""

text = text.replace(old_heart, new_heart)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)
