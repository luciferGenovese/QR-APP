import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    text = f.read()

# Fix 1: BorderStroke brace
#                 border = androidx.compose.foundation.BorderStroke(
#                     1.dp,
#                     MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
#                 ) {
#                 Column(
bad_1 = """                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ) {"""
good_1 = """                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {"""
text = text.replace(bad_1, good_1)

# Fix 2: Argument already passed for this parameter in TextStudioDialog call
bad_2 = """                isUppercase = true
            },
            showSubtext = true,
            isBold = true,
            isUppercase = true,
            onDismiss ="""
good_2 = """                isUppercase = true
            },
            onDismiss ="""
text = text.replace(bad_2, good_2)

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'w') as f:
    f.write(text)


with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()
    
# Fix 3: SmartQrFeatures unresolved again
# Looking at the original file before my modifications, it imported `SmartQrFeatures` but I replaced it with `com.example.utils.SmartQrFeatures`
# Let's just restore the file completely from a clean slate. I'll get the old templaterenderer logic and put it in.
