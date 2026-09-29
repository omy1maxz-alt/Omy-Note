package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.BlockType
import com.example.data.model.Note
import java.io.File
import java.io.OutputStream

object PdfExporter {
    private const val PAGE_WIDTH = 595 // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

    fun exportNoteToPdf(
        context: Context,
        note: Note,
        categoryName: String?,
        outputStream: OutputStream
    ): Result<Unit> {
        val document = PdfDocument()
        try {
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val metaPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.rgb(30, 30, 30)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val bulletPaint = Paint().apply {
                color = Color.rgb(0, 121, 107)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            var currentY = MARGIN + 25f

            fun checkNewPage(neededHeight: Float) {
                if (currentY + neededHeight > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = MARGIN + 20f
                }
            }

            // Draw Title
            val titleText = note.title.ifBlank { "Untitled Note" }
            canvas.drawText(titleText, MARGIN, currentY, titlePaint)
            currentY += 28f

            // Draw Meta Row (Category + Date)
            val metaText = buildString {
                if (!categoryName.isNullOrBlank()) {
                    append("Category: $categoryName • ")
                }
                append(DateFormats.formatNoteTimestamp(note.updatedAt))
            }
            canvas.drawText(metaText, MARGIN, currentY, metaPaint)
            currentY += 16f

            // Divider Line
            canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
            currentY += 24f

            // Draw Note Blocks
            for (block in note.blocks) {
                when (block.type) {
                    BlockType.TEXT -> {
                        val lines = breakTextIntoLines(block.content, CONTENT_WIDTH, bodyPaint)
                        for (line in lines) {
                            checkNewPage(18f)
                            canvas.drawText(line, MARGIN, currentY, bodyPaint)
                            currentY += 18f
                        }
                        currentY += 8f
                    }
                    BlockType.CHECKLIST -> {
                        checkNewPage(22f)
                        val checkSymbol = if (block.checked) "☑ " else "☐ "
                        val checkPaint = if (block.checked) metaPaint else bodyPaint
                        canvas.drawText(checkSymbol, MARGIN, currentY, bulletPaint)

                        val indent = 22f
                        val lines = breakTextIntoLines(block.content, CONTENT_WIDTH - indent, checkPaint)
                        if (lines.isNotEmpty()) {
                            canvas.drawText(lines.first(), MARGIN + indent, currentY, checkPaint)
                            currentY += 18f
                            for (i in 1 until lines.size) {
                                checkNewPage(18f)
                                canvas.drawText(lines[i], MARGIN + indent, currentY, checkPaint)
                                currentY += 18f
                            }
                        } else {
                            currentY += 18f
                        }
                        currentY += 4f
                    }
                    BlockType.BULLET -> {
                        checkNewPage(20f)
                        canvas.drawText("• ", MARGIN + 5f, currentY, bulletPaint)
                        val indent = 20f
                        val lines = breakTextIntoLines(block.content, CONTENT_WIDTH - indent, bodyPaint)
                        if (lines.isNotEmpty()) {
                            canvas.drawText(lines.first(), MARGIN + indent, currentY, bodyPaint)
                            currentY += 18f
                            for (i in 1 until lines.size) {
                                checkNewPage(18f)
                                canvas.drawText(lines[i], MARGIN + indent, currentY, bodyPaint)
                                currentY += 18f
                            }
                        } else {
                            currentY += 18f
                        }
                        currentY += 4f
                    }
                    BlockType.IMAGE -> {
                        if (!block.imagePath.isNullOrBlank()) {
                            val mediaFile = File(context.filesDir, block.imagePath)
                            val bitmap = BitmapLoader.loadScaledBitmap(mediaFile, 500, 300)
                            if (bitmap != null) {
                                val aspect = bitmap.height.toFloat() / bitmap.width.toFloat()
                                val renderWidth = (PAGE_WIDTH - 2 * MARGIN).coerceAtMost(400f)
                                val renderHeight = renderWidth * aspect

                                checkNewPage(renderHeight + 15f)
                                val rect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
                                val dst = android.graphics.RectF(MARGIN, currentY, MARGIN + renderWidth, currentY + renderHeight)
                                canvas.drawBitmap(bitmap, rect, dst, null)
                                currentY += renderHeight + 15f
                                bitmap.recycle()
                            }
                        }
                    }
                    BlockType.HANDWRITING -> {
                        // Indicate handwritten doodle block
                        checkNewPage(24f)
                        canvas.drawText("[Handwritten Note / Doodle]", MARGIN, currentY, metaPaint)
                        currentY += 24f
                    }
                }
            }

            document.finishPage(page)
            document.writeTo(outputStream)
            outputStream.flush()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            try {
                document.close()
            } catch (ignored: Exception) {}
        }
    }

    private fun breakTextIntoLines(text: String, maxWidth: Float, paint: Paint): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            if (paragraph.isEmpty()) {
                result.add("")
                continue
            }

            val words = paragraph.split(" ")
            var currentLine = StringBuilder()

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val measure = paint.measureText(testLine)

                if (measure <= maxWidth) {
                    currentLine = StringBuilder(testLine)
                } else {
                    if (currentLine.isNotEmpty()) {
                        result.add(currentLine.toString())
                    }
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) {
                result.add(currentLine.toString())
            }
        }
        return result
    }
}
