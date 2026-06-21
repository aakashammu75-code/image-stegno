package com.example.stego

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object StegoImageHelper {
    /**
     * Loads a Bitmap from an Android Uri safely.
     */
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
                inScaled = false // Crucial to prevent system standard screen density scaling!
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Detects loaded image file format by reading direct signature magic bytes.
     */
    fun detectImageFormat(context: Context, uri: Uri): String {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val header = ByteArray(8)
                val count = inputStream.read(header)
                inputStream.close()
                if (count >= 2) {
                    // Check BMP: 'B', 'M'
                    if (header[0] == 0x42.toByte() && header[1] == 0x4D.toByte()) {
                        return "BMP"
                    }
                    // Check JPEG: FF D8
                    if (header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte()) {
                        return "JPEG"
                    }
                    // Check PNG: 89 50 4E 47 0D 0A 1A 0A
                    if (count >= 8 &&
                        header[0] == 0x89.toByte() && header[1] == 0x50.toByte() &&
                        header[2] == 0x4E.toByte() && header[3] == 0x47.toByte() &&
                        header[4] == 0x0D.toByte() && header[5] == 0x0A.toByte() &&
                        header[6] == 0x1A.toByte() && header[7] == 0x0A.toByte()) {
                        return "PNG"
                    }
                }
            }
            // Fallback content resolver check
            val mimeType = context.contentResolver.getType(uri)
            if (mimeType != null) {
                if (mimeType.contains("png", ignoreCase = true)) return "PNG"
                if (mimeType.contains("jpeg", ignoreCase = true) || mimeType.contains("jpg", ignoreCase = true)) return "JPEG"
                if (mimeType.contains("bmp", ignoreCase = true) || mimeType.contains("x-ms-bmp", ignoreCase = true)) return "BMP"
            }
            "UNKNOWN"
        } catch (e: Exception) {
            e.printStackTrace()
            "UNKNOWN"
        }
    }

    /**
     * Encodes a Bitmap as a lossless standard 24-bit Bitmap (BMP) file format.
     */
    fun saveAsBmp(bitmap: Bitmap, outputStream: OutputStream): Boolean {
        return try {
            val width = bitmap.width
            val height = bitmap.height

            val rowSize = ((24 * width + 31) / 32) * 4
            val pixelDataSize = rowSize * height
            val fileSize = 54 + pixelDataSize

            val header = ByteArray(54)
            // BM Magic signatures
            header[0] = 'B'.toByte()
            header[1] = 'M'.toByte()

            // File Size
            header[2] = (fileSize and 0xFF).toByte()
            header[3] = ((fileSize shr 8) and 0xFF).toByte()
            header[4] = ((fileSize shr 16) and 0xFF).toByte()
            header[5] = ((fileSize shr 24) and 0xFF).toByte()

            // Bytes 6-9 are reserved 0

            // Pixel offset index (54)
            header[10] = 54.toByte()
            header[11] = 0.toByte()
            header[12] = 0.toByte()
            header[13] = 0.toByte()

            // DIB Header Info Size (40)
            header[14] = 40.toByte()
            header[15] = 0.toByte()
            header[16] = 0.toByte()
            header[17] = 0.toByte()

            // Image Width
            header[18] = (width and 0xFF).toByte()
            header[19] = ((width shr 8) and 0xFF).toByte()
            header[20] = ((width shr 16) and 0xFF).toByte()
            header[21] = ((width shr 24) and 0xFF).toByte()

            // Image Height (Positive for standard bottom-up order)
            header[22] = (height and 0xFF).toByte()
            header[23] = ((height shr 8) and 0xFF).toByte()
            header[24] = ((height shr 16) and 0xFF).toByte()
            header[25] = ((height shr 24) and 0xFF).toByte()

            // Colour Planes (1)
            header[26] = 1.toByte()
            header[27] = 0.toByte()

            // Bits per Pixel (24-bit)
            header[28] = 24.toByte()
            header[29] = 0.toByte()

            // Compression BI_RGB (0)
            header[30] = 0.toByte()
            header[31] = 0.toByte()
            header[32] = 0.toByte()
            header[33] = 0.toByte()

            // Image pixel size
            header[34] = (pixelDataSize and 0xFF).toByte()
            header[35] = ((pixelDataSize shr 8) and 0xFF).toByte()
            header[36] = ((pixelDataSize shr 16) and 0xFF).toByte()
            header[37] = ((pixelDataSize shr 24) and 0xFF).toByte()

            // Horizontal & Vertical density coefficients (Default: 2835 ppm approx 72 DPI)
            header[38] = 0x13.toByte()
            header[39] = 0x0B.toByte()
            header[40] = 0.toByte()
            header[41] = 0.toByte()

            header[42] = 0x13.toByte()
            header[43] = 0x0B.toByte()
            header[44] = 0.toByte()
            header[45] = 0.toByte()

            // Palette + Important colors counts are 0

            outputStream.write(header)

            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val rowBuffer = ByteArray(rowSize)
            // BMP pixels are bottom-up row sequence
            for (y in height - 1 downTo 0) {
                val offset = y * width
                var colByte = 0
                for (x in 0 until width) {
                    val pixel = pixels[offset + x]
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF

                    // Write BGR order
                    rowBuffer[colByte++] = b.toByte()
                    rowBuffer[colByte++] = g.toByte()
                    rowBuffer[colByte++] = r.toByte()
                }
                // Zero-padding down to rowSize
                while (colByte < rowSize) {
                    rowBuffer[colByte++] = 0.toByte()
                }
                outputStream.write(rowBuffer, 0, rowSize)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Saves a stego Bitmap to the preferred format (PNG or BMP).
     * Returns: Private file path.
     */
    fun saveStegoBitmap(context: Context, bitmap: Bitmap, baseName: String, format: String = "PNG"): String? {
        val extension = if (format.uppercase() == "BMP") "bmp" else "png"
        val mimeType = if (format.uppercase() == "BMP") "image/bmp" else "image/png"
        val fileName = "${baseName}_${System.currentTimeMillis()}.$extension"

        // 1. Save internally for local history tracking
        var privateFilePath: String? = null
        try {
            val internalDir = File(context.filesDir, "stego_history_images")
            if (!internalDir.exists()) {
                internalDir.mkdirs()
            }
            val privateFile = File(internalDir, fileName)
            val outPrivate = FileOutputStream(privateFile)
            if (format.uppercase() == "BMP") {
                saveAsBmp(bitmap, outPrivate)
            } else {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outPrivate)
            }
            outPrivate.flush()
            outPrivate.close()
            privateFilePath = privateFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Save to public MediaStore Images directory
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Steganography")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val uri = context.contentResolver.insert(collection, contentValues)
            if (uri != null) {
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    if (format.uppercase() == "BMP") {
                        saveAsBmp(bitmap, outputStream)
                    } else {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    outputStream.flush()
                    outputStream.close()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return privateFilePath
    }

    /**
     * Downloads (copies) the stego image to the public Downloads directory of the device.
     * Returns the absolute path of the downloaded file or a user-friendly descriptive URI string.
     */
    fun downloadImageToPublicDownloads(context: Context, bitmap: Bitmap, format: String): String? {
        val extension = if (format.uppercase() == "BMP") "bmp" else "png"
        val mimeType = if (format.uppercase() == "BMP") "image/bmp" else "image/png"
        val fileName = "Stego_PixelPerfect_${System.currentTimeMillis()}.$extension"

        try {
            // Android 10 (Q, API 29) +
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val uri = resolver.insert(collection, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri).use { outputStream ->
                        if (outputStream != null) {
                            if (format.uppercase() == "BMP") {
                                saveAsBmp(bitmap, outputStream)
                            } else {
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            }
                            outputStream.flush()
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    return "Downloads/$fileName"
                }
            } else {
                // Older Android: Direct Files API with Environment.DIRECTORY_DOWNLOADS
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val publicFile = File(downloadsDir, fileName)
                FileOutputStream(publicFile).use { outputStream ->
                    if (format.uppercase() == "BMP") {
                        saveAsBmp(bitmap, outputStream)
                    } else {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    outputStream.flush()
                }
                return publicFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
