package com.example.stego.web

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.stego.StegoCrypto
import com.example.stego.SteganographyEngine
import com.example.stego.db.StegoHistory
import com.example.stego.db.StegoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class StegoWebServer(
    private val context: android.content.Context,
    private val repository: StegoRepository,
    private val scope: CoroutineScope
) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val executor: ExecutorService = Executors.newFixedThreadPool(4)
    var boundPort: Int = 8080
        private set

    fun start() {
        // Try starting from port 8080 up to 8090
        var port = 8080
        var startedSuccessfully = false
        while (port < 8090 && !startedSuccessfully) {
            try {
                serverSocket = ServerSocket(port)
                boundPort = port
                isRunning = true
                startedSuccessfully = true
                
                // Start background thread for listening loop
                Thread {
                    while (isRunning) {
                        try {
                            val socket = serverSocket?.accept() ?: break
                            executor.submit {
                                handleClient(socket)
                            }
                        } catch (e: Exception) {
                            if (!isRunning) break
                        }
                    }
                }.start()
                
            } catch (e: Exception) {
                e.printStackTrace()
                port++
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            executor.shutdownNow()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleClient(socket: Socket) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = socket.getInputStream()
            outputStream = socket.getOutputStream()

            // Function to read raw bytes until a line delimiter \n
            fun readLineBytes(ins: InputStream): ByteArray {
                val baos = ByteArrayOutputStream()
                while (true) {
                    val b = ins.read()
                    if (b == -1) break
                    if (b == '\n'.code) {
                        baos.write(b)
                        break
                    }
                    baos.write(b)
                }
                return baos.toByteArray()
            }

            // Read the request/status line
            val firstLineBytes = readLineBytes(inputStream)
            if (firstLineBytes.isEmpty()) return
            val firstLine = String(firstLineBytes, StandardCharsets.UTF_8).trim()
            val parts = firstLine.split(" ")
            if (parts.size < 2) return
            val method = parts[0].uppercase()
            val path = parts[1]

            // Read headers
            var contentLength = 0
            while (true) {
                val lineBytes = readLineBytes(inputStream)
                val line = String(lineBytes, StandardCharsets.UTF_8).trim()
                if (line.isEmpty()) break // End of headers
                
                val headerParts = line.split(":", limit = 2)
                if (headerParts.size == 2) {
                    val name = headerParts[0].trim().lowercase()
                    if (name == "content-length") {
                        contentLength = headerParts[1].trim().toIntOrNull() ?: 0
                    }
                }
            }

            // Read body bytes if Content-Length specified
            val body = if (contentLength > 0) {
                val bodyBytes = ByteArray(contentLength)
                var totalRead = 0
                while (totalRead < contentLength) {
                    val read = inputStream.read(bodyBytes, totalRead, contentLength - totalRead)
                    if (read == -1) break
                    totalRead += read
                }
                String(bodyBytes, StandardCharsets.UTF_8)
            } else {
                ""
            }

            // Handle OPTIONS / preflight request
            if (method == "OPTIONS") {
                sendResponse(outputStream, 204, "text/plain", "")
                return
            }

            when {
                path == "/" && method == "GET" -> {
                    sendResponse(outputStream, 200, "text/html; charset=utf-8", StegoWebAssets.INDEX_HTML)
                }
                path == "/api/encode" && method == "POST" -> {
                    handleEncode(body, outputStream)
                }
                path == "/api/decode" && method == "POST" -> {
                    handleDecode(body, outputStream)
                }
                path == "/api/history" && method == "GET" -> {
                    handleHistory(outputStream)
                }
                path == "/api/history/clear" && method == "POST" -> {
                    handleHistoryClear(outputStream)
                }
                else -> {
                    sendError(outputStream, "Endpoint not found: $path", 404)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                if (outputStream != null) {
                    sendError(outputStream, "Internal Handler Failure: ${e.message}", 500)
                }
            } catch (ex: Exception) {}
        } finally {
            try {
                socket.close()
            } catch (e: Exception) {}
        }
    }

    private fun sendResponse(output: OutputStream, statusCode: Int, contentType: String, body: String) {
        val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
        val responseHeaders = buildString {
            append("HTTP/1.1 $statusCode OK\r\n")
            append("Access-Control-Allow-Origin: *\r\n")
            append("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
            append("Access-Control-Allow-Headers: Content-Type, Authorization\r\n")
            append("Content-Type: $contentType\r\n")
            append("Content-Length: ${bodyBytes.size}\r\n")
            append("Connection: close\r\n")
            append("\r\n")
        }
        output.write(responseHeaders.toByteArray(StandardCharsets.UTF_8))
        output.write(bodyBytes)
        output.flush()
    }

    private fun sendError(output: OutputStream, errorMessage: String, statusCode: Int = 400) {
        val json = JSONObject()
        json.put("success", false)
        json.put("error", errorMessage)
        sendResponse(output, statusCode, "application/json", json.toString())
    }

    private fun base64ToBitmap(b64String: String): Bitmap {
        val cleanB64 = if (b64String.contains(",")) b64String.substringAfter(",") else b64String
        val decodedBytes = Base64.decode(cleanB64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun bitmapToBase64(bitmap: Bitmap, format: String): String {
        val outputStream = ByteArrayOutputStream()
        if (format.equals("BMP", ignoreCase = true)) {
            com.example.stego.StegoImageHelper.saveAsBmp(bitmap, outputStream)
        } else {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        val bytes = outputStream.toByteArray()
        val mime = if (format.equals("BMP", ignoreCase = true)) "image/bmp" else "image/png"
        return "data:$mime;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun handleEncode(body: String, output: OutputStream) {
        if (body.isEmpty()) {
            sendError(output, "Request body empty.")
            return
        }

        try {
            val json = JSONObject(body)
            val imageB64 = json.getString("imageB64")
            val fileName = json.optString("fileName", "undefined_web_canvas.png")
            val message = json.getString("message")
            val password = json.optString("password", "")
            val useEncryption = json.optBoolean("useEncryption", false)
            val format = json.optString("format", "PNG")

            if (message.isEmpty()) {
                sendError(output, "Message payload cannot be empty.")
                return
            }

            if (useEncryption && password.isEmpty()) {
                sendError(output, "Passphrase is required when encryption toggle is set.")
                return
            }

            val sourceBitmap = base64ToBitmap(imageB64)
            val rawPayloadBytes = message.toByteArray(StandardCharsets.UTF_8)
            
            val payloadBytes = if (useEncryption) {
                StegoCrypto.encrypt(rawPayloadBytes, password.toCharArray())
            } else {
                rawPayloadBytes
            }

            val encodedBitmap = SteganographyEngine.hideData(sourceBitmap, payloadBytes, useEncryption)
            val encodedB64 = bitmapToBase64(encodedBitmap, format)

            val width = sourceBitmap.width
            val height = sourceBitmap.height
            val maxCapacityBytes = ((width * height * 3) / 8)
            val requiredBytes = payloadBytes.size + 8

            val savedDownloadPath = com.example.stego.StegoImageHelper.downloadImageToPublicDownloads(context, encodedBitmap, format)

            scope.launch {
                repository.insert(
                    StegoHistory(
                        actionType = "HIDE",
                        imageName = fileName,
                        payloadSize = message.length,
                        isEncrypted = useEncryption,
                        wasSuccessful = true,
                        details = "Embedded via PixelSecured Web Server Console. Formatted loss-free."
                    )
                )
            }

            val outJson = JSONObject()
            outJson.put("success", true)
            outJson.put("encodedImageB64", encodedB64)
            outJson.put("capacity", maxCapacityBytes)
            outJson.put("requiredSize", requiredBytes)
            outJson.put("savedPath", savedDownloadPath ?: "Sandbox Memory Pipeline")

            sendResponse(output, 200, "application/json", outJson.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            sendError(output, "Steganographic embedding error: " + e.localizedMessage)
        }
    }

    private fun handleDecode(body: String, output: OutputStream) {
        if (body.isEmpty()) {
            sendError(output, "Request body empty.")
            return
        }

        try {
            val json = JSONObject(body)
            val imageB64 = json.getString("imageB64")
            val fileName = json.optString("fileName", "parse_stego_web_canvas.png")
            val password = json.optString("password", "")

            val bitmap = base64ToBitmap(imageB64)
            val extractedResult = SteganographyEngine.extractData(bitmap)

            if (extractedResult == null) {
                scope.launch {
                    repository.insert(
                        StegoHistory(
                            actionType = "EXTRACT",
                            imageName = fileName,
                            payloadSize = 0,
                            isEncrypted = false,
                            wasSuccessful = false,
                            details = "Rejected: Magic signature mismatch."
                        )
                    )
                }
                sendError(output, "No valid PixelSecured signature found in this image canvas.")
                return
            }

            val originalMessage: String
            val isEncrypted = extractedResult.isEncrypted
            
            if (isEncrypted) {
                if (password.isEmpty()) {
                    sendError(output, "Decrypting requires authentication. Secure passphrase is missing.")
                    return
                }
                val decryptedBytes = StegoCrypto.decrypt(extractedResult.payload, password.toCharArray())
                originalMessage = String(decryptedBytes, StandardCharsets.UTF_8)
            } else {
                originalMessage = String(extractedResult.payload, StandardCharsets.UTF_8)
            }

            scope.launch {
                repository.insert(
                    StegoHistory(
                        actionType = "EXTRACT",
                        imageName = fileName,
                        payloadSize = originalMessage.length,
                        isEncrypted = isEncrypted,
                        wasSuccessful = true,
                        details = "Verified & reconstructed successful via Web Console."
                    )
                )
            }

            val outJson = JSONObject()
            outJson.put("success", true)
            outJson.put("message", originalMessage)
            outJson.put("isEncrypted", isEncrypted)
            sendResponse(output, 200, "application/json", outJson.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            sendError(output, "Failed to reconstruct secret: incorrect password key or format decay.")
        }
    }

    private fun handleHistory(output: OutputStream) {
        scope.launch {
            try {
                val flowList = repository.allHistory.first()
                val array = JSONArray()
                for (item in flowList) {
                    val innerObj = JSONObject()
                    innerObj.put("id", item.id)
                    innerObj.put("actionType", item.actionType)
                    innerObj.put("timestamp", item.timestamp)
                    innerObj.put("imageName", item.imageName)
                    innerObj.put("payloadSize", item.payloadSize)
                    innerObj.put("isEncrypted", item.isEncrypted)
                    innerObj.put("wasSuccessful", item.wasSuccessful)
                    innerObj.put("details", item.details)
                    array.put(innerObj)
                }
                
                withContext(Dispatchers.IO) {
                    sendResponse(output, 200, "application/json", array.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.IO) {
                    sendError(output, "Audit query exception: ${e.message}")
                }
            }
        }
    }

    private fun handleHistoryClear(output: OutputStream) {
        scope.launch {
            try {
                repository.clearAll()
                val outJson = JSONObject()
                outJson.put("success", true)
                
                withContext(Dispatchers.IO) {
                    sendResponse(output, 200, "application/json", outJson.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.IO) {
                    sendError(output, "Wiping database exception: ${e.message}")
                }
            }
        }
    }
}
