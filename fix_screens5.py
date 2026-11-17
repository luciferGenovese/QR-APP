import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    text = f.read()

# Let's fix missing parameters in QuickEditDialog and TextStudioDialog calls.
# I removed isBold, isUppercase, showSubtext from them earlier.

# QuickEditDialog missing: isBold, isUppercase
q_str = "            colorPalette = colorPalette,"
q_rep = "            colorPalette = colorPalette,\n            isBold = true,\n            isUppercase = true,"
text = text.replace(q_str, q_rep)

# TextStudioDialog missing: showSubtext, isBold, isUppercase
t_str = "                isUppercase = true"
t_rep = "                isUppercase = true,\n                showSubtext = true,\n                isBold = true"
text = text.replace(t_str, t_rep)

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'w') as f:
    f.write(text)

