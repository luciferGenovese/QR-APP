package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfAndPrintHelper {

    /**
     * Sends a single QR poster directly to Android PrintManager (Wi-Fi, Bluetooth or Save as PDF).
     */
    fun printQrPoster(context: Context, qrBitmap: Bitmap, title: String, subtext: String = "Escanea con tu cámara") {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Servicio de impresión no disponible", Toast.LENGTH_SHORT).show()
                return
            }

            val jobName = "QR_Poster_${System.currentTimeMillis()}"
            val printAdapter = object : PrintDocumentAdapter() {
                private var pdfDocument: PdfDocument? = null

                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }

                    val pdi = PrintDocumentInfo.Builder(jobName)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()

                    callback?.onLayoutFinished(pdi, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    if (destination == null) {
                        callback?.onWriteFailed("Destino nulo")
                        return
                    }

                    pdfDocument = createPosterPdfDocument(qrBitmap, title, subtext)
                    try {
                        FileOutputStream(destination.fileDescriptor).use { out ->
                            pdfDocument?.writeTo(out)
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    } finally {
                        pdfDocument?.close()
                        pdfDocument = null
                    }
                }
            }

            printManager.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                    .build()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al iniciar impresión: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Sends a Sticker Sheet (Grid of multiple stickers per page) to Android PrintManager.
     */
    fun printStickerSheet(context: Context, qrBitmap: Bitmap, title: String, count: Int = 8) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Servicio de impresión no disponible", Toast.LENGTH_SHORT).show()
                return
            }

            val jobName = "QR_Stickers_${System.currentTimeMillis()}"
            val printAdapter = object : PrintDocumentAdapter() {
                private var pdfDocument: PdfDocument? = null

                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val pdi = PrintDocumentInfo.Builder(jobName)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback?.onLayoutFinished(pdi, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    if (destination == null) {
                        callback?.onWriteFailed("Destino nulo")
                        return
                    }

                    pdfDocument = createStickerSheetPdfDocument(qrBitmap, title, count)
                    try {
                        FileOutputStream(destination.fileDescriptor).use { out ->
                            pdfDocument?.writeTo(out)
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    } finally {
                        pdfDocument?.close()
                        pdfDocument = null
                    }
                }
            }

            printManager.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                    .build()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al iniciar impresión de pegatinas", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Creates an A4 PDF document for a single poster.
     */
    fun createPosterPdfDocument(qrBitmap: Bitmap, title: String, subtext: String): PdfDocument {
        val pdfDocument = PdfDocument()
        // Standard A4 at 72dpi: 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        canvas.drawColor(Color.WHITE)

        // Outer decorative border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val innerMargin = 30f
        canvas.drawRoundRect(RectF(innerMargin, innerMargin, pageWidth - innerMargin, pageHeight - innerMargin), 20f, 20f, borderPaint)

        // Header Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val cleanTitle = if (title.isBlank()) "ESCANÉAME" else title.uppercase()
        canvas.drawText(cleanTitle, pageWidth / 2f, 130f, titlePaint)

        // Sub-header line
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3B82F6")
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(pageWidth / 2f - 60f, 155f, pageWidth / 2f + 60f, 155f, linePaint)

        // Draw QR Code Card
        val qrCardSize = 360f
        val qrLeft = (pageWidth - qrCardSize) / 2f
        val qrTop = 220f
        val qrCardRect = RectF(qrLeft, qrTop, qrLeft + qrCardSize, qrTop + qrCardSize)

        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(qrCardRect, 24f, 24f, cardBgPaint)
        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(qrCardRect, 24f, 24f, cardBorderPaint)

        // Draw the QR bitmap centered inside the card
        val qrInnerPad = 24f
        if (!qrBitmap.isRecycled && qrBitmap.width > 0 && qrBitmap.height > 0) {
            val srcRect = Rect(0, 0, qrBitmap.width, qrBitmap.height)
            val destRect = RectF(qrLeft + qrInnerPad, qrTop + qrInnerPad, qrLeft + qrCardSize - qrInnerPad, qrTop + qrCardSize - qrInnerPad)
            canvas.drawBitmap(qrBitmap, srcRect, destRect, null)
        }

        // Instructions Footer
        val subtextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val cleanSubtext = if (subtext.isBlank()) "Apunta con la cámara de tu móvil para acceder" else subtext
        canvas.drawText(cleanSubtext, pageWidth / 2f, 650f, subtextPaint)

        val smallHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Compatible con Android & iOS • Sin necesidad de descargar apps adicionales", pageWidth / 2f, 680f, smallHintPaint)

        pdfDocument.finishPage(page)
        return pdfDocument
    }

    /**
     * Creates an A4 PDF document with a grid of QR stickers (e.g. 6, 8 or 12 per page) with cut lines.
     */
    fun createStickerSheetPdfDocument(qrBitmap: Bitmap, title: String, count: Int = 8): PdfDocument {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(Color.WHITE)

        // Header
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("HOJA DE PEGATINAS / ADHESIVOS QR (LISTO PARA IMPRIMIR Y RECORTAR)", pageWidth / 2f, 40f, headerPaint)

        val cols = if (count <= 6) 2 else if (count <= 9) 3 else 3
        val rows = if (count <= 6) 3 else if (count <= 9) 3 else 4
        val totalStickers = (cols * rows).coerceAtMost(12)

        val marginX = 40f
        val startY = 70f
        val availableW = pageWidth - (marginX * 2)
        val availableH = pageHeight - startY - 40f

        val cellW = availableW / cols
        val cellH = availableH / rows
        val stickerPad = 10f

        val cutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
        }

        val stickerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val cleanTitle = if (title.isBlank()) "ESCANÉAME" else title.uppercase()

        for (i in 0 until totalStickers) {
            val col = i % cols
            val row = i / cols

            val x = marginX + (col * cellW)
            val y = startY + (row * cellH)

            val boxRect = RectF(x + stickerPad, y + stickerPad, x + cellW - stickerPad, y + cellH - stickerPad)
            canvas.drawRoundRect(boxRect, 8f, 8f, cutPaint)

            // Draw mini title
            canvas.drawText(cleanTitle, boxRect.centerX(), boxRect.top + 16f, stickerTitlePaint)

            // Draw QR
            val qrBoxSize = Math.min(boxRect.width() - 20f, boxRect.height() - 32f)
            val qrLeft = boxRect.centerX() - (qrBoxSize / 2f)
            val qrTop = boxRect.top + 22f

            if (!qrBitmap.isRecycled && qrBitmap.width > 0 && qrBitmap.height > 0) {
                val srcRect = Rect(0, 0, qrBitmap.width, qrBitmap.height)
                val destRect = RectF(qrLeft, qrTop, qrLeft + qrBoxSize, qrTop + qrBoxSize)
                canvas.drawBitmap(qrBitmap, srcRect, destRect, null)
            }
        }

        pdfDocument.finishPage(page)
        return pdfDocument
    }

    /**
     * Saves PDF to cache and returns shareable URI.
     */
    fun savePdfAndGetUri(context: Context, pdfDocument: PdfDocument, filename: String): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "pdf_exports").apply { mkdirs() }
            val file = File(cacheDir, filename)
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                pdfDocument.close()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Saves SVG string to cache and returns shareable URI.
     */
    fun saveSvgAndGetUri(context: Context, svgContent: String, filename: String): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "svg_exports").apply { mkdirs() }
            val file = File(cacheDir, filename)
            file.writeText(svgContent, Charsets.UTF_8)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Shares a file URI with standard Android share sheet.
     */
    fun shareUri(context: Context, uri: Uri, mimeType: String, chooserTitle: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No hay aplicación disponible para compartir", Toast.LENGTH_SHORT).show()
        }
    }
}
