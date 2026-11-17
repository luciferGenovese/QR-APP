import re

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'r') as f:
    text = f.read()

# Add EMOJI shape
old_list = """            Pair(context.getString(R.string.shape_pentagon), com.example.utils.BodyShape.PENTAGON),
            Pair(context.getString(R.string.shape_octagon), com.example.utils.BodyShape.OCTAGON)
        )"""

new_list = """            Pair(context.getString(R.string.shape_pentagon), com.example.utils.BodyShape.PENTAGON),
            Pair(context.getString(R.string.shape_octagon), com.example.utils.BodyShape.OCTAGON),
            Pair("Emoji (PRO)", com.example.utils.BodyShape.EMOJI)
        )"""
text = text.replace(old_list, new_list)

# Add customEmoji state
old_state = "var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }"
new_state = "var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }\n    var customEmojiText by remember { mutableStateOf(\"😃\") }"
text = text.replace(old_state, new_state)

with open('app/src/main/java/com/example/ui/screens/GenerateScreen.kt', 'w') as f:
    f.write(text)

