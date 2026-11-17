import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

old_fun = """    logoSizeRatio: Float,
    logoBadgeShape: LogoBadgeShape,
    targetDimension: Int
) {"""

new_fun = """    logoSizeRatio: Float,
    logoBadgeShape: LogoBadgeShape,
    targetDimension: Int,
    customEmoji: String = "😀"
) {"""

text = text.replace(old_fun, new_fun)

old_body = """            logoBadgeShape = logoBadgeShape,
            customEmoji = customEmojiText
        )"""

new_body = """            logoBadgeShape = logoBadgeShape,
            customEmoji = customEmoji
        )"""

text = text.replace(old_body, new_body)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

