import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r') as f:
    text = f.read()

# I see it now.
# In the TextStudioDialog CALL:
#                 isBold = true
#                 isUppercase = true
#             },
#             onDismiss = { showTextStudioDialog = false }

# It's missing: showSubtext = showSubtext, onShowSubtextChange = { showSubtext = it },
# Wait, the error is file:///app/applet/app/src/main/java/com/example/ui/screens/TemplatesScreen.kt:1102:13 No value passed for parameter 'showSubtext'.

# So the block starting around line 1059:
#         TextStudioDialog(
#             initialTarget = activeEditTarget,
#             titleText = titleText,
#             onTitleChange = { titleText = it },
#             headerAccentText = headerAccentText,
#             onHeaderAccentChange = { headerAccentText = it },
#             subtext = subtext,
#             onSubtextChange = { subtext = it },
#             footerAccentText = footerAccentText,
#             onFooterAccentChange = { footerAccentText = it },
#             showSubtext = showSubtext,
#             onShowSubtextChange = { showSubtext = it },

# Let's see what's actually there.
