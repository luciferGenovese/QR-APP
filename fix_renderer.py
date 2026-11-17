import re

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'r') as f:
    text = f.read()

# Let's fix the elements in AVAILABLE_TEMPLATES to have x=512f if they are centered, and adjust x for align left/right.
# Since all current elements are CENTER, we just change x=128f to x=512f in TemplateTextElement constructors.
# But wait, there is a title with alignment support. 
# Let's just do a clean regex replacement for the hardcoded logic inside `renderTemplateBitmap`.

hardcoded_logic = """                // Adjust offsets
                var adjustedX = el.x * scaleX
                var adjustedY = el.y * scaleY
                
                if (el.id == "title" || el.id == "header") {
                    adjustedX += custom.textOffsetX * scaleX * 2f
                    adjustedY += custom.textOffsetY * scaleY * 2f
                    
                    // Specific title X alignment logic based on old behavior
                    if (el.id == "title") {
                        val baseTitleX = if (custom.templateDef.id == "escanea_aqui") {
                            when (el.align) {
                                Paint.Align.LEFT -> targetWidth * 0.255f
                                Paint.Align.RIGHT -> targetWidth * 0.86f
                                Paint.Align.CENTER -> targetWidth * 0.56f
                            }
                        } else {
                            when (el.align) {
                                Paint.Align.LEFT -> targetWidth * 0.12f
                                Paint.Align.RIGHT -> targetWidth * 0.88f
                                Paint.Align.CENTER -> targetWidth * 0.5f
                            }
                        }
                        adjustedX = baseTitleX + (custom.textOffsetX * scaleX * 2f)
                    } else if (el.id == "header") {
                        adjustedX = targetWidth / 2f
                    }
                }
                
                if (el.id == "subtext" || el.id == "footer") {
                    adjustedY += (custom.textOffsetY * 0.5f) * scaleY * 2f
                    adjustedX = targetWidth / 2f
                }"""

clean_logic = """                // Base coordinates from the template
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

text = text.replace(hardcoded_logic, clean_logic)

with open('app/src/main/java/com/example/utils/TemplateRenderer.kt', 'w') as f:
    f.write(text)

