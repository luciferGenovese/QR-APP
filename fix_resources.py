import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

text = text.replace("R.drawable.template_frame_scan_me", "R.drawable.template_frame_scanme")
text = text.replace("R.drawable.template_frame_vip_gold", "R.drawable.template_frame_gold")
text = text.replace("R.drawable.template_frame_eco", "R.drawable.template_frame_nature")

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

