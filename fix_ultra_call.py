import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

old_call = """                                    logoSizeRatio = selectedLogoSizeRatio,
                                    logoBadgeShape = selectedLogoBadgeShape,
                                    targetDimension = resPx
                                )"""

new_call = """                                    logoSizeRatio = selectedLogoSizeRatio,
                                    logoBadgeShape = selectedLogoBadgeShape,
                                    targetDimension = resPx,
                                    customEmoji = customEmojiText
                                )"""

text = text.replace(old_call, new_call)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

