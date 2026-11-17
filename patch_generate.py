import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'context\.startActivity\(Intent\.createChooser\(shareIntent, "Imprimir o Compartir Hoja de Stickers"\)\)\n\s*\}\n\s*Toast\.makeText\(context, context\.getString\(R\.string\.toast_pdf_success\), Toast\.LENGTH_SHORT\)\.show\(\)'
replacement = '''context.startActivity(Intent.createChooser(shareIntent, "Imprimir o Compartir Hoja de Stickers"))
            com.example.utils.ReviewManager.askForReview(context)
        }
        Toast.makeText(context, context.getString(R.string.toast_pdf_success), Toast.LENGTH_SHORT).show()'''

content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Patched GenerateScreen for Review")
