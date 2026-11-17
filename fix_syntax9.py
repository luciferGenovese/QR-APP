import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    text = f.read()

# Fix 1: BorderStroke error in TemplatesScreen
# file:///app/applet/app/src/main/java/com/example/ui/screens/TemplatesScreen.kt:382:54 None of the following candidates is applicable:
# Let's find line 382
