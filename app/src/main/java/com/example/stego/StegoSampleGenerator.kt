package com.example.stego

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object StegoSampleGenerator {
    /**
     * Generates a programmatic abstract bitmap of size W x H for steganography.
     */
    fun generatePreset(type: String, width: Int = 1000, height: Int = 1000): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        when (type) {
            "Cyber Grid" -> {
                // Dark slate background with a cyan grid and glowing circles
                canvas.drawColor(Color.parseColor("#0F172A")) // Slate 900
                
                // Draw digital grids
                paint.color = Color.parseColor("#1E293B") // Slate 800
                paint.strokeWidth = 2f
                val step = 40f
                for (x in 0 until (width / step).toInt()) {
                    canvas.drawLine(x * step, 0f, x * step, height.toFloat(), paint)
                }
                for (y in 0 until (height / step).toInt()) {
                    canvas.drawLine(0f, y * step, width.toFloat(), y * step, paint)
                }

                // Draw cyber nodes
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 4f
                paint.color = Color.parseColor("#06B6D4") // Cyan 500
                canvas.drawCircle(width / 2f, height / 2f, 250f, paint)
                
                paint.color = Color.parseColor("#3B82F6") // Blue 500
                canvas.drawCircle(width / 2f, height / 2f, 150f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#14B8A6") // Teal 500
                canvas.drawCircle(width / 4f, height / 4f, 15f, paint)
                canvas.drawCircle(3 * width / 4f, height / 4f, 15f, paint)
                canvas.drawCircle(width / 4f, 3 * height / 4f, 15f, paint)
                canvas.drawCircle(3 * width / 4f, 3 * height / 4f, 15f, paint)
            }
            "Neon Lattice" -> {
                // Purple and pink cyberpunk visual lattice
                canvas.drawColor(Color.parseColor("#1E1B4B")) // Indigo 950
                
                paint.style = Paint.Style.FILL
                // Diagonal lines
                paint.color = Color.parseColor("#4F46E5") // Indigo 600
                paint.strokeWidth = 3f
                for (i in 0 until width step 60) {
                    canvas.drawLine(i.toFloat(), 0f, (i + 200).toFloat(), height.toFloat(), paint)
                }
                
                paint.color = Color.parseColor("#EC4899") // Pink 500
                canvas.drawCircle(width / 2f, height / 2f, 80f, paint)
                
                paint.color = Color.parseColor("#8B5CF6") // Purple 500
                canvas.drawCircle(width / 2f - 120f, height / 2f + 120f, 40f, paint)
                canvas.drawCircle(width / 2f + 120f, height / 2f - 120f, 40f, paint)
            }
            else -> {
                // Golden Aurora / Cosmic Glow
                canvas.drawColor(Color.parseColor("#18181B")) // Zinc 900
                
                paint.style = Paint.Style.FILL
                // Golden glowing suns and stars
                paint.color = Color.parseColor("#D97706") // Amber 600
                canvas.drawCircle(width / 2f, height / 3f, 180f, paint)
                
                paint.color = Color.parseColor("#F59E0B") // Amber 500
                canvas.drawCircle(width / 2f, height / 3f, 120f, paint)

                paint.color = Color.parseColor("#FBBF24") // Amber 400
                canvas.drawCircle(width / 2f, height / 3f, 60f, paint)

                paint.color = Color.parseColor("#27272A") // Zinc 800
                paint.strokeWidth = 8f
                paint.style = Paint.Style.STROKE
                canvas.drawRect(80f, 80f, width - 80f, height - 80f, paint)
            }
        }
        return bitmap
    }
}
