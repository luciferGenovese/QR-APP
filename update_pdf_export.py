import re

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'private fun exportTemplatePdf\(.*?\}\n\}'
replacement = '''private fun exportTemplatePdf(
    context: Context,
    bitmap: Bitmap,
    templateName: String,
    titleText: String,
    content: String
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Title
        paint.color = Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Hoja de Etiquetas Imprimibles (12 Stickers)", 40f, 40f, paint)

        // Metadata
        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Estilo: $templateName | Texto: $titleText", 40f, 60f, paint)
        
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, 70f, 555f, 70f, paint)

        // Draw 3 columns x 4 rows = 12 stickers
        val cols = 3
        val rows = 4
        val stickerWidth = 144f
        val stickerHeight = 180f
        val gapX = 25f
        val gapY = 15f
        val marginX = (595f - (cols * stickerWidth + (cols - 1) * gapX)) / 2f
        val marginY = 85f

        val scaled = Bitmap.createScaledBitmap(bitmap, stickerWidth.toInt(), stickerHeight.toInt(), true)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = marginX + c * (stickerWidth + gapX)
                val top = marginY + r * (stickerHeight + gapY)
                
                // Draw shadow or border if desired, but template is usually dark/colored.
                canvas.drawBitmap(scaled, left, top, null)
            }
        }

        // Watermark / Footer
        paint.color = Color.GRAY
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("Generado por QR OFICIAL para Android - Formato A4", 40f, 820f, paint)

        pdfDocument.finishPage(page)

        val cachePath = File(context.cacheDir, "shared_templates")
        if (!cachePath.exists()) cachePath.mkdirs()
        val file = File(cachePath, "plantilla_qr.pdf")
        val stream = FileOutputStream(file)
        pdfDocument.writeTo(stream)
        stream.close()
        pdfDocument.close()

        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "com.aistudio.omnicode.zvyf.fileprovider",
            file
        )

        if (contentUri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, "application/pdf")
                putExtra(Intent.EXTRA_STREAM, contentUri)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Exportar PDF de Plantilla"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error en PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}'''

new_content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/TemplatesScreen.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Updated exportTemplatePdf.")
