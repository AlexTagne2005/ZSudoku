package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.Difficulty
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    fun shareVictoryCard(
        context: Context,
        difficulty: Difficulty,
        timeSeconds: Long,
        stars: Int,
        errorCount: Int,
        maxErrors: Int
    ) {
        val mins = timeSeconds / 60
        val secs = timeSeconds % 60
        val timeFormatted = String.format("%02d:%02d", mins, secs)
        val starsStr = "★".repeat(stars) + "☆".repeat(3 - stars)

        val textSummary = "🧩 ZenSudoku Victoire !\n" +
                "• Niveau: ${difficulty.displayName}\n" +
                "• Étoiles: $starsStr ($stars/3)\n" +
                "• Temps: $timeFormatted\n" +
                "• Fautes: $errorCount / $maxErrors\n" +
                "Jouez à ZenSudoku pour entraîner votre esprit dans la sérénité ! ✨"

        // 1. Copy text to Clipboard
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ZenSudoku Victory", textSummary)
            clipboard.setPrimaryClip(clip)
        } catch (_: Exception) {}

        // 2. Generate Image Bitmap
        val bitmap = createVictoryBitmap(
            difficultyName = difficulty.displayName,
            timeStr = timeFormatted,
            starsCount = stars,
            errorCount = errorCount,
            maxErrors = maxErrors
        )

        // 3. Save Bitmap to Cache
        val file = File(context.cacheDir, "sudoku_victory_share.png")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. Trigger Share Intent with FileProvider Uri
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_SUBJECT, "Victoire ZenSudoku !")
                putExtra(Intent.EXTRA_TEXT, textSummary)
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Partager ma victoire ZenSudoku"))
            Toast.makeText(context, "Image & texte copiés dans le presse-papier !", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Fallback text share if chooser fails
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textSummary)
            }
            context.startActivity(Intent.createChooser(textIntent, "Partager texte"))
        }
    }

    private fun createVictoryBitmap(
        difficultyName: String,
        timeStr: String,
        starsCount: Int,
        errorCount: Int,
        maxErrors: Int
    ): Bitmap {
        val size = 900
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient Background (Deep Ocean Teal / Zen Dark Gradient)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, size.toFloat(),
                intArrayOf(Color.parseColor("#0F172A"), Color.parseColor("#1E293B"), Color.parseColor("#0F766E")),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        // Inner Card Box
        val cardPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val cardRect = RectF(60f, 60f, size - 60f, size - 60f)
        canvas.drawRoundRect(cardRect, 40f, 40f, cardPaint)

        // Border Accent
        val borderPaint = Paint().apply {
            color = Color.parseColor("#0D9488")
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 40f, 40f, borderPaint)

        // Text Paint settings
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val starPaint = Paint().apply {
            color = Color.parseColor("#F59E0B")
            textSize = 70f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val statLabelPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val brandPaint = Paint().apply {
            color = Color.parseColor("#2DD4BF")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // Draw Content
        canvas.drawText("🧩 ZenSudoku", size / 2f, 150f, titlePaint)
        canvas.drawText("Victoire • Niveau $difficultyName", size / 2f, 210f, subtitlePaint)

        // Draw Stars
        val starsStr = "★ ".repeat(starsCount) + "☆ ".repeat(3 - starsCount)
        canvas.drawText(starsStr.trim(), size / 2f, 310f, starPaint)

        // Stats Box
        val statsBgPaint = Paint().apply {
            color = Color.parseColor("#334155")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val statsRect = RectF(120f, 380f, size - 120f, 620f)
        canvas.drawRoundRect(statsRect, 24f, 24f, statsBgPaint)

        canvas.drawText("⏱️ Temps: $timeStr", size / 2f, 460f, statLabelPaint)
        canvas.drawText("⚠️ Fautes: $errorCount / $maxErrors", size / 2f, 540f, statLabelPaint)

        // Quote & Footer
        canvas.drawText("« La sérénité commence avec la clarté d'esprit »", size / 2f, 710f, subtitlePaint)
        canvas.drawText("ZenSudoku • Mode Esprit Zen", size / 2f, 780f, brandPaint)

        return bitmap
    }
}
