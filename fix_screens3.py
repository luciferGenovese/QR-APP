import re

# In my previous script, I blindly did:
# text = re.sub(r',\s*qrBodyShape\s*=\s*[^,)]+', '', text)
# which broke everything. Let's fix those lines.

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

bad_line = "                            qrBackgroundColor = selectedBackgroundColor) null else (selectedLogoBitmap ?: getSocialLogo(context, type)))"
good_line = "                            qrBackgroundColor = selectedBackgroundColor,\n                            logoBitmap = if (type == \"Wifi\" || type == \"Email\" || type == \"SMS\" || type == \"VCard\" || type == \"Text\" || type == \"Event\") null else (selectedLogoBitmap ?: getSocialLogo(context, type))\n                        )"

text = text.replace(bad_line, good_line)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    text2 = f.read()

# Let's fix TemplatesScreen too
# First let's find the broken lines

