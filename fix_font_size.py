import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

text = text.replace(
    "newSize = custom.textSizeSp * 0.5f * (1024f / 512f)", 
    "newSize = custom.textSizeSp * 0.5f * density * (1024f / 512f)"
)
text = text.replace(
    "newSize = custom.textSizeSp * (1024f / 512f)", 
    "newSize = custom.textSizeSp * density * (1024f / 512f)"
)
text = text.replace(
    "newSize = (custom.subtextSizeSp * 1.1f) * (1024f / 512f)", 
    "newSize = (custom.subtextSizeSp * 1.1f) * density * (1024f / 512f)"
)
text = text.replace(
    "newSize = (custom.subtextSizeSp * 1.2f) * (1024f / 512f)", 
    "newSize = (custom.subtextSizeSp * 1.2f) * density * (1024f / 512f)"
)
text = text.replace(
    "newSize = custom.subtextSizeSp * (1024f / 512f)", 
    "newSize = custom.subtextSizeSp * density * (1024f / 512f)"
)
text = text.replace(
    "newSize = custom.subtextSizeSp * 0.8f * (1024f / 512f)", 
    "newSize = custom.subtextSizeSp * 0.8f * density * (1024f / 512f)"
)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

