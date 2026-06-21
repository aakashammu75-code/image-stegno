package com.example.stego

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stego.db.StegoDatabase
import com.example.stego.db.StegoHistory
import com.example.stego.db.StegoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.StandardCharsets

sealed interface EncodeResultState {
    object Idle : EncodeResultState
    object Loading : EncodeResultState
    data class Success(val savedPath: String, val bitmap: Bitmap) : EncodeResultState
    data class Error(val message: String) : EncodeResultState
}

sealed interface DecodeResultState {
    object Idle : DecodeResultState
    object Loading : DecodeResultState
    data class Success(val originalMessage: String, val isEncrypted: Boolean) : DecodeResultState
    object PasswordRequired : DecodeResultState
    data class Error(val message: String) : DecodeResultState
}

class StegoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StegoRepository
    private val webServer: com.example.stego.web.StegoWebServer

    val webServerPort: Int
        get() = webServer.boundPort

    init {
        val database = StegoDatabase.getDatabase(application)
        repository = StegoRepository(database.stegoHistoryDao())
        webServer = com.example.stego.web.StegoWebServer(application, repository, viewModelScope)
        webServer.start()
    }

    override fun onCleared() {
        super.onCleared()
        webServer.stop()
    }

    // Expose local file history of stego actions
    val historyList: StateFlow<List<StegoHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // -------------------------------------------------------------
    // Hide / Encode State
    // -------------------------------------------------------------
    var selectedBitmapForEncode by mutableStateOf<Bitmap?>(null)
        private set
    var selectedBitmapForEncodeFormat by mutableStateOf("PNG")
        private set
    var selectedBitmapForEncodeHasSecret by mutableStateOf<Boolean?>(null)
        private set
    var selectedBitmapForEncodeIsEncrypted by mutableStateOf<Boolean?>(null)
        private set
    var isScanningEncodeImage by mutableStateOf(false)
        private set
    var selectedOutputFormat by mutableStateOf("PNG") // PNG or BMP lossless
        private set
    var secretMessage by mutableStateOf("")
        private set
    var encodePassword by mutableStateOf("")
        private set
    var isEncryptionEnabled by mutableStateOf(true)
        private set
    var encodeState by mutableStateOf<EncodeResultState>(EncodeResultState.Idle)
        private set

    var isDownloading by mutableStateOf(false)
        private set
    var downloadSuccessPath by mutableStateOf<String?>(null)
        private set
    var downloadError by mutableStateOf<String?>(null)
        private set

    // Capacity calculations
    val currentPayloadSizeInBytes: Int
        get() {
            val plainBytesSize = secretMessage.toByteArray(StandardCharsets.UTF_8).size
            return if (isEncryptionEnabled && secretMessage.isNotEmpty()) {
                // SALT_LENGTH_BYTES (16) + IV_LENGTH_BYTES (12) + plainBytesSize + AuthTag (16)
                plainBytesSize + 44
            } else {
                plainBytesSize
            }
        }

    val maxCapacityInBytes: Int
        get() {
            val bitmap = selectedBitmapForEncode ?: return 0
            val totalPixels = bitmap.width * bitmap.height
            // Each pixel stores 3 bits. Header needs 8 bytes (64 bits).
            return (totalPixels * 3 - 64) / 8
        }

    fun setEncodeImage(bitmap: Bitmap?, format: String = "PNG") {
        selectedBitmapForEncode = bitmap
        selectedBitmapForEncodeFormat = format
        // Reset state
        encodeState = EncodeResultState.Idle
        selectedBitmapForEncodeHasSecret = null
        selectedBitmapForEncodeIsEncrypted = null
        isDownloading = false
        downloadSuccessPath = null
        downloadError = null
        
        if (bitmap != null) {
            isScanningEncodeImage = true
            viewModelScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        SteganographyEngine.extractData(bitmap)
                    }
                    if (result != null) {
                        selectedBitmapForEncodeHasSecret = true
                        selectedBitmapForEncodeIsEncrypted = result.isEncrypted
                    } else {
                        selectedBitmapForEncodeHasSecret = false
                        selectedBitmapForEncodeIsEncrypted = null
                    }
                } catch (e: Exception) {
                    selectedBitmapForEncodeHasSecret = false
                    selectedBitmapForEncodeIsEncrypted = null
                } finally {
                    isScanningEncodeImage = false
                }
            }
        }
    }

    fun updateSelectedOutputFormat(format: String) {
        selectedOutputFormat = format.uppercase()
    }

    fun updateSecretMessage(message: String) {
        secretMessage = message
        if (encodeState is EncodeResultState.Success || encodeState is EncodeResultState.Error) {
            encodeState = EncodeResultState.Idle
        }
    }

    fun updateEncodePassword(password: String) {
        encodePassword = password
        if (encodeState is EncodeResultState.Success || encodeState is EncodeResultState.Error) {
            encodeState = EncodeResultState.Idle
        }
    }

    fun toggleEncryption(enabled: Boolean) {
        isEncryptionEnabled = enabled
        if (encodeState is EncodeResultState.Success || encodeState is EncodeResultState.Error) {
            encodeState = EncodeResultState.Idle
        }
    }

    fun hideMessageInImage() {
        val bitmap = selectedBitmapForEncode ?: return
        if (secretMessage.isEmpty()) {
            encodeState = EncodeResultState.Error("Secret message cannot be empty.")
            return
        }

        if (isEncryptionEnabled && encodePassword.isEmpty()) {
            encodeState = EncodeResultState.Error("Password must be supplied for cryptographic security.")
            return
        }

        if (currentPayloadSizeInBytes > maxCapacityInBytes) {
            encodeState = EncodeResultState.Error("Data exceeds selected image storage capacity.")
            return
        }

        encodeState = EncodeResultState.Loading

        viewModelScope.launch {
            try {
                val resultBitmap = withContext(Dispatchers.Default) {
                    val rawPayloadBytes = secretMessage.toByteArray(StandardCharsets.UTF_8)
                    val payloadBytes = if (isEncryptionEnabled) {
                        try {
                            StegoCrypto.encrypt(rawPayloadBytes, encodePassword.toCharArray())
                        } catch (e: Exception) {
                            throw RuntimeException("Encryption Error: ${e.message}")
                        }
                    } else {
                        rawPayloadBytes
                    }
                    SteganographyEngine.hideData(bitmap, payloadBytes, isEncryptionEnabled)
                }

                val savedPath = withContext(Dispatchers.IO) {
                    StegoImageHelper.saveStegoBitmap(getApplication(), resultBitmap, "stego_encoded", selectedOutputFormat)
                }

                if (savedPath != null) {
                    encodeState = EncodeResultState.Success(savedPath, resultBitmap)
                    // Log to history database
                    repository.insert(
                        StegoHistory(
                            actionType = "HIDE",
                            imageName = File(savedPath).name,
                            payloadSize = currentPayloadSizeInBytes,
                            isEncrypted = isEncryptionEnabled,
                            wasSuccessful = true,
                            details = "Message successfully sealed visually."
                        )
                    )
                } else {
                    encodeState = EncodeResultState.Error("Failed to save encoded image.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                encodeState = EncodeResultState.Error(e.message ?: "An unknown encoding error occurred.")
                repository.insert(
                    StegoHistory(
                        actionType = "HIDE",
                        imageName = "Failed Action",
                        payloadSize = 0,
                        isEncrypted = isEncryptionEnabled,
                        wasSuccessful = false,
                        details = e.message ?: "Encoder abort."
                    )
                )
            }
        }
    }

    fun downloadBitmapToDownloadsFolder(bitmap: Bitmap, format: String) {
        val app = getApplication<android.app.Application>()
        isDownloading = true
        downloadSuccessPath = null
        downloadError = null
        viewModelScope.launch {
            val path = withContext(Dispatchers.IO) {
                StegoImageHelper.downloadImageToPublicDownloads(app, bitmap, format)
            }
            if (path != null) {
                downloadSuccessPath = path
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(app, "Downloaded: $path", android.widget.Toast.LENGTH_LONG).show()
                }
            } else {
                downloadError = "Failed to download image."
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(app, "Download failed.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            isDownloading = false
        }
    }

    // -------------------------------------------------------------
    // Extract / Decode State
    // -------------------------------------------------------------
    var selectedBitmapForDecode by mutableStateOf<Bitmap?>(null)
        private set
    var selectedBitmapForDecodeFormat by mutableStateOf("PNG")
        private set
    var decodePassword by mutableStateOf("")
        private set
    var decodeResultState by mutableStateOf<DecodeResultState>(DecodeResultState.Idle)
        private set
    var detectedEncryptedPayload by mutableStateOf<ExtractedResult?>(null)
        private set

    fun setDecodeImage(bitmap: Bitmap?, format: String = "PNG") {
        selectedBitmapForDecode = bitmap
        selectedBitmapForDecodeFormat = format
        decodePassword = ""
        decodeResultState = DecodeResultState.Idle
        detectedEncryptedPayload = null
        
        // Auto-scan steganographic trace as soon as image is loaded
        if (bitmap != null) {
            scanImageForPayload(bitmap)
        }
    }

    fun updateDecodePassword(password: String) {
        decodePassword = password
        if (decodeResultState is DecodeResultState.Error) {
            decodeResultState = DecodeResultState.Idle
        }
    }

    private fun scanImageForPayload(bitmap: Bitmap) {
        decodeResultState = DecodeResultState.Loading
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    SteganographyEngine.extractData(bitmap)
                }

                if (result == null) {
                    decodeResultState = DecodeResultState.Error("No steganography payload signature detected in this image.")
                    detectedEncryptedPayload = null
                } else if (result.isEncrypted) {
                    detectedEncryptedPayload = result
                    decodeResultState = DecodeResultState.PasswordRequired
                } else {
                    detectedEncryptedPayload = null
                    val originalMessage = String(result.payload, StandardCharsets.UTF_8)
                    decodeResultState = DecodeResultState.Success(originalMessage, isEncrypted = false)
                    
                    repository.insert(
                        StegoHistory(
                            actionType = "EXTRACT",
                            imageName = "Unidentified",
                            payloadSize = result.payload.size,
                            isEncrypted = false,
                            wasSuccessful = true,
                            details = "Message successfully retrieved."
                        )
                    )
                }
            } catch (e: Exception) {
                decodeResultState = DecodeResultState.Error("Scan failed: ${e.message}")
            }
        }
    }

    fun decryptAndExtract() {
        val payloadRecord = detectedEncryptedPayload
        if (payloadRecord == null) {
            decodeResultState = DecodeResultState.Error("Extraction target is missing.")
            return
        }

        if (decodePassword.isEmpty()) {
            decodeResultState = DecodeResultState.Error("Cryptographic password is required to decrypt this data.")
            return
        }

        decodeResultState = DecodeResultState.Loading

        viewModelScope.launch {
            try {
                val decryptedBytes = withContext(Dispatchers.Default) {
                    StegoCrypto.decrypt(payloadRecord.payload, decodePassword.toCharArray())
                }

                val decryptedMessage = String(decryptedBytes, StandardCharsets.UTF_8)
                decodeResultState = DecodeResultState.Success(decryptedMessage, isEncrypted = true)

                repository.insert(
                    StegoHistory(
                        actionType = "EXTRACT",
                        imageName = "Encrypted Container",
                        payloadSize = decryptedBytes.size,
                        isEncrypted = true,
                        wasSuccessful = true,
                        details = "Decrypted using cryptographic key."
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                decodeResultState = DecodeResultState.Error("Incorrect password or cipher corruption.")
                
                repository.insert(
                    StegoHistory(
                        actionType = "EXTRACT",
                        imageName = "Encrypted Container",
                        payloadSize = 0,
                        isEncrypted = true,
                        wasSuccessful = false,
                        details = "Decryption attempt failed."
                    )
                )
            }
        }
    }

    /**
     * Loads a photo directly from a local history file path.
     */
    fun loadFromHistoryPath(path: String) {
        viewModelScope.launch {
            try {
                val file = File(path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        val format = if (file.name.endsWith(".bmp", ignoreCase = true)) "BMP" else "PNG"
                        setDecodeImage(bitmap, format)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Deletes a specific history record and cleans up his cached image if exists.
     */
    fun deleteHistoryRecord(history: StegoHistory) {
        viewModelScope.launch {
            repository.deleteById(history.id)
            if (history.actionType == "HIDE") {
                try {
                    val internalDir = File(getApplication<Application>().filesDir, "stego_history_images")
                    val file = File(internalDir, history.imageName)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Clear all recorded history log.
     */
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
            try {
                val internalDir = File(getApplication<Application>().filesDir, "stego_history_images")
                if (internalDir.exists()) {
                    internalDir.deleteRecursively()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
