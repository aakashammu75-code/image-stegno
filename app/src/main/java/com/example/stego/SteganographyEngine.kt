package com.example.stego

import android.graphics.Bitmap

object SteganographyEngine {
    // Hidden header key: 'S', 'T', 'G'
    private val MAGIC_BYTES = byteArrayOf('S'.toByte(), 'T'.toByte(), 'G'.toByte())
    private const val HEADER_SIZE = 8 // magic(3) + isEncrypted(1) + payloadSize(4)

    /**
     * Embeds a byte payload inside the Least Significant Bits of the R, G, B channels of a Bitmap.
     * Generates a lossless ARGB_8888 bitmap.
     */
    fun hideData(sourceBitmap: Bitmap, payload: ByteArray, isEncrypted: Boolean): Bitmap {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val totalPixels = width * height

        val totalBytesToEmbed = HEADER_SIZE + payload.size
        val totalBitsToEmbed = totalBytesToEmbed * 8

        // Each pixel can store 3 bits (R, G, B LSB)
        val maxAvailableBits = totalPixels * 3
        if (totalBitsToEmbed > maxAvailableBits) {
            throw IllegalArgumentException("Selected image is too small to hide this secret. Need at least $totalBitsToEmbed bits, but image only provides $maxAvailableBits bits.")
        }

        // Construct full package: Magic Bytes + IsEncrypted + Payload Size + Payload
        val fullData = ByteArray(totalBytesToEmbed)
        System.arraycopy(MAGIC_BYTES, 0, fullData, 0, MAGIC_BYTES.size)
        fullData[3] = if (isEncrypted) 1.toByte() else 0.toByte()

        val pSize = payload.size
        fullData[4] = ((pSize shr 24) and 0xFF).toByte()
        fullData[5] = ((pSize shr 16) and 0xFF).toByte()
        fullData[6] = ((pSize shr 8) and 0xFF).toByte()
        fullData[7] = (pSize and 0xFF).toByte()

        System.arraycopy(payload, 0, fullData, HEADER_SIZE, pSize)

        // Create mutable ARGB_8888 bitmap for bit modification
        val workingBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(totalPixels)
        workingBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var bitIndex = 0
        val totalBits = totalBitsToEmbed

        for (i in 0 until totalPixels) {
            if (bitIndex >= totalBits) break

            val pixel = pixels[i]
            val a = (pixel shr 24) and 0xFF
            var r = (pixel shr 16) and 0xFF
            var g = (pixel shr 8) and 0xFF
            var b = pixel and 0xFF

            // Red channel LSB
            if (bitIndex < totalBits) {
                val bit = getBit(fullData, bitIndex)
                r = (r and 0xFE) or bit
                bitIndex++
            }

            // Green channel LSB
            if (bitIndex < totalBits) {
                val bit = getBit(fullData, bitIndex)
                g = (g and 0xFE) or bit
                bitIndex++
            }

            // Blue channel LSB
            if (bitIndex < totalBits) {
                val bit = getBit(fullData, bitIndex)
                b = (b and 0xFE) or bit
                bitIndex++
            }

            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        workingBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return workingBitmap
    }

    /**
     * Extracts a hidden package from a Bitmap's LSB.
     * Returns null if no valid magic header is found.
     */
    fun extractData(sourceBitmap: Bitmap): ExtractedResult? {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val totalPixels = width * height

        val pixels = IntArray(totalPixels)
        sourceBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Read header first: 8 bytes = 64 bits
        val headerBits = HEADER_SIZE * 8
        if (totalPixels * 3 < headerBits) return null

        val headerBytes = ByteArray(HEADER_SIZE)
        var bitIndex = 0

        for (i in 0 until totalPixels) {
            if (bitIndex >= headerBits) break

            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            if (bitIndex < headerBits) {
                setBit(headerBytes, bitIndex, r and 1)
                bitIndex++
            }
            if (bitIndex < headerBits) {
                setBit(headerBytes, bitIndex, g and 1)
                bitIndex++
            }
            if (bitIndex < headerBits) {
                setBit(headerBytes, bitIndex, b and 1)
                bitIndex++
            }
        }

        // Verify magic bytes: 'S', 'T', 'G'
        if (headerBytes[0] != MAGIC_BYTES[0] || headerBytes[1] != MAGIC_BYTES[1] || headerBytes[2] != MAGIC_BYTES[2]) {
            return null
        }

        val isEncrypted = headerBytes[3].toInt() == 1
        val payloadSize = ((headerBytes[4].toInt() and 0xFF) shl 24) or
                ((headerBytes[5].toInt() and 0xFF) shl 16) or
                ((headerBytes[6].toInt() and 0xFF) shl 8) or
                (headerBytes[7].toInt() and 0xFF)

        // Validate size bounds
        if (payloadSize < 0 || payloadSize > (totalPixels * 3 - headerBits) / 8) {
            return null
        }

        // Extract complete payload
        val totalBytesToRead = HEADER_SIZE + payloadSize
        val totalBitsToRead = totalBytesToRead * 8
        val fullBytes = ByteArray(totalBytesToRead)

        bitIndex = 0
        for (i in 0 until totalPixels) {
            if (bitIndex >= totalBitsToRead) break

            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            if (bitIndex < totalBitsToRead) {
                setBit(fullBytes, bitIndex, r and 1)
                bitIndex++
            }
            if (bitIndex < totalBitsToRead) {
                setBit(fullBytes, bitIndex, g and 1)
                bitIndex++
            }
            if (bitIndex < totalBitsToRead) {
                setBit(fullBytes, bitIndex, b and 1)
                bitIndex++
            }
        }

        val payload = ByteArray(payloadSize)
        System.arraycopy(fullBytes, HEADER_SIZE, payload, 0, payloadSize)

        return ExtractedResult(payload = payload, isEncrypted = isEncrypted)
    }

    private fun getBit(bytes: ByteArray, bitIndex: Int): Int {
        val byteIndex = bitIndex / 8
        val offset = bitIndex % 8
        val b = bytes[byteIndex].toInt()
        return (b shr (7 - offset)) and 1
    }

    private fun setBit(bytes: ByteArray, bitIndex: Int, value: Int) {
        val byteIndex = bitIndex / 8
        val offset = bitIndex % 8
        val mask = (1 shl (7 - offset)).inv()
        val currentByte = bytes[byteIndex].toInt()
        bytes[byteIndex] = ((currentByte and mask) or ((value and 1) shl (7 - offset))).toByte()
    }
}

data class ExtractedResult(
    val payload: ByteArray,
    val isEncrypted: Boolean
)
