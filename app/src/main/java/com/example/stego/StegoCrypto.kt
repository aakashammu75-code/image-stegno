package com.example.stego

import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object StegoCrypto {
    private const val ITERATION_COUNT = 1000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12

    /**
     * Encrypts plaintext bytes using AES-256-GCM derived from a password using PBKDF2.
     * Returns: [Salt (16 bytes)] + [IV (12 bytes)] + [CipherText]
     */
    fun encrypt(plainText: ByteArray, password: CharArray): ByteArray {
        // 1. Generate random salt
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)

        // 2. Derive key from password and salt
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")

        // 3. Generate random IV
        val iv = ByteArray(IV_LENGTH_BYTES)
        SecureRandom().nextBytes(iv)

        // 4. Encrypt with AES/GCM/NoPadding
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val cipherText = cipher.doFinal(plainText)

        // 5. Package output: Salt + IV + CipherText
        val result = ByteArray(salt.size + iv.size + cipherText.size)
        System.arraycopy(salt, 0, result, 0, salt.size)
        System.arraycopy(iv, 0, result, salt.size, iv.size)
        System.arraycopy(cipherText, 0, result, salt.size + iv.size, cipherText.size)
        return result
    }

    /**
     * Decrypts ciphertext bytes using AES-256-GCM derived from a password using PBKDF2.
     */
    fun decrypt(encryptedData: ByteArray, password: CharArray): ByteArray {
        if (encryptedData.size < SALT_LENGTH_BYTES + IV_LENGTH_BYTES) {
            throw IllegalArgumentException("Data is too small to be a valid encrypted payload.")
        }

        // 1. Extract Salt, IV, and cipherText
        val salt = ByteArray(SALT_LENGTH_BYTES)
        val iv = ByteArray(IV_LENGTH_BYTES)
        val cipherTextSize = encryptedData.size - SALT_LENGTH_BYTES - IV_LENGTH_BYTES
        val cipherText = ByteArray(cipherTextSize)

        System.arraycopy(encryptedData, 0, salt, 0, SALT_LENGTH_BYTES)
        System.arraycopy(encryptedData, SALT_LENGTH_BYTES, iv, 0, IV_LENGTH_BYTES)
        System.arraycopy(encryptedData, SALT_LENGTH_BYTES + IV_LENGTH_BYTES, cipherText, 0, cipherTextSize)

        // 2. Derive key from password and salt
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")

        // 3. Decrypt with AES/GCM/NoPadding
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        return cipher.doFinal(cipherText)
    }
}
