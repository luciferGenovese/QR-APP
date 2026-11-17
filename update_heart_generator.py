import re

with open('app/src/main/java/com/example/utils/CodeGenerator.kt', 'r') as f:
    text = f.read()

old_heart = """                                BodyShape.HEART -> {
                                    val path = Path()
                                    val mw = vWidth
                                    val mh = vHeight
                                    path.moveTo(cx, vTop + mh * 0.9f)
                                    path.cubicTo(vLeft - mw * 0.1f, vTop + mh * 0.3f, cx - mw * 0.1f, vTop - mh * 0.2f, cx, vTop + mh * 0.2f)
                                    path.cubicTo(cx + mw * 0.1f, vTop - mh * 0.2f, vRight + mw * 0.1f, vTop + mh * 0.3f, cx, vTop + mh * 0.9f)
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }"""

new_heart = """                                BodyShape.HEART -> {
                                    val path = Path()
                                    val mw = vWidth
                                    val mh = vHeight
                                    path.moveTo(cx, vTop + mh * 0.9f)
                                    path.cubicTo(vLeft - mw * 0.15f, vTop + mh * 0.3f, cx - mw * 0.15f, vTop - mh * 0.25f, cx, vTop + mh * 0.2f)
                                    path.cubicTo(cx + mw * 0.15f, vTop - mh * 0.25f, vRight + mw * 0.15f, vTop + mh * 0.3f, cx, vTop + mh * 0.9f)
                                    path.close()
                                    canvas.drawPath(path, paint)
                                }"""

text = text.replace(old_heart, new_heart)

with open('app/src/main/java/com/example/utils/CodeGenerator.kt', 'w') as f:
    f.write(text)
