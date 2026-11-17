with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

print("Contains fitTextSize?", "fitTextSize" in text)
print("Contains drawFormattedText?", "drawFormattedText" in text)
