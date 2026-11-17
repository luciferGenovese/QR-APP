import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    text = f.read()

# Fix 1: Stray parenthesis in TemplatesScreen
bad_1 = "            )\n            )"
good_1 = "            )"
text = text.replace(bad_1, good_1)

# Fix 2: Duplicated parameters and syntax error in TemplatesScreen TextStudioDialog
bad_2 = "                isUppercase = true,\n                showSubtext = true,\n                isBold = true"
good_2 = "                isUppercase = true\n                // showSubtext and isBold are no longer required by the block above since it's an apply {} block of a state holding object, wait, let me look at the code"
text = text.replace(bad_2, "                isUppercase = true")

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'w') as f:
    f.write(text)

