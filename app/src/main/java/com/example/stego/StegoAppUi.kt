package com.example.stego

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import android.app.Application
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.ValueCallback
import android.net.Uri
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stego.db.StegoHistory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StegoMainScreen(viewModel: StegoViewModel) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("HIDE") }
    var layoutMode by remember { mutableStateOf("WEB_CONSOLE") } // Default to Web Console as requested to convert the app!

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("stego_main_screen"),
        bottomBar = {
            if (layoutMode == "MOBILE") {
                StegoBottomNavigationBar(
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Dashboard Header
            StegoDashboardHeader()

            // Dynamic Client Switching Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C101B))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Desktop Web Console Tab Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .background(
                            color = if (layoutMode == "WEB_CONSOLE") Color(0xFF10B981).copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (layoutMode == "WEB_CONSOLE") Color(0xFF10B981) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { layoutMode = "WEB_CONSOLE" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF10B981), shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Web Console",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (layoutMode == "WEB_CONSOLE") Color.White else Color.Gray,
                        )
                        Text(
                            text = " :${viewModel.webServerPort}",
                            fontSize = 10.sp,
                            color = if (layoutMode == "WEB_CONSOLE") Color(0xFF10B981) else Color.DarkGray,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Native Mobile Tab Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .background(
                            color = if (layoutMode == "MOBILE") Color(0xFF06B6D4).copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (layoutMode == "MOBILE") Color(0xFF06B6D4) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { layoutMode = "MOBILE" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Native",
                            tint = if (layoutMode == "MOBILE") Color(0xFF06B6D4) else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Native Mobile UI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (layoutMode == "MOBILE") Color.White else Color.Gray,
                        )
                    }
                }
            }

            // Tab Content Frame
            if (layoutMode == "WEB_CONSOLE") {
                StegoWebConsoleScreen(viewModel)
            } else {
                when (activeTab) {
                    "HIDE" -> HideSecretTab(viewModel = viewModel, onNavigateToExtract = { activeTab = "EXTRACT" })
                    "EXTRACT" -> ExtractSecretTab(viewModel = viewModel)
                    "HISTORY" -> VaultHistoryTab(
                        viewModel = viewModel,
                        onLoadHistoryItem = { path ->
                            viewModel.loadFromHistoryPath(path)
                            activeTab = "EXTRACT"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StegoDashboardHeader() {
    val timeString = remember {
        val sdf = SimpleDateFormat("HH:mm 'UTC'", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        sdf.format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Subtle neon bottom border
                val strokeHeight = 1.dp.toPx()
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF06B6D4), // Cyan
                            Color(0xFF6366F1), // Indigo
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, size.height - strokeHeight),
                    end = Offset(size.width, size.height - strokeHeight),
                    strokeWidth = strokeHeight
                )
            }
            .background(Color(0xFF0C101B))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF06B6D4).copy(alpha = 0.15f), shape = CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security Status",
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "IMAGE STEGANOGRAPHY",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "LSB Visual Cipher Engine",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFF1E293B), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeString,
                    color = Color(0xFF22D3EE),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun HideSecretTab(viewModel: StegoViewModel, onNavigateToExtract: () -> Unit) {
    val context = LocalContext.current
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val bitmap = StegoImageHelper.loadBitmapFromUri(context, uri)
                if (bitmap != null) {
                    val format = StegoImageHelper.detectImageFormat(context, uri)
                    viewModel.setEncodeImage(bitmap, format)
                }
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hide_secret_tab")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Choose Canvas Image
        item {
            StegoSectionHeader(title = "1. CHOOSE SOURCE IMAGE")
        }

        item {
            // Dynamic programmatic presets
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Immediate Presets (Generate high-quality ciphers instantly)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf("Cyber Grid", "Neon Lattice", "Gold Aurora")
                    items(presets) { preset ->
                        PresetCard(
                            name = preset,
                            onClick = {
                                val generated = StegoSampleGenerator.generatePreset(preset)
                                viewModel.setEncodeImage(generated)
                            }
                        )
                    }
                }
            }
        }

        // Custom File Picker
        item {
            Card(
                onClick = {
                    photoLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_image_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Photo",
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Or Upload Custom Image from Gallery",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Selected Image Preview
        viewModel.selectedBitmapForEncode?.let { bitmap ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("encode_image_preview_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected Canvas (${viewModel.selectedBitmapForEncodeFormat})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22D3EE),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${bitmap.width} x ${bitmap.height} px",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            val imgBitmap = remember(bitmap) { bitmap.asImageBitmap() }
                            Image(
                                bitmap = imgBitmap,
                                contentDescription = "Active Canvas",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Visual Floating Indicator Overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                when {
                                    viewModel.isScanningEncodeImage -> {
                                        Surface(
                                            color = Color(0xFF0F172A).copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    color = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(12.dp),
                                                    strokeWidth = 1.5.dp
                                                )
                                                Text(
                                                    text = "ANALYZING...",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                    viewModel.selectedBitmapForEncodeHasSecret == true -> {
                                        Surface(
                                            color = Color(0xFF7F1D1D).copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFF87171))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Lock icon indicator",
                                                    tint = Color(0xFFF87171),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "SECRET DETECTED",
                                                    color = Color(0xFFF87171),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                    viewModel.selectedBitmapForEncodeHasSecret == false -> {
                                        Surface(
                                            color = Color(0xFF064E3B).copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFF34D399))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Safe icon indicator",
                                                    tint = Color(0xFF34D399),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "CLEAN CANVAS",
                                                    color = Color(0xFF34D399),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Detailed description section alongside indicators
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val indicatorColor = when {
                                        viewModel.isScanningEncodeImage -> Color(0xFF38BDF8)
                                        viewModel.selectedBitmapForEncodeHasSecret == true -> Color(0xFFEF4444)
                                        else -> Color(0xFF10B981)
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(indicatorColor, shape = RoundedCornerShape(100.dp))
                                    )
                                    
                                    Text(
                                        text = when {
                                            viewModel.isScanningEncodeImage -> "Steganography deep-scan in progress..."
                                            viewModel.selectedBitmapForEncodeHasSecret == true -> "WARNING: Existing stego message inside!"
                                            else -> "Ready to hide data securely."
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = when {
                                        viewModel.isScanningEncodeImage -> "Analyzing pixels color profiles for LSB magic signature..."
                                        viewModel.selectedBitmapForEncodeHasSecret == true -> {
                                            val cryptText = if (viewModel.selectedBitmapForEncodeIsEncrypted == true) "AES-GCM encrypted" else "plaintext"
                                            "This image contains a valid $cryptText hidden payload starting block. Injecting a new payload will fully overwrite the existing one."
                                        }
                                        else -> "Our pixel scanning confirms no stego signature is embedded in this image. Safe to embed new secrets."
                                    },
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Input Secret message
        item {
            StegoSectionHeader(title = "2. SECRET PAYLOAD & CRYPTO")
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131A26), shape = RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, Color(0xFF1E293B)), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Secret Input text
                OutlinedTextField(
                    value = viewModel.secretMessage,
                    onValueChange = { viewModel.updateSecretMessage(it) },
                    label = { Text("Secret Text Message") },
                    placeholder = { Text("Type sensitive data that you want to seal...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("secret_message_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF06B6D4),
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedLabelColor = Color(0xFF06B6D4)
                    ),
                    maxLines = 6
                )

                // Encryption toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Crypto Lock",
                            tint = if (viewModel.isEncryptionEnabled) Color(0xFF06B6D4) else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AES-256 Cryptographic Lock",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Encrypts payload before hiding in pixels",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Switch(
                        checked = viewModel.isEncryptionEnabled,
                        onCheckedChange = { viewModel.toggleEncryption(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF06B6D4),
                            checkedTrackColor = Color(0xFF06B6D4).copy(alpha = 0.3f),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }

                // Password field
                AnimatedVisibility(visible = viewModel.isEncryptionEnabled) {
                    var passwordVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = viewModel.encodePassword,
                        onValueChange = { viewModel.updateEncodePassword(it) },
                        label = { Text("AES Passphrase / Key") },
                        placeholder = { Text("Required to decrypt later") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("passphrase_input"),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Text(
                                text = if (passwordVisible) "HIDE" else "SHOW",
                                color = Color(0xFF22D3EE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .clickable { passwordVisible = !passwordVisible }
                                    .padding(end = 12.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF06B6D4),
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedLabelColor = Color(0xFF06B6D4)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    )
                }

                // Capacity meter
                if (viewModel.selectedBitmapForEncode != null) {
                    val progress = if (viewModel.maxCapacityInBytes > 0) {
                        (viewModel.currentPayloadSizeInBytes.toFloat() / viewModel.maxCapacityInBytes.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    val progressColor = if (progress > 0.85f) Color(0xFFF43F5E) else Color(0xFF10B981)

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Image Data Capacity Meter",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "${viewModel.currentPayloadSizeInBytes} B / ${viewModel.maxCapacityInBytes} B",
                                fontSize = 11.sp,
                                color = progressColor,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            color = progressColor,
                            trackColor = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        if (progress > 0.85f) {
                            Text(
                                text = "Warning: Approaching pixel boundary limits!",
                                color = Color(0xFFF43F5E),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action & Results Section
        item {
            StegoSectionHeader(title = "3. STAMP & SEAL")
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                val hasCanvas = viewModel.selectedBitmapForEncode != null
                val payloadInLimit = viewModel.currentPayloadSizeInBytes <= viewModel.maxCapacityInBytes

                if (hasCanvas) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Stego Export Format",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22D3EE),
                                modifier = Modifier.padding(bottom = 4.dp),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Losing details via JPEG deletes hidden bits. Saving as PNG or BMP guarantees lossless visual hiding.",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf("PNG", "BMP").forEach { format ->
                                    val isSelected = viewModel.selectedOutputFormat == format
                                    Card(
                                        onClick = { viewModel.updateSelectedOutputFormat(format) },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFF22D3EE).copy(alpha = 0.15f) else Color(0xFF1E293B)
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFF22D3EE) else Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("output_format_$format")
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (format == "PNG") "PNG (Lossless Standard)" else "BMP (Raw Digital)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFF22D3EE) else Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { viewModel.hideMessageInImage() },
                    enabled = hasCanvas && viewModel.secretMessage.isNotEmpty() && payloadInLimit && (viewModel.encodeState !is EncodeResultState.Loading),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("seal_secret_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF06B6D4),
                        disabledContainerColor = Color(0xFF1E293B)
                    )
                ) {
                    if (viewModel.encodeState is EncodeResultState.Loading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield Guard",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SEAL & SECURELY HIDE DATA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (!hasCanvas) {
                    Text(
                        text = "* Please generate or upload an image canvas first in Step 1.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }
        }

        // Render Encode Result States
        item {
            when (val state = viewModel.encodeState) {
                is EncodeResultState.Success -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E2A)),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success tick",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "STEGANOGRAPHIC PROCESS COMPLETE",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your secret data has been mathematically sealed into the lowest binary bit values of pixel colors. It is entirely invisible to standard inspection.",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Saved Path (Device Pictures/Steganography):\n${state.savedPath}",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Primary call to action: Download/Save modified stego image to Downloads folder
                            Button(
                                onClick = {
                                    viewModel.downloadBitmapToDownloadsFolder(state.bitmap, viewModel.selectedOutputFormat)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .testTag("download_stego_image_button"),
                                enabled = !viewModel.isDownloading
                            ) {
                                if (viewModel.isDownloading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Saving to Downloads...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Save file",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download / Save to Downloads", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            viewModel.downloadSuccessPath?.let { path ->
                                Text(
                                    text = "✓ Success! Saved to: $path",
                                    fontSize = 11.sp,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                            }

                            viewModel.downloadError?.let { err ->
                                Text(
                                    text = "✗ Error: $err",
                                    fontSize = 11.sp,
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // Auto load this into extraction screen for instant testing
                                        viewModel.setDecodeImage(state.bitmap)
                                        // Navigate to Extract (done via tab delegate callback parameter)
                                        onNavigateToExtract()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.5f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Verify Extract icon",
                                        tint = Color(0xFF06B6D4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verify Extract", fontSize = 11.sp, color = Color(0xFF06B6D4))
                                }

                                Button(
                                    onClick = {
                                        // Build share intent for image
                                        try {
                                            val shareIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "Checkout this stego file! Message is securely hidden within. Path: ${state.savedPath}")
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Stego Reference"))
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share reference",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share Path", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
                is EncodeResultState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1920)),
                        border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error notification",
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = state.message,
                                fontSize = 12.sp,
                                color = Color(0xFFF43F5E),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ExtractSecretTab(viewModel: StegoViewModel) {
    val context = LocalContext.current
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val bitmap = StegoImageHelper.loadBitmapFromUri(context, uri)
                if (bitmap != null) {
                    val format = StegoImageHelper.detectImageFormat(context, uri)
                    viewModel.setDecodeImage(bitmap, format)
                }
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("extract_secret_tab")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StegoSectionHeader(title = "1. CHOOSE STEGO IMAGE")
        }

        item {
            Card(
                onClick = {
                    photoLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("select_decode_image_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Open Library",
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pick Sealed Image from Gallery",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Active image loading preview
        viewModel.selectedBitmapForDecode?.let { bitmap ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("decode_image_preview_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Extraction Canvas Target (${viewModel.selectedBitmapForDecodeFormat})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22D3EE),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${bitmap.width} x ${bitmap.height} px",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            val imgBitmap = remember(bitmap) { bitmap.asImageBitmap() }
                            Image(
                                bitmap = imgBitmap,
                                contentDescription = "Active Canvas to Extract",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Visual Floating Indicator Overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                when (val state = viewModel.decodeResultState) {
                                    is DecodeResultState.Loading -> {
                                        Surface(
                                            color = Color(0xFF0F172A).copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    color = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(12.dp),
                                                    strokeWidth = 1.5.dp
                                                )
                                                Text(
                                                    text = "EXTRACTING...",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                    is DecodeResultState.PasswordRequired, is DecodeResultState.Success -> {
                                        Surface(
                                            color = Color(0xFF064E3B).copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFF34D399))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Lock detected indicator",
                                                    tint = Color(0xFF34D399),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "SECRET SIGNATURE DETECTED",
                                                    color = Color(0xFF34D399),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                    is DecodeResultState.Error -> {
                                        Surface(
                                            color = Color(0xFF7F1D1D).copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFF87171))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Empty canvas indicator",
                                                    tint = Color(0xFFF87171),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "NO VALID SECRET SIGNATURE",
                                                    color = Color(0xFFF87171),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                        
                        // Detailed description section alongside indicators
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                val state = viewModel.decodeResultState
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val indicatorColor = when (state) {
                                        is DecodeResultState.Loading -> Color(0xFF38BDF8)
                                        is DecodeResultState.PasswordRequired, is DecodeResultState.Success -> Color(0xFF10B981)
                                        else -> Color(0xFFEF4444)
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(indicatorColor, shape = RoundedCornerShape(100.dp))
                                    )
                                    
                                    Text(
                                        text = when (state) {
                                            is DecodeResultState.Loading -> "Deep scanning bitmap structures..."
                                            is DecodeResultState.PasswordRequired -> "Encrypted Stego Secret Found!"
                                            is DecodeResultState.Success -> "Message Extracted Successfully!"
                                            else -> "Payload Analysis Failed / Empty"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = when (state) {
                                        is DecodeResultState.Loading -> "Analyzing pixel color profiles to detect standard magic headers..."
                                        is DecodeResultState.PasswordRequired -> "A cryptographically secured steganographic payload is encoded. Provide the password below to decrypt and view."
                                        is DecodeResultState.Success -> {
                                            val cryptStr = if (state.isEncrypted) "decrypted successfully" else "read directly"
                                            "A valid text message was found and $cryptStr. The contents are displayed below."
                                        }
                                        is DecodeResultState.Error -> "The selected canvas contains raw unencoded pixels, is heavily compressed (JPEG), or doesn't match the required steganography magic-header format."
                                        else -> "Select/upload an image to initiate the pixel extraction scanner."
                                    },
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            StegoSectionHeader(title = "2. EXTRACTED SYSTEM STATUS")
        }

        // Decoding States Rendering
        item {
            when (val state = viewModel.decodeResultState) {
                is DecodeResultState.Idle -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Information status",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Awaiting Stego container analysis...",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
                is DecodeResultState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF06B6D4))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Extracting lowest bits from RGB color tables...",
                                color = Color(0xFF22D3EE),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                is DecodeResultState.PasswordRequired -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B232D)),
                        border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Encrypted Container lock",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SECURE PAYLOAD DETECTED",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This signature utilizes AES-256 GCM security lock. Supply the valid cipher passphrase below to extract the message contents.",
                                fontSize = 12.sp,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = viewModel.decodePassword,
                                onValueChange = { viewModel.updateDecodePassword(it) },
                                label = { Text("AES Secret Passphrase") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("extract_password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF06B6D4),
                                    unfocusedBorderColor = Color(0xFF1E293B)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.decryptAndExtract() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("decrypt_reveal_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Key Icon",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("DECRYPT & REVEAL SECRET", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
                is DecodeResultState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E2A)),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Extracted Done",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PAYLOAD EXTRACTED",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (state.isEncrypted) Color(0xFF06B6D4).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (state.isEncrypted) "AES SECURED" else "PLAINTEXT",
                                        fontSize = 9.sp,
                                        color = if (state.isEncrypted) Color(0xFF22D3EE) else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Secret Message Contents:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Secret Text viewer
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF090C12), shape = RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, Color(0xFF1E293B)), shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = state.originalMessage,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("extracted_secret_text")
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        try {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Stego Secret", state.originalMessage)
                                            clipboard.setPrimaryClip(clip)
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Clipboard Copy",
                                        tint = Color(0xFF06B6D4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Text", fontSize = 12.sp, color = Color(0xFF06B6D4))
                                }

                                Button(
                                    onClick = {
                                        try {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, state.originalMessage)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Extracted Text"))
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share text",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share Text", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
                is DecodeResultState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1920)),
                        border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error notification",
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = state.message,
                                fontSize = 12.sp,
                                color = Color(0xFFF43F5E),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VaultHistoryTab(viewModel: StegoViewModel, onLoadHistoryItem: (String) -> Unit) {
    val historyItems by viewModel.historyList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("vault_history_tab")
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StegoSectionHeader(title = "VAULT TRANSACTIONS LOG")
            
            if (historyItems.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clean All Logs",
                        tint = Color(0xFFF43F5E)
                    )
                }
            }
        }

        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No Operations history",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "History Ledger is Clear",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "All encoding/decoding operations appear here.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyItems) { record ->
                    HistoryItemCard(
                        history = record,
                        onLoad = {
                            val context = viewModel.getApplication<Application>()
                            val internalDir = File(context.filesDir, "stego_history_images")
                            val file = File(internalDir, record.imageName)
                            if (file.exists()) {
                                onLoadHistoryItem(file.absolutePath)
                            }
                        },
                        onDelete = { viewModel.deleteHistoryRecord(record) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    history: StegoHistory,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val isHideAction = history.actionType == "HIDE"
    val dateString = remember(history.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(history.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
        border = BorderStroke(1.dp, if (history.wasSuccessful) Color(0xFF1E293B) else Color(0xFFF43F5E).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (isHideAction) Color(0xFF06B6D4).copy(alpha = 0.15f) else Color(0xFF6366F1).copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Operation action",
                            tint = if (isHideAction) Color(0xFF06B6D4) else Color(0xFF6366F1),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isHideAction) "DATA ENCODED (SEAL)" else "PAYLOAD UNSEALED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHideAction) Color(0xFF22D3EE) else Color(0xFF6366F1),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = dateString,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (history.isEncrypted) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD97706).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("AES", fontSize = 8.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Single Log",
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "File: ${history.imageName}",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Payload Size: ${history.payloadSize} Bytes | Status: ${history.details}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // If is a HIDE action, and the private history file exists, offer "Quick Load in Decrypt"
                if (isHideAction && history.wasSuccessful) {
                    Button(
                        onClick = onLoad,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("Extract", fontSize = 10.sp, color = Color(0xFF06B6D4))
                    }
                }
            }
        }
    }
}

@Composable
fun PresetCard(name: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(105.dp, 60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .border(BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = name,
                tint = Color(0xFF22D3EE),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StegoSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF06B6D4),
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
fun StegoBottomNavigationBar(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0C101B))
            .drawBehind {
                val strokeHeight = 1.dp.toPx()
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeHeight
                )
            }
            .navigationBarsPadding()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            label = "Seal Secret",
            icon = Icons.Default.Lock,
            isActive = activeTab == "HIDE",
            onClick = { onTabSelected("HIDE") },
            testTag = "nav_tab_hide"
        )
        BottomNavItem(
            label = "Unseal data",
            icon = Icons.Default.Lock,
            isActive = activeTab == "EXTRACT",
            onClick = { onTabSelected("EXTRACT") },
            testTag = "nav_tab_extract"
        )
        BottomNavItem(
            label = "Ledger Logs",
            icon = Icons.Default.Info,
            isActive = activeTab == "HISTORY",
            onClick = { onTabSelected("HISTORY") },
            testTag = "nav_tab_history"
        )
    }
}

@Composable
fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
            .padding(horizontal = 14.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) Color(0xFF06B6D4) else Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) Color.White else Color(0xFF94A3B8)
        )
    }
}

@Composable
fun StegoWebConsoleScreen(viewModel: StegoViewModel) {
    val context = LocalContext.current
    var uploadMessageCallback: ValueCallback<Array<Uri>>? by remember { mutableStateOf(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uploadMessageCallback != null) {
            val uriArray = uris.map { it }.toTypedArray()
            uploadMessageCallback?.onReceiveValue(if (uriArray.isNotEmpty()) uriArray else null)
            uploadMessageCallback = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            return false
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            uploadMessageCallback = filePathCallback
                            try {
                                fileChooserLauncher.launch("image/*")
                            } catch (e: Exception) {
                                uploadMessageCallback?.onReceiveValue(null)
                                uploadMessageCallback = null
                            }
                            return true
                        }
                    }

                    loadUrl("http://localhost:${viewModel.webServerPort}")
                }
            },
            update = {
                // Self-updating or single load is handled inside factory
            }
        )
    }
}
