import re

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

target = """                val preview = CodeGenerator.generateBitmap(
                    content = currentContent,
                    format = format,
                    width = bw,
                    height = bh,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedBodyShape,
                    logoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                )
                livePreviewBitmap = if (cachedTemplateBitmap != null && preview != null) {
                    compositeWithCachedTemplate(cachedTemplateBitmap!!, preview)
                } else {
                    preview
                }"""

new_target = """                val rawBitmap = CodeGenerator.generateBitmap(
                    content = currentContent,
                    format = format,
                    width = bw,
                    height = bh,
                    foregroundColor = selectedForegroundColor,
                    backgroundColor = selectedBackgroundColor,
                    bodyShape = selectedBodyShape,
                    logoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                )
                val preview = if (selectedTemplateDef != null && rawBitmap != null) {
                    try {
                        val custom = TemplateCustomization(
                            templateDef = selectedTemplateDef!!,
                            qrContent = currentContent,
                            titleText = customTitleText.ifBlank { selectedTemplateDef!!.defaultText },
                            subtext = customSubText.ifBlank { selectedTemplateDef!!.defaultSubtext },
                            qrForegroundColor = selectedForegroundColor,
                            qrBackgroundColor = selectedBackgroundColor,
                            qrBodyShape = selectedBodyShape,
                            qrLogoBitmap = if (removeLogo) null else (selectedLogoBitmap ?: getSocialLogo(context, type))
                        )
                        TemplateRenderer.renderTemplateBitmap(context, custom, 1024, 1280)
                    } catch (e: Exception) {
                        rawBitmap
                    }
                } else {
                    rawBitmap
                }
                
                livePreviewBitmap = if (cachedTemplateBitmap != null && preview != null) {
                    compositeWithCachedTemplate(cachedTemplateBitmap!!, preview)
                } else {
                    preview
                }"""

if target in content:
    content = content.replace(target, new_target)
else:
    print("Not found preview!")

with open("app/src/main/java/com/example/ui/screens/GenerateScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)
