import re

with open('app/src/main/java/com/example/utils/CodeGenerator.kt', 'r') as f:
    text = f.read()

old_scale = """                            val scale = 0.7f // 70% of the size, leaving 30% gap
                            val cx = left + modWidth / 2f"""

new_scale = """                            val scale = when (bodyShape) {
                                BodyShape.HEART -> 1.05f
                                BodyShape.STAR -> 0.95f
                                BodyShape.CROSS -> 0.9f
                                BodyShape.DIAMOND -> 0.85f
                                BodyShape.TRIANGLE -> 0.9f
                                BodyShape.PENTAGON -> 0.9f
                                BodyShape.OCTAGON -> 0.85f
                                BodyShape.HEXAGON -> 0.85f
                                else -> 0.75f
                            }
                            val cx = left + modWidth / 2f"""

text = text.replace(old_scale, new_scale)

with open('app/src/main/java/com/example/utils/CodeGenerator.kt', 'w') as f:
    f.write(text)

