import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

clean_logic2 = """                // Base coordinates from the template
                var baseAnchorX = el.x
                
                // If the user changed the text alignment from the UI, we adapt the anchor point
                // Assuming el.x is the CENTER anchor when width is defined, we can shift left/right
                if (el.id == "title" || el.align != Paint.Align.CENTER) {
                    when (el.align) {
                        Paint.Align.LEFT -> baseAnchorX = el.x - (el.width / 2f) + (1024f * 0.12f)
                        Paint.Align.RIGHT -> baseAnchorX = el.x + (el.width / 2f) - (1024f * 0.12f)
                        Paint.Align.CENTER -> baseAnchorX = el.x
                    }
                }
                
                var adjustedX = baseAnchorX * scaleX
                var adjustedY = el.y * scaleY
                
                // Apply user UI offsets (sliders)
                if (el.id == "title" || el.id == "header") {
                    adjustedX += custom.textOffsetX * scaleX * 2f
                    adjustedY += custom.textOffsetY * scaleY * 2f
                } else if (el.id == "subtext" || el.id == "footer") {
                    // Subtext and footer move slightly slower
                    adjustedY += (custom.textOffsetY * 0.5f) * scaleY * 2f
                }"""

old_logic = """                // Base coordinates from the template
                var baseAnchorX = el.x
                
                // If the user changed the text alignment from the UI, we adapt the anchor point
                // Using 1024f as the base width
                if (el.id == "title") {
                    when (el.align) {
                        Paint.Align.LEFT -> baseAnchorX = 1024f * 0.12f
                        Paint.Align.RIGHT -> baseAnchorX = 1024f * 0.88f
                        Paint.Align.CENTER -> baseAnchorX = 1024f * 0.5f
                    }
                } else if (el.align == Paint.Align.CENTER) {
                    baseAnchorX = 1024f * 0.5f
                }
                
                var adjustedX = baseAnchorX * scaleX
                var adjustedY = el.y * scaleY
                
                // Apply user UI offsets (sliders)
                if (el.id == "title" || el.id == "header") {
                    adjustedX += custom.textOffsetX * scaleX * 2f
                    adjustedY += custom.textOffsetY * scaleY * 2f
                } else if (el.id == "subtext" || el.id == "footer") {
                    // Subtext and footer move slightly slower
                    adjustedY += (custom.textOffsetY * 0.5f) * scaleY * 2f
                }"""

text = text.replace(old_logic, clean_logic2)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

