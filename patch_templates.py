import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'context\.startActivity\(Intent\.createChooser\(shareIntent, "Exportar PDF de Plantilla"\)\)\n\s*\}'
replacement = '''context.startActivity(Intent.createChooser(shareIntent, "Exportar PDF de Plantilla"))
            com.example.utils.ReviewManager.askForReview(context)
        }'''

content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Patched TemplatesScreen for Review")
