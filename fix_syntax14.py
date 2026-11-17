import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    text = f.read()

# I deleted showSubtext = showSubtext from the TextStudioDialog call by accident in fix_screens.py probably.
# Let's put them back.

bad_1 = "            onFooterAccentChange = { footerAccentText = it },\n            onShowSubtextChange = { showSubtext = it },"
good_1 = "            onFooterAccentChange = { footerAccentText = it },\n            showSubtext = showSubtext,\n            onShowSubtextChange = { showSubtext = it },"
text = text.replace(bad_1, good_1)

bad_2 = "            onTextOffsetXChange = { textOffsetX = it },\n            onBoldChange = { isBold = it },"
good_2 = "            onTextOffsetXChange = { textOffsetX = it },\n            isBold = isBold,\n            onBoldChange = { isBold = it },\n            isUppercase = isUppercase,"
text = text.replace(bad_2, good_2)

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'w') as f:
    f.write(text)

