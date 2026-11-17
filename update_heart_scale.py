import re

with open('app/src/main/java/com/example/utils/CodeGenerator.kt', 'r') as f:
    text = f.read()

text = text.replace("BodyShape.HEART -> 1.05f", "BodyShape.HEART -> 0.85f")
text = text.replace("BodyShape.STAR -> 0.95f", "BodyShape.STAR -> 0.85f")

with open('app/src/main/java/com/example/utils/CodeGenerator.kt', 'w') as f:
    f.write(text)
