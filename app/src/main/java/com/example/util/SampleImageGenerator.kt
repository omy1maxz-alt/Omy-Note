package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class SampleImagePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val previewEmoji: String
)

object SampleImageGenerator {

    val PRESETS = listOf(
        SampleImagePreset("sunset_lake", "Alpine Sunset Lake", "Nature & Landscape", "🌄"),
        SampleImagePreset("cozy_desk", "Cozy Workspace Desk", "Productivity & Setup", "☕"),
        SampleImagePreset("idea_mindmap", "Creative Mind Map", "Brainstorm & Ideas", "💡"),
        SampleImagePreset("vintage_film", "Vintage Polaroid Frame", "Retro & Memories", "📸"),
        SampleImagePreset("stats_chart", "Modern Analytics Card", "Data & Architecture", "📊")
    )

    suspend fun generateAndSaveSampleImage(context: Context, presetId: String): String = withContext(Dispatchers.IO) {
        val width = 800
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        when (presetId) {
            "sunset_lake" -> drawSunsetLake(canvas, width, height)
            "cozy_desk" -> drawCozyDesk(canvas, width, height)
            "idea_mindmap" -> drawIdeaMindmap(canvas, width, height)
            "vintage_film" -> drawVintageFilm(canvas, width, height)
            "stats_chart" -> drawStatsChart(canvas, width, height)
            else -> drawSunsetLake(canvas, width, height)
        }

        // Save to internal storage
        val imagesDir = File(context.filesDir, "note_images").apply { mkdirs() }
        val filename = "sample_${presetId}_${UUID.randomUUID().toString().take(8)}.png"
        val targetFile = File(imagesDir, filename)

        FileOutputStream(targetFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
            fos.flush()
        }
        bitmap.recycle()

        "note_images/$filename"
    }

    private fun drawSunsetLake(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Sky gradient (deep magenta to orange to gold)
        paint.shader = LinearGradient(
            0f, 0f, 0f, h * 0.65f,
            intArrayOf(Color.parseColor("#1B1A47"), Color.parseColor("#7B2D6C"), Color.parseColor("#D45248"), Color.parseColor("#FCA048")),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Glowing Sun
        paint.color = Color.parseColor("#FFF3C4")
        paint.setShadowLayer(40f, 0f, 0f, Color.parseColor("#FFA040"))
        canvas.drawCircle(w * 0.5f, h * 0.38f, 55f, paint)
        paint.clearShadowLayer()

        // Distant Mountains
        val mtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4A235A")
        }
        val path1 = Path().apply {
            moveTo(0f, h * 0.55f)
            lineTo(w * 0.25f, h * 0.32f)
            lineTo(w * 0.48f, h * 0.48f)
            lineTo(w * 0.72f, h * 0.28f)
            lineTo(w.toFloat(), h * 0.55f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(path1, mtnPaint)

        // Closer Mountain Layer
        mtnPaint.color = Color.parseColor("#261435")
        val path2 = Path().apply {
            moveTo(0f, h * 0.62f)
            lineTo(w * 0.18f, h * 0.45f)
            lineTo(w * 0.38f, h * 0.58f)
            lineTo(w * 0.62f, h * 0.42f)
            lineTo(w * 0.88f, h * 0.60f)
            lineTo(w.toFloat(), h * 0.52f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(path2, mtnPaint)

        // Calm Lake Water Gradient
        paint.shader = LinearGradient(
            0f, h * 0.62f, 0f, h.toFloat(),
            intArrayOf(Color.parseColor("#1F1C38"), Color.parseColor("#121124")),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, h * 0.62f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Sun reflection in water
        paint.color = Color.parseColor("#40FFA040")
        for (i in 0..12) {
            val y = h * 0.64f + (i * 14f)
            val lineW = (60f + (i * 18f)) * (1f - (i * 0.04f))
            canvas.drawRoundRect(w * 0.5f - lineW / 2, y, w * 0.5f + lineW / 2, y + 4f, 2f, 2f, paint)
        }

        // Pine trees silhouette along foreground edge
        val treePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0C0A14")
        }
        drawPineTree(canvas, treePaint, w * 0.08f, h * 0.62f, 90f)
        drawPineTree(canvas, treePaint, w * 0.15f, h * 0.64f, 75f)
        drawPineTree(canvas, treePaint, w * 0.85f, h * 0.63f, 85f)
        drawPineTree(canvas, treePaint, w * 0.92f, h * 0.65f, 70f)
    }

    private fun drawPineTree(canvas: Canvas, paint: Paint, x: Float, baseY: Float, height: Float) {
        val path = Path()
        path.moveTo(x, baseY - height)
        path.lineTo(x - height * 0.25f, baseY - height * 0.4f)
        path.lineTo(x - height * 0.15f, baseY - height * 0.4f)
        path.lineTo(x - height * 0.35f, baseY)
        path.lineTo(x + height * 0.35f, baseY)
        path.lineTo(x + height * 0.15f, baseY - height * 0.4f)
        path.lineTo(x + height * 0.25f, baseY - height * 0.4f)
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawCozyDesk(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Warm wall background
        paint.shader = LinearGradient(
            0f, 0f, 0f, h * 0.6f,
            intArrayOf(Color.parseColor("#2C3E50"), Color.parseColor("#34495E")),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h * 0.6f, paint)

        // Warm Wooden Desk Surface
        paint.shader = LinearGradient(
            0f, h * 0.6f, 0f, h.toFloat(),
            intArrayOf(Color.parseColor("#D35400"), Color.parseColor("#A04000")),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, h * 0.6f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Laptop base & screen
        paint.color = Color.parseColor("#BDC3C7")
        val screenRect = RectF(w * 0.3f, h * 0.28f, w * 0.7f, h * 0.65f)
        canvas.drawRoundRect(screenRect, 12f, 12f, paint)

        // Glowing Screen
        paint.color = Color.parseColor("#1C2833")
        val innerScreen = RectF(w * 0.32f, h * 0.30f, w * 0.68f, h * 0.60f)
        canvas.drawRoundRect(innerScreen, 8f, 8f, paint)

        // Code lines on screen
        paint.color = Color.parseColor("#2ECC71")
        canvas.drawRoundRect(w * 0.35f, h * 0.35f, w * 0.50f, h * 0.37f, 3f, 3f, paint)
        paint.color = Color.parseColor("#3498DB")
        canvas.drawRoundRect(w * 0.38f, h * 0.40f, w * 0.62f, h * 0.42f, 3f, 3f, paint)
        paint.color = Color.parseColor("#E67E22")
        canvas.drawRoundRect(w * 0.38f, h * 0.45f, w * 0.58f, h * 0.47f, 3f, 3f, paint)
        paint.color = Color.parseColor("#F1C40F")
        canvas.drawRoundRect(w * 0.35f, h * 0.50f, w * 0.48f, h * 0.52f, 3f, 3f, paint)

        // Laptop keyboard base
        paint.color = Color.parseColor("#7F8C8D")
        val kbPath = Path().apply {
            moveTo(w * 0.22f, h * 0.72f)
            lineTo(w * 0.78f, h * 0.72f)
            lineTo(w * 0.70f, h * 0.65f)
            lineTo(w * 0.30f, h * 0.65f)
            close()
        }
        canvas.drawPath(kbPath, paint)

        // Coffee Mug with steam
        paint.color = Color.parseColor("#E74C3C")
        canvas.drawRoundRect(w * 0.12f, h * 0.62f, w * 0.22f, h * 0.78f, 10f, 10f, paint)
        paint.color = Color.parseColor("#C0392B")
        canvas.drawOval(w * 0.13f, h * 0.61f, w * 0.21f, h * 0.65f, paint)

        // Notepad with Pen
        paint.color = Color.parseColor("#ECF0F1")
        val padRect = RectF(w * 0.75f, h * 0.62f, w * 0.92f, h * 0.82f)
        canvas.drawRoundRect(padRect, 6f, 6f, paint)
        paint.color = Color.parseColor("#BDC3C7")
        for (i in 0..4) {
            val y = h * 0.66f + (i * 12f)
            canvas.drawLine(w * 0.78f, y, w * 0.89f, y, paint)
        }
    }

    private fun drawIdeaMindmap(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Dark modern background
        paint.shader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            intArrayOf(Color.parseColor("#0F2027"), Color.parseColor("#203A43"), Color.parseColor("#2C5364")),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Connector lines
        paint.color = Color.parseColor("#40FFFFFF")
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        val cx = w * 0.5f
        val cy = h * 0.5f

        val nodes = listOf(
            Pair(w * 0.22f, h * 0.30f),
            Pair(w * 0.78f, h * 0.28f),
            Pair(w * 0.20f, h * 0.72f),
            Pair(w * 0.80f, h * 0.70f),
            Pair(w * 0.50f, h * 0.18f)
        )

        for (node in nodes) {
            canvas.drawLine(cx, cy, node.first, node.second, paint)
        }

        // Center Big Node
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#9B59B6")
        canvas.drawCircle(cx, cy, 55f, paint)
        paint.color = Color.WHITE
        paint.textSize = 28f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("IDEA", cx, cy + 10f, paint)

        // Satellite Nodes
        val colors = intArrayOf(
            Color.parseColor("#3498DB"),
            Color.parseColor("#2ECC71"),
            Color.parseColor("#E67E22"),
            Color.parseColor("#E74C3C"),
            Color.parseColor("#F1C40F")
        )
        val labels = listOf("UI/UX", "CODE", "DATA", "LAUNCH", "AI")

        nodes.forEachIndexed { idx, node ->
            paint.color = colors[idx % colors.size]
            canvas.drawCircle(node.first, node.second, 38f, paint)
            paint.color = Color.WHITE
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText(labels[idx % labels.size], node.first, node.second + 5f, paint)
        }
    }

    private fun drawVintageFilm(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Dark Wood background
        paint.color = Color.parseColor("#1A120B")
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        // Polaroid Frame
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FAF8F5")
            setShadowLayer(25f, 0f, 10f, Color.parseColor("#80000000"))
        }
        val pRect = RectF(w * 0.22f, h * 0.10f, w * 0.78f, h * 0.90f)
        canvas.drawRoundRect(pRect, 10f, 10f, framePaint)

        // Photo inside frame
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                w * 0.26f, h * 0.15f, w * 0.74f, h * 0.72f,
                intArrayOf(Color.parseColor("#E67E22"), Color.parseColor("#F39C12"), Color.parseColor("#D35400")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        val photoRect = RectF(w * 0.26f, h * 0.15f, w * 0.74f, h * 0.72f)
        canvas.drawRoundRect(photoRect, 6f, 6f, photoPaint)

        // Polaroid Caption
        paint.color = Color.parseColor("#5C4033")
        paint.textSize = 22f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        canvas.drawText("Notes & Memories ✨", w * 0.5f, h * 0.83f, paint)
    }

    private fun drawStatsChart(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Card Container Gradient
        paint.shader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            intArrayOf(Color.parseColor("#111827"), Color.parseColor("#1F2937")),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Chart Bar Graph
        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bars = listOf(0.35f, 0.55f, 0.85f, 0.65f, 0.95f, 0.75f, 0.90f)
        val barWidth = 45f
        val startX = w * 0.15f
        val gap = (w * 0.7f - (bars.size * barWidth)) / (bars.size - 1)
        val baseline = h * 0.75f

        bars.forEachIndexed { index, ratio ->
            val x = startX + index * (barWidth + gap)
            val barHeight = ratio * (h * 0.5f)
            barPaint.shader = LinearGradient(
                x, baseline - barHeight, x, baseline,
                intArrayOf(Color.parseColor("#6366F1"), Color.parseColor("#8B5CF6")),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(x, baseline - barHeight, x + barWidth, baseline, 8f, 8f, barPaint)
        }

        // Header Text
        paint.color = Color.WHITE
        paint.textSize = 26f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Weekly Notes Growth", w * 0.12f, h * 0.18f, paint)

        paint.color = Color.parseColor("#10B981")
        paint.textSize = 18f
        canvas.drawText("+48% this week 🚀", w * 0.65f, h * 0.18f, paint)
    }
}
