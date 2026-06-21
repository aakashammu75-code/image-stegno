package com.example.stego.web

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.stego.StegoCrypto
import com.example.stego.SteganographyEngine
import com.example.stego.db.StegoHistory
import com.example.stego.db.StegoRepository
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

class StegoWebServer(
    private val context: android.content.Context,
    private val repository: StegoRepository,
    private val scope: CoroutineScope
) {
    private var server: HttpServer? = null
    var boundPort: Int = 8080
        private set

    fun start() {
        // Try starting from port 8080 up to 8090
        var port = 8080
        var startedSuccessfully = false
        while (port < 8090 && !startedSuccessfully) {
            try {
                val currentServer = HttpServer.create(InetSocketAddress(port), 0)
                currentServer.createContext("/", RootHandler())
                currentServer.createContext("/api/encode", EncodeHandler())
                currentServer.createContext("/api/decode", DecodeHandler())
                currentServer.createContext("/api/history", HistoryHandler())
                currentServer.createContext("/api/history/clear", HistoryClearHandler())
                
                // Set fixed executor for handling concurrent requests
                currentServer.executor = java.util.concurrent.Executors.newFixedThreadPool(4)
                currentServer.start()
                
                server = currentServer
                boundPort = port
                startedSuccessfully = true
            } catch (e: Exception) {
                e.printStackTrace()
                port++
            }
        }
    }

    fun stop() {
        try {
            server?.stop(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addCorsAndDefaultHeaders(exchange: HttpExchange, contentType: String) {
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        exchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type,Authorization")
        exchange.responseHeaders.add("Content-Type", contentType)
    }

    private fun handleOptions(exchange: HttpExchange): Boolean {
        if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
            addCorsAndDefaultHeaders(exchange, "text/plain")
            exchange.sendResponseHeaders(204, -1)
            exchange.close()
            return true
        }
        return false
    }

    private fun sendResponse(exchange: HttpExchange, statusCode: Int, body: String) {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        exchange.sendResponseHeaders(statusCode, bytes.size.toLong())
        val os: OutputStream = exchange.responseBody
        os.write(bytes)
        os.flush()
        os.close()
    }

    private fun sendError(exchange: HttpExchange, errorMessage: String, statusCode: Int = 400) {
        try {
            val responseObj = JSONObject()
            responseObj.put("success", false)
            responseObj.put("error", errorMessage)
            sendResponse(exchange, statusCode, responseObj.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readRequestBody(exchange: HttpExchange): String {
        val reader = BufferedReader(InputStreamReader(exchange.requestBody, StandardCharsets.UTF_8))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        return sb.toString()
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

    // -----------------------------------------------------------------
    // HANDLERS
    // -----------------------------------------------------------------

    private inner class RootHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (handleOptions(exchange)) return
                
                addCorsAndDefaultHeaders(exchange, "text/html; charset=utf-8")
                sendResponse(exchange, 200, StegoWebAssets.INDEX_HTML)
            } catch (e: Exception) {
                e.printStackTrace()
                sendError(exchange, "Internal Server Error: ${e.message}", 500)
            } finally {
                exchange.close()
            }
        }
    }

    private inner class EncodeHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (handleOptions(exchange)) return
                
                addCorsAndDefaultHeaders(exchange, "application/json")
                
                if (!exchange.requestMethod.equals("POST", ignoreCase = true)) {
                    sendError(exchange, "Method not allowed. Use POST.")
                    return
                }

                val requestText = readRequestBody(exchange)
                if (requestText.isEmpty()) {
                    sendError(exchange, "Request body empty.")
                    return
                }

                val json = JSONObject(requestText)
                val imageB64 = json.getString("imageB64")
                val fileName = json.optString("fileName", "undefined_web_canvas.png")
                val message = json.getString("message")
                val password = json.optString("password", "")
                val useEncryption = json.optBoolean("useEncryption", false)
                val format = json.optString("format", "PNG")

                if (message.isEmpty()) {
                    sendError(exchange, "Message payload cannot be empty.")
                    return
                }

                if (useEncryption && password.isEmpty()) {
                    sendError(exchange, "Passphrase is required when encryption toggle is set.")
                    return
                }

                // Run stego calculation
                val sourceBitmap = base64ToBitmap(imageB64)
                val rawPayloadBytes = message.toByteArray(StandardCharsets.UTF_8)
                
                val payloadBytes = if (useEncryption) {
                    StegoCrypto.encrypt(rawPayloadBytes, password.toCharArray())
                } else {
                    rawPayloadBytes
                }

                // Try to hide the data
                val encodedBitmap = SteganographyEngine.hideData(sourceBitmap, payloadBytes, useEncryption)
                val encodedB64 = bitmapToBase64(encodedBitmap, format)

                val width = sourceBitmap.width
                val height = sourceBitmap.height
                val maxCapacityBytes = ((width * height * 3) / 8)
                val requiredBytes = payloadBytes.size + 8

                // Automatically write to persistent public Downloads folder for user convenience
                val savedDownloadPath = com.example.stego.StegoImageHelper.downloadImageToPublicDownloads(context, encodedBitmap, format)

                // Store history logs to our Room database
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
                
                sendResponse(exchange, 200, outJson.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                sendError(exchange, "Steganographic embedding error: " + e.localizedMessage)
            } finally {
                exchange.close()
            }
        }
    }

    private inner class DecodeHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (handleOptions(exchange)) return
                
                addCorsAndDefaultHeaders(exchange, "application/json")

                if (!exchange.requestMethod.equals("POST", ignoreCase = true)) {
                    sendError(exchange, "Method not allowed. Use POST.")
                    return
                }

                val requestText = readRequestBody(exchange)
                val json = JSONObject(requestText)
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
                    sendError(exchange, "No valid PixelSecured signature found in this image canvas.")
                    return
                }

                val originalMessage: String
                val isEncrypted = extractedResult.isEncrypted
                
                if (isEncrypted) {
                    if (password.isEmpty()) {
                        sendError(exchange, "Decrypting requires authentication. Secure passphrase is missing.")
                        return
                    }
                    val decryptedBytes = StegoCrypto.decrypt(extractedResult.payload, password.toCharArray())
                    originalMessage = String(decryptedBytes, StandardCharsets.UTF_8)
                } else {
                    originalMessage = String(extractedResult.payload, StandardCharsets.UTF_8)
                }

                // Log a successful extraction
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
                sendResponse(exchange, 200, outJson.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                sendError(exchange, "Failed to reconstruct secret: incorrect password key or format decay.")
            } finally {
                exchange.close()
            }
        }
    }

    private inner class HistoryHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (handleOptions(exchange)) return
                
                addCorsAndDefaultHeaders(exchange, "application/json")

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
                            sendResponse(exchange, 200, array.toString())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.IO) {
                            sendError(exchange, "Audit query exception: ${e.message}")
                        }
                    } finally {
                        exchange.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sendError(exchange, "Internal Handler Failure", 500)
                exchange.close()
            }
        }
    }

    private inner class HistoryClearHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (handleOptions(exchange)) return
                
                addCorsAndDefaultHeaders(exchange, "application/json")

                scope.launch {
                    try {
                        repository.clearAll()
                        val outJson = JSONObject()
                        outJson.put("success", true)
                        
                        withContext(Dispatchers.IO) {
                            sendResponse(exchange, 200, outJson.toString())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.IO) {
                            sendError(exchange, "Wiping database exception: ${e.message}")
                        }
                    } finally {
                        exchange.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sendError(exchange, "Internal Handler Failure", 500)
                exchange.close()
            }
        }
    }
}
