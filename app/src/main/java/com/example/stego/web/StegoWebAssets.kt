package com.example.stego.web

object StegoWebAssets {
    val INDEX_HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PixelSecured Web Console</title>
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- FontAwesome Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        @import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@300;400;500;700&family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap');
        
        body {
            font-family: 'Plus Jakarta Sans', sans-serif;
        }
        .mono {
            font-family: 'JetBrains Mono', monospace;
        }
    </style>
</head>
<body class="bg-slate-950 text-slate-100 min-h-screen selection:bg-sky-500 selection:text-white">

    <div class="max-w-6xl mx-auto px-4 py-8">
        <!-- Header -->
        <header class="flex flex-col md:flex-row md:items-center justify-between border-b border-slate-800 pb-6 mb-8 gap-4">
            <div class="flex items-center space-x-4">
                <div class="bg-gradient-to-tr from-sky-500 to-emerald-500 p-3 rounded-xl shadow-lg shadow-sky-500/10">
                    <i class="fa-solid fa-user-shield text-2xl text-slate-950"></i>
                </div>
                <div>
                    <h1 class="text-2xl font-bold tracking-tight bg-gradient-to-r from-sky-400 via-teal-400 to-emerald-400 bg-clip-text text-transparent">
                        PIXEL-SECURE WEB CONSOLE
                    </h1>
                    <p class="text-xs text-slate-400 mt-0.5 tracking-wide uppercase mono">
                        Lossless Steganography Cryptographic Hub
                    </p>
                </div>
            </div>
            
            <div class="flex items-center bg-slate-900 border border-slate-800 px-4 py-2 rounded-xl text-xs space-x-3">
                <span class="relative flex h-2 w-2">
                    <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                    <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
                </span>
                <span class="mono text-slate-300">Server Connected (Local Node: 8080)</span>
            </div>
        </header>

        <!-- Main Workspace -->
        <main class="grid grid-cols-1 lg:grid-cols-12 gap-8">
            
            <!-- Left Sidebar Navigation & Controls -->
            <div class="lg:col-span-3 flex flex-col space-y-4">
                <button onclick="switchTab('encode')" id="btn-tab-encode" class="flex items-center space-x-3 px-4 py-3.5 rounded-xl transition duration-250 border border-sky-500/20 bg-sky-950/40 text-sky-400 font-semibold text-sm shadow-md shadow-sky-500/5">
                    <i class="fa-solid fa-compress text-lg"></i>
                    <span>Inject & Encode</span>
                </button>
                
                <button onclick="switchTab('decode')" id="btn-tab-decode" class="flex items-center space-x-3 px-4 py-3.5 rounded-xl transition duration-250 border border-slate-800 text-slate-400 hover:text-slate-200 hover:bg-slate-900 font-semibold text-sm">
                    <i class="fa-solid fa-expand text-lg"></i>
                    <span>Extract & Decode</span>
                </button>
                
                <button onclick="switchTab('history')" id="btn-tab-history" class="flex items-center space-x-3 px-4 py-3.5 rounded-xl transition duration-250 border border-slate-800 text-slate-400 hover:text-slate-200 hover:bg-slate-900 font-semibold text-sm">
                    <i class="fa-solid fa-receipt text-lg"></i>
                    <span>Vault Activity Logs</span>
                </button>

                <div class="bg-gradient-to-tr from-slate-900 to-slate-950 p-5 rounded-2xl border border-slate-800/80 mt-4 space-y-3">
                    <div class="flex items-center space-x-2 text-xs text-sky-400 font-bold tracking-wider uppercase">
                        <i class="fa-solid fa-shield-halved"></i>
                        <span>Tech Specs</span>
                    </div>
                    <p class="text-xs text-slate-400 leading-relaxed">
                        Uses LSB (Least Significant Bit) steganography which modifies the raw RGB bytes of lossless images. Supported formats are non-compressed PNG and BMP to prevent data degradation.
                    </p>
                    <div class="pt-2 border-t border-slate-800 flex flex-col space-y-1.5 text-[11px] text-slate-400 mono">
                        <div>Cipher: <span class="text-emerald-400">AES-256-GCM</span></div>
                        <div>Mode: <span class="text-emerald-400">PBKDF2 Salted</span></div>
                        <div>Fidelity: <span class="text-emerald-400">100% loss-free</span></div>
                    </div>
                </div>
            </div>

            <!-- Main Content Panels -->
            <div class="lg:col-span-9">
                
                <!-- 1. ENCODE PANEL -->
                <div id="panel-encode" class="space-y-6">
                    <div class="bg-slate-900/40 rounded-3xl border border-slate-800/80 p-6 md:p-8 space-y-6">
                        <div class="flex items-center justify-between pb-4 border-b border-slate-800">
                            <div>
                                <h2 class="text-lg font-bold text-slate-100 flex items-center space-x-2">
                                    <i class="fa-solid fa-compress text-sky-400"></i>
                                    <span>Stego Encoder Pipeline</span>
                                </h2>
                                <p class="text-xs text-slate-400 mt-1">Embed raw or encrypted string payloads seamlessly into digital pixels</p>
                            </div>
                            <span class="mono text-[10px] text-sky-400 bg-sky-950/80 border border-sky-800 px-2.5 py-1 rounded-full uppercase font-bold tracking-wider">
                                Phase 1
                            </span>
                        </div>

                        <!-- Grid -->
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                            <!-- Image Selection Column -->
                            <div class="space-y-4">
                                <label class="block text-xs font-bold text-slate-300 uppercase tracking-wider">Select Base Canvas</label>
                                
                                <div id="encode-drag-box" onclick="document.getElementById('encode-file').click()" class="border-2 border-dashed border-slate-800 hover:border-sky-500/50 bg-slate-950/50 hover:bg-slate-900/30 rounded-2xl p-6 flex flex-col items-center justify-center cursor-pointer transition duration-200">
                                    <input type="file" id="encode-file" accept="image/png, image/bmp" class="hidden" onchange="handleEncodeImageSelect(this)">
                                    <i class="fa-solid fa-cloud-arrow-up text-3xl text-slate-600 mb-3" id="encode-upload-icon"></i>
                                    <span class="text-sm text-slate-300 font-semibold mb-1" id="encode-box-text">Choose PNG or BMP file</span>
                                    <span class="text-xs text-slate-500 text-center leading-relaxed">Drag-n-drop or click to browse local files</span>
                                </div>

                                <!-- Image Details Card -->
                                <div id="encode-image-details" class="hidden bg-slate-950/80 border border-slate-800 rounded-xl p-4 flex items-center justify-between space-x-4">
                                    <div class="flex items-center space-x-3 overflow-hidden">
                                        <img id="encode-preview-img" class="h-12 w-12 object-cover rounded-lg border border-slate-800 bg-slate-900" src="" alt="preview">
                                        <div class="overflow-hidden">
                                            <div class="text-xs font-bold text-slate-100 truncate" id="encode-fname">-</div>
                                            <div class="text-[10px] text-slate-400 mono mt-0.5" id="encode-fdims">-</div>
                                        </div>
                                    </div>
                                    <button onclick="resetEncodeImage()" class="text-slate-500 hover:text-rose-400 px-2.5 py-1.5 rounded-lg hover:bg-slate-900 text-xs transition">
                                        Remove
                                    </button>
                                </div>
                            </div>

                            <!-- Payload Inputs Column -->
                            <div class="space-y-4">
                                <div class="space-y-1.5">
                                    <label for="encode-msg" class="block text-xs font-bold text-slate-300 uppercase tracking-wider">Secret Message Payload</label>
                                    <textarea id="encode-msg" rows="4" placeholder="Enter the secret content to embed..." oninput="recalcCapacity()" class="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-sm focus:border-sky-500 focus:ring-1 focus:ring-sky-500 outline-none text-slate-200 placeholder:text-slate-600 leading-relaxed resize-none"></textarea>
                                </div>

                                <div class="bg-slate-950/80 rounded-xl border border-slate-800 p-4 space-y-3.5">
                                    <div class="flex items-center justify-between">
                                        <div class="flex flex-col">
                                            <span class="text-xs font-bold text-slate-200">AES-256 Encryption</span>
                                            <span class="text-[10px] text-slate-400">Secure the byte stream with crypto password</span>
                                        </div>
                                        <label class="inline-flex items-center cursor-pointer">
                                            <input type="checkbox" id="encode-encrypt-toggle" class="sr-only peer" onchange="toggleEncryptSection(this)">
                                            <div class="relative w-9 h-5 bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-slate-400 after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-emerald-500 peer-checked:after:bg-slate-950 peer-checked:after:border-transparent"></div>
                                        </label>
                                    </div>

                                    <div id="encode-password-container" class="hidden transition duration-200">
                                        <div class="relative">
                                            <input type="password" id="encode-password" placeholder="Passphrase key setup" class="w-full bg-slate-900 border border-slate-800 rounded-lg py-2 pl-3 pr-10 text-xs focus:border-sky-500 outline-none placeholder:text-slate-600 text-emerald-400 mono">
                                            <i class="fa-solid fa-lock absolute right-3 top-2.5 text-slate-600 text-[11px]"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Capacity calculations -->
                        <div id="capacity-meter" class="bg-slate-950/40 rounded-xl border border-slate-800 p-4 space-y-2">
                            <div class="flex items-center justify-between text-xs font-semibold">
                                <span class="text-slate-400">Pixel Payload Usage:</span>
                                <span id="capacity-text" class="mono text-slate-200">No Image Loaded</span>
                            </div>
                            <div class="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                                <div id="capacity-bar" class="bg-sky-400 h-1.5 rounded-full w-0 transition-all duration-300"></div>
                            </div>
                        </div>

                        <!-- Inject Action Button -->
                        <div class="flex flex-col space-y-3 pt-2">
                            <button id="btn-submit-encode" onclick="submitEncode()" class="w-full bg-gradient-to-r from-sky-500 to-teal-500 hover:from-sky-600 hover:to-teal-600 text-slate-950 font-bold py-3.5 px-6 rounded-xl text-sm transition duration-200 flex items-center justify-center space-x-2 tracking-wide uppercase shadow-lg shadow-sky-500/10">
                                <i class="fa-solid fa-shield-virus"></i>
                                <span>Inject Secret Message & Process Pixels</span>
                            </button>
                        </div>
                    </div>

                    <!-- Encode Result Card -->
                    <div id="encode-result" class="hidden bg-slate-900/40 rounded-3xl border border-emerald-500/20 p-6 md:p-8 space-y-6">
                        <div class="flex items-center space-x-3 pb-4 border-b border-slate-800">
                            <div class="bg-emerald-500/10 p-2 rounded-lg text-emerald-400">
                                <i class="fa-solid fa-circle-check text-xl"></i>
                            </div>
                            <div>
                                <h3 class="text-md font-bold text-slate-100">Embedding Completed!</h3>
                                <p class="text-xs text-slate-400">A pixel-perfect lossless container has been compiled successfully</p>
                            </div>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 items-center">
                            <div class="space-y-4">
                                <div class="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-2">
                                    <div class="text-xs flex justify-between"><span class="text-slate-400">Format:</span><span id="res-format" class="mono font-semibold">-</span></div>
                                    <div class="text-xs flex justify-between"><span class="text-slate-400">Original Capacity:</span><span id="res-cap" class="mono font-semibold">-</span></div>
                                    <div class="text-xs flex justify-between"><span class="text-slate-400">Payload Weight:</span><span id="res-size" class="mono font-semibold">-</span></div>
                                    <div class="text-xs flex justify-between"><span class="text-slate-400">Security Mode:</span><span id="res-crypt" class="mono font-semibold text-emerald-400">-</span></div>
                                </div>
                                <a id="btn-download-stego" href="#" download="" class="w-full bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-bold py-3 px-6 rounded-xl text-xs transition duration-200 flex items-center justify-center space-x-2 uppercase tracking-wider">
                                    <i class="fa-solid fa-download"></i>
                                    <span>Download Stego Image</span>
                                </a>
                            </div>
                            <div class="flex flex-col items-center justify-center border border-slate-800 bg-slate-950/60 p-4 rounded-2xl relative">
                                <div class="absolute inset-x-0 top-3 text-[10px] uppercase font-bold mono text-center text-emerald-400">Pixel-Sized Preview</div>
                                <img id="res-img" class="max-h-48 object-contain rounded-lg border border-slate-800 bg-slate-900 mt-4 shadow-lg" src="" alt="stego result img">
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 2. DECODE PANEL -->
                <div id="panel-decode" class="hidden space-y-6">
                    <div class="bg-slate-900/40 rounded-3xl border border-slate-800/80 p-6 md:p-8 space-y-6">
                        <div class="flex items-center justify-between pb-4 border-b border-slate-800">
                            <div>
                                <h2 class="text-lg font-bold text-slate-100 flex items-center space-x-2">
                                    <i class="fa-solid fa-expand text-sky-400"></i>
                                    <span>Stego Extractor Engine</span>
                                </h2>
                                <p class="text-xs text-slate-400 mt-1">Deep-scan binary canvases to reconstruct and read hidden parameters</p>
                            </div>
                            <span class="mono text-[10px] text-sky-400 bg-sky-950/80 border border-sky-800 px-2.5 py-1 rounded-full uppercase font-bold tracking-wider">
                                Phase 2
                            </span>
                        </div>

                        <!-- Grid -->
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                            <!-- Image Selection Column -->
                            <div class="space-y-4">
                                <label class="block text-xs font-bold text-slate-300 uppercase tracking-wider">Target Stego Image</label>
                                
                                <div id="decode-drag-box" onclick="document.getElementById('decode-file').click()" class="border-2 border-dashed border-slate-800 hover:border-sky-500/50 bg-slate-950/50 hover:bg-slate-900/30 rounded-2xl p-6 flex flex-col items-center justify-center cursor-pointer transition duration-200">
                                    <input type="file" id="decode-file" accept="image/png, image/bmp" class="hidden" onchange="handleDecodeImageSelect(this)">
                                    <i class="fa-solid fa-circle-nodes text-3xl text-slate-600 mb-3"></i>
                                    <span class="text-sm text-slate-300 font-semibold mb-1">Choose png or bmp stego image</span>
                                    <span class="text-xs text-slate-500 text-center leading-relaxed">Drag-n-drop or click to parse target canvas</span>
                                </div>

                                <!-- Image Details Card -->
                                <div id="decode-image-details" class="hidden bg-slate-950/80 border border-slate-800 rounded-xl p-4 flex items-center justify-between space-x-4">
                                    <div class="flex items-center space-x-3 overflow-hidden">
                                        <img id="decode-preview-img" class="h-12 w-12 object-cover rounded-lg border border-slate-800 bg-slate-900" src="" alt="preview">
                                        <div class="overflow-hidden">
                                            <div class="text-xs font-bold text-slate-100 truncate" id="decode-fname">-</div>
                                            <div class="text-[10px] text-slate-400 mono mt-0.5" id="decode-fdims">-</div>
                                        </div>
                                    </div>
                                    <button onclick="resetDecodeImage()" class="text-slate-500 hover:text-rose-400 px-2.5 py-1.5 rounded-lg hover:bg-slate-900 text-xs transition">
                                        Remove
                                    </button>
                                </div>
                            </div>

                            <!-- Encryption Key Column -->
                            <div class="space-y-4">
                                <div class="space-y-1">
                                    <label for="decode-password" class="block text-xs font-bold text-slate-300 uppercase tracking-wider">Passphrase Security Key</label>
                                    <p class="text-[10px] text-slate-400">Leave completely blank if payload was stored plaintext</p>
                                </div>
                                <div class="relative">
                                    <input type="password" id="decode-password" placeholder="AES cryptographic key" class="w-full bg-slate-950 border border-slate-800 rounded-xl py-3 pl-4 pr-10 text-sm focus:border-sky-500 focus:ring-1 focus:ring-sky-500 outline-none text-emerald-400 mono placeholder:text-slate-600">
                                    <i class="fa-solid fa-lock absolute right-4 top-3.5 text-slate-600"></i>
                                </div>
                            </div>
                        </div>

                        <!-- Action bar -->
                        <button id="btn-submit-decode" onclick="submitDecode()" class="w-full bg-slate-100 hover:bg-white text-slate-950 font-bold py-3.5 px-6 rounded-xl text-sm transition duration-200 flex items-center justify-center space-x-2 tracking-wide uppercase shadow-lg">
                            <i class="fa-solid fa-circle-notch animate-spin text-sm hidden" id="decode-spinner"></i>
                            <i class="fa-solid fa-microchip text-sm" id="decode-icon"></i>
                            <span>Execute Extraction Algorithm</span>
                        </button>
                    </div>

                    <!-- Extraction Result Monospace Terminal -->
                    <div id="decode-result" class="hidden space-y-4">
                        <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider">Reconstructed Payload Console Output: </label>
                        <div class="bg-slate-900 border border-slate-800 rounded-xl p-5 relative overflow-hidden">
                            <div class="absolute right-4 top-4 flex space-x-2">
                                <button onclick="copyExtractedText()" class="bg-slate-950 hover:bg-slate-800 border border-slate-800 px-3 py-1.5 rounded-lg text-xs font-semibold text-sky-400 hover:text-sky-300 transition flex items-center space-x-1.5">
                                    <i class="fa-solid fa-copy"></i>
                                    <span>Copy Text</span>
                                </button>
                            </div>
                            <div class="flex items-center space-x-2 text-xs text-slate-500 border-b border-slate-800 pb-3 mb-4 select-none uppercase tracking-widest mono">
                                <span class="bg-emerald-500 h-2 w-2 rounded-full inline-block animate-pulse"></span>
                                <span>Output Segment reconstructed successful</span>
                            </div>
                            <div class="mono text-emerald-400/95 leading-relaxed text-sm whitespace-pre-wrap max-h-64 overflow-y-auto" id="decode-output-text">
                                -
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 3. HISTORY PANEL -->
                <div id="panel-history" class="hidden space-y-6">
                    <div class="bg-slate-900/40 rounded-3xl border border-slate-800/80 p-6 md:p-8 space-y-6">
                        <div class="flex items-center justify-between pb-4 border-b border-slate-800">
                            <div>
                                <h2 class="text-lg font-bold text-slate-100 flex items-center space-x-2">
                                    <i class="fa-solid fa-receipt text-sky-400"></i>
                                    <span>Integrated Database Vault Logs</span>
                                </h2>
                                <p class="text-xs text-slate-400 mt-1">Real-time Room Database logs, synchronized with the host mobile device storage</p>
                            </div>
                            <button onclick="clearHistory()" class="bg-slate-950 hover:bg-slate-900 border border-slate-800 text-rose-400 hover:text-rose-300 px-3.5 py-1.5 rounded-xl text-xs font-semibold uppercase tracking-wider transition">
                                Clear Audit Trail
                            </button>
                        </div>

                        <!-- Logs list -->
                        <div class="space-y-4" id="history-list">
                            <div class="text-slate-500 text-center py-12 text-sm">
                                <i class="fa-solid fa-spinner animate-spin text-xl mb-3 block"></i>
                                Loading secure audit trail...
                            </div>
                        </div>
                    </div>
                </div>

            </div>

        </main>
    </div>

    <!-- Alert Banners -->
    <div id="alert-box" class="fixed bottom-6 right-6 max-w-sm hidden transition-all duration-300 transform translate-y-12 z-50">
        <div id="alert-content" class="bg-slate-900 border border-slate-800/80 rounded-2xl p-4 flex items-start space-x-3.5 shadow-2xl">
            <div id="alert-icon-container" class="p-1 px-2.5 rounded-lg text-lg">
                <i id="alert-icon" class="fa-solid"></i>
            </div>
            <div>
                <h4 id="alert-title" class="font-bold text-xs uppercase tracking-wide">Alert</h4>
                <p id="alert-body" class="text-xs text-slate-400 mt-1 leading-relaxed"></p>
            </div>
        </div>
    </div>

    <!-- Script behavior logic -->
    <script>
        let currentTab = 'encode';
        let encodeSelectedFileBase64 = null;
        let decodeSelectedFileBase64 = null;
        let encodeFileName = "";
        let decodeFileName = "";
        let encodeImageDims = "";
        let decodeImageDims = "";
        let encodeImageWidth = 0;
        let encodeImageHeight = 0;

        function switchTab(tabId) {
            currentTab = tabId;
            
            // Hide all panels
            document.getElementById('panel-encode').classList.add('hidden');
            document.getElementById('panel-decode').classList.add('hidden');
            document.getElementById('panel-history').classList.add('hidden');
            
            // Remove active classes from buttons
            const btns = ['btn-tab-encode', 'btn-tab-decode', 'btn-tab-history'];
            btns.forEach(id => {
                const b = document.getElementById(id);
                b.className = "flex items-center space-x-3 px-4 py-3.5 rounded-xl transition duration-250 border border-slate-800 text-slate-400 hover:text-slate-200 hover:bg-slate-900 font-semibold text-sm";
            });

            // Show selected panel
            document.getElementById('panel-' + tabId).classList.remove('hidden');
            
            // Add active classes to selected button
            const activeBtn = document.getElementById('btn-tab-' + tabId);
            activeBtn.className = "flex items-center space-x-3 px-4 py-3.5 rounded-xl transition duration-250 border border-sky-500/20 bg-sky-950/40 text-sky-400 font-semibold text-sm shadow-md shadow-sky-500/5";
            
            if (tabId === 'history') {
                loadHistoryList();
            }
        }

        function toggleEncryptSection(checkbox) {
            const container = document.getElementById('encode-password-container');
            if (checkbox.checked) {
                container.classList.remove('hidden');
            } else {
                container.classList.add('hidden');
            }
        }

        // Handle File Selection
        function handleEncodeImageSelect(input) {
            const file = input.files[0];
            if (!file) return;
            
            encodeFileName = file.name;
            const reader = new FileReader();

            reader.onload = function(e) {
                const b64 = e.target.result;
                encodeSelectedFileBase64 = b64;
                
                // Get image measurements to calculate capacity
                const img = new Image();
                img.onload = function() {
                    encodeImageWidth = img.width;
                    encodeImageHeight = img.height;
                    encodeImageDims = img.width + " x " + img.height + " px";
                    
                    document.getElementById('encode-fdims').innerText = encodeImageDims;
                    recalcCapacity();
                };
                img.src = b64;

                document.getElementById('encode-fname').innerText = encodeFileName;
                document.getElementById('encode-preview-img').src = b64;
                document.getElementById('encode-image-details').classList.remove('hidden');
                document.getElementById('encode-drag-box').classList.add('hidden');
            };
            reader.readAsDataURL(file);
        }

        function resetEncodeImage() {
            encodeSelectedFileBase64 = null;
            encodeFileName = "";
            encodeImageDims = "";
            encodeImageWidth = 0;
            encodeImageHeight = 0;
            document.getElementById('encode-file').value = "";
            document.getElementById('encode-image-details').classList.add('hidden');
            document.getElementById('encode-drag-box').classList.remove('hidden');
            
            document.getElementById('capacity-text').innerText = "No Image Loaded";
            document.getElementById('capacity-bar').style.width = "0%";
            document.getElementById('capacity-bar').className = "bg-sky-400 h-1.5 rounded-full w-0";
        }

        function recalcCapacity() {
            if (!encodeSelectedFileBase64 || encodeImageWidth === 0) {
                document.getElementById('capacity-text').innerText = "No Image Loaded";
                document.getElementById('capacity-bar').style.width = "0%";
                return;
            }

            const totalPixels = encodeImageWidth * encodeImageHeight;
            // Max bits = totalPixels * 3. So max bytes = (totalPixels * 3) / 8
            const maxCapacityBytes = Math.floor((totalPixels * 3) / 8);
            
            const message = document.getElementById('encode-msg').value;
            // Get UTF-8 exact byte weight
            const msgBytes = new TextEncoder().encode(message).length;
            
            // Header: MAGIC(3) + ENCRYPTED(1) + SIZE(4) = 8 bytes
            const totalRequiredBytes = msgBytes > 0 ? (8 + msgBytes) : 0;
            
            const percent = Math.min(100, Math.ceil((totalRequiredBytes / maxCapacityBytes) * 100)) || 0;
            
            document.getElementById('capacity-text').innerText = totalRequiredBytes + " / " + maxCapacityBytes + " Bytes used (" + percent + "%)";
            document.getElementById('capacity-bar').style.width = percent + "%";
            
            if (percent > 99) {
                document.getElementById('capacity-bar').className = "bg-rose-500 h-1.5 rounded-full transition-all duration-300";
            } else if (percent > 80) {
                document.getElementById('capacity-bar').className = "bg-amber-500 h-1.5 rounded-full transition-all duration-300";
            } else {
                document.getElementById('capacity-bar').className = "bg-emerald-400 h-1.5 rounded-full transition-all duration-300";
            }
        }

        function handleDecodeImageSelect(input) {
            const file = input.files[0];
            if (!file) return;
            
            decodeFileName = file.name;
            const reader = new FileReader();

            reader.onload = function(e) {
                const b64 = e.target.result;
                decodeSelectedFileBase64 = b64;
                
                const img = new Image();
                img.onload = function() {
                    decodeImageDims = img.width + " x " + img.height + " px";
                    document.getElementById('decode-fdims').innerText = decodeImageDims;
                };
                img.src = b64;

                document.getElementById('decode-fname').innerText = decodeFileName;
                document.getElementById('decode-preview-img').src = b64;
                document.getElementById('decode-image-details').classList.remove('hidden');
                document.getElementById('decode-drag-box').classList.add('hidden');
            };
            reader.readAsDataURL(file);
        }

        function resetDecodeImage() {
            decodeSelectedFileBase64 = null;
            decodeFileName = "";
            decodeImageDims = "";
            document.getElementById('decode-file').value = "";
            document.getElementById('decode-image-details').classList.add('hidden');
            document.getElementById('decode-drag-box').classList.remove('hidden');
            document.getElementById('decode-result').classList.add('hidden');
        }

        // Show Toast Notifications
        function alertToast(title, body, type = 'blue') {
            const container = document.getElementById('alert-box');
            container.classList.add('hidden');
            
            const content = document.getElementById('alert-content');
            const titleEl = document.getElementById('alert-title');
            const bodyEl = document.getElementById('alert-body');
            const iconContainer = document.getElementById('alert-icon-container');
            const iconEl = document.getElementById('alert-icon');
            
            titleEl.innerText = title;
            bodyEl.innerText = body;
            
            // set styles
            if (type === 'green') {
                content.className = "bg-slate-900 border border-emerald-500/30 rounded-2xl p-4 flex items-start space-x-3.5 shadow-2xl shadow-emerald-500/5";
                iconContainer.className = "p-1.5 px-2.5 rounded-xl bg-emerald-500/10 text-emerald-400 text-lg";
                iconEl.className = "fa-solid fa-circle-check";
            } else if (type === 'red') {
                content.className = "bg-slate-900 border border-rose-500/30 rounded-2xl p-4 flex items-start space-x-3.5 shadow-2xl shadow-rose-500/5";
                iconContainer.className = "p-1.5 px-2.5 rounded-xl bg-rose-500/10 text-rose-400 text-lg";
                iconEl.className = "fa-solid fa-circle-xmark";
            } else {
                content.className = "bg-slate-900 border border-sky-500/30 rounded-2xl p-4 flex items-start space-x-3.5 shadow-2xl shadow-sky-500/5";
                iconContainer.className = "p-1.5 px-2.5 rounded-xl bg-sky-500/10 text-sky-400 text-lg";
                iconEl.className = "fa-solid fa-circle-info";
            }
            
            container.classList.remove('hidden');
            container.classList.remove('translate-y-12');
            
            setTimeout(() => {
                container.classList.add('translate-y-12');
                setTimeout(() => { container.classList.add('hidden'); }, 300);
            }, 5000);
        }

        // Submissions
        function submitEncode() {
            if (!encodeSelectedFileBase64) {
                alertToast("Error", "Please load a raw lossless canvas first.", 'red');
                return;
            }
            const msg = document.getElementById('encode-msg').value;
            if (!msg || msg.trim().length === 0) {
                alertToast("Error", "Secret message content cannot be empty.", 'red');
                return;
            }
            const useEnc = document.getElementById('encode-encrypt-toggle').checked;
            const pass = document.getElementById('encode-password').value;
            if (useEnc && (!pass || pass.trim().length === 0)) {
                alertToast("Error", "Passphrase is required when cryptographic encryption is enabled.", 'red');
                return;
            }

            const format = encodeFileName.toUpperCase().endsWith('.BMP') ? 'BMP' : 'PNG';
            
            const btn = document.getElementById('btn-submit-encode');
            btn.disabled = true;
            btn.innerHTML = `<i class="fa-solid fa-spinner animate-spin"></i><span>Processing Pixels...</span>`;

            fetch('/api/encode', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    imageB64: encodeSelectedFileBase64,
                    fileName: encodeFileName,
                    message: msg,
                    password: pass,
                    useEncryption: useEnc,
                    format: format
                })
            })
            .then(res => res.json())
            .then(data => {
                btn.disabled = false;
                btn.innerHTML = `<i class="fa-solid fa-shield-virus"></i><span>Inject Secret Message & Process Pixels</span>`;
                
                if (data.success) {
                    alertToast("Success", "Steganographic encoding fully established!", 'green');
                    
                    // Populate result
                    document.getElementById('res-format').innerText = format;
                    document.getElementById('res-cap').innerText = data.capacity + " Bytes";
                    document.getElementById('res-size').innerText = data.requiredSize + " Bytes";
                    document.getElementById('res-crypt').innerText = useEnc ? "AES-GCM ENCRYPTED" : "PLAINTEXT";
                    document.getElementById('res-crypt').className = useEnc ? "mono font-semibold text-emerald-400" : "mono font-semibold text-amber-500";
                    
                    const dlink = document.getElementById('btn-download-stego');
                    dlink.href = data.encodedImageB64;
                    dlink.download = "Stego_WebSecure_" + Date.now() + "." + format.toLowerCase();
                    
                    document.getElementById('res-img').src = data.encodedImageB64;
                    document.getElementById('encode-result').classList.remove('hidden');
                    document.getElementById('encode-result').scrollIntoView({ behavior: 'smooth' });
                } else {
                    alertToast("Processing Failed", data.error || "Pixel calculation error occurs.", 'red');
                }
            })
            .catch(err => {
                btn.disabled = false;
                btn.innerHTML = `<i class="fa-solid fa-shield-virus"></i><span>Inject Secret Message & Process Pixels</span>`;
                alertToast("Connection Lost", "Failed to communicate with Android stego node: " + err, 'red');
            });
        }

        function submitDecode() {
            if (!decodeSelectedFileBase64) {
                alertToast("Error", "Please select a target stego image first.", 'red');
                return;
            }
            const pass = document.getElementById('decode-password').value;
            
            const btn = document.getElementById('btn-submit-decode');
            const spinner = document.getElementById('decode-spinner');
            const icon = document.getElementById('decode-icon');
            
            btn.disabled = true;
            spinner.classList.remove('hidden');
            icon.classList.add('hidden');

            fetch('/api/decode', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    imageB64: decodeSelectedFileBase64,
                    fileName: decodeFileName,
                    password: pass
                })
            })
            .then(res => res.json())
            .then(data => {
                btn.disabled = false;
                spinner.classList.add('hidden');
                icon.classList.remove('hidden');

                if (data.success) {
                    alertToast("Success", "Steganographic payload reconstructed!", 'green');
                    document.getElementById('decode-output-text').innerText = data.message;
                    document.getElementById('decode-result').classList.remove('hidden');
                    document.getElementById('decode-result').scrollIntoView({ behavior: 'smooth' });
                } else {
                    alertToast("Extraction Failed", data.error || "Authentication error or no valid signature.", 'red');
                }
            })
            .catch(err => {
                btn.disabled = false;
                spinner.classList.add('hidden');
                icon.classList.remove('hidden');
                alertToast("Connection Lost", "Host node unreachable: " + err, 'red');
            });
        }

        function copyExtractedText() {
            const text = document.getElementById('decode-output-text').innerText;
            navigator.clipboard.writeText(text).then(() => {
                alertToast("Copied", "Text copied to browser clipboard!", 'green');
            }).catch(() => {
                alertToast("Error", "Clipboard access denied.", 'red');
            });
        }

        function loadHistoryList() {
            const wrapper = document.getElementById('history-list');
            
            fetch('/api/history')
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    let html = `<div class="grid grid-cols-1 gap-4">`;
                    data.forEach(item => {
                        const dateStr = new Date(item.timestamp).toLocaleString();
                        const isEnc = item.isEncrypted;
                        const action = item.actionType;
                        
                        const bgClass = action === "HIDE" ? "bg-sky-950/20 border-sky-950/60" : "bg-emerald-950/20 border-emerald-950/60";
                        const textClass = action === "HIDE" ? "text-sky-400" : "text-emerald-400";
                        const iconClass = action === "HIDE" ? "fa-solid fa-compress" : "fa-solid fa-expand";
                        
                        html += `
                            <div class="border ` + bgClass + ` rounded-2xl p-5 flex flex-col md:flex-row md:items-center justify-between gap-4 transition hover:bg-slate-900/40">
                                <div class="flex items-start space-x-4">
                                    <div class="p-2.5 rounded-xl bg-slate-950 border border-slate-800 ` + textClass + `">
                                        <i class="` + iconClass + ` text-lg"></i>
                                    </div>
                                    <div>
                                        <div class="flex items-center space-x-2">
                                            <span class="text-xs font-bold uppercase tracking-wider ` + textClass + `">` + action + `</span>
                                            <span class="text-[10px] text-slate-500">•</span>
                                            <span class="text-[10px] text-slate-400 font-medium">` + dateStr + `</span>
                                        </div>
                                        <h4 class="text-sm font-semibold text-slate-100 mt-1 truncate max-w-xs md:max-w-md">` + item.imageName + `</h4>
                                        <p class="text-xs text-slate-400 mt-1">` + item.details + `</p>
                                    </div>
                                </div>
                                <div class="flex items-center justify-between md:justify-end gap-4 min-w-48 border-t border-slate-800/40 pt-3 md:border-none md:pt-0">
                                    <div class="flex flex-col space-y-1">
                                        <span class="text-[10px] uppercase font-bold text-slate-500 mr-1 tracking-wider">Payload Weight:</span>
                                        <span class="mono text-slate-300 text-xs font-semibold">` + item.payloadSize + ` bytes</span>
                                    </div>
                                    <div class="flex flex-col space-y-1 text-right">
                                        <span class="text-[10px] uppercase font-bold text-slate-500 tracking-wider">State:</span>
                                        <span class="text-xs font-bold flex items-center space-x-1.5 justify-end">
                                            ` + (isEnc ? `<i class="fa-solid fa-lock text-[10px] text-emerald-400"></i><span class="text-emerald-400 uppercase tracking-widest text-[10px] font-bold">Encrypted</span>` : `<i class="fa-solid fa-lock-open text-[10px] text-amber-500"></i><span class="text-amber-500 uppercase tracking-widest text-[10px] font-bold">Plaintext</span>`) + `
                                        </span>
                                    </div>
                                </div>
                            </div>
                        `;
                    });
                    html += `</div>`;
                    wrapper.innerHTML = html;
                } else {
                    wrapper.innerHTML = `
                        <div class="text-slate-500 text-center py-16 border border-dashed border-slate-800 rounded-3xl">
                            <i class="fa-solid fa-folder-open text-3xl mb-3 block text-slate-700"></i>
                            <h3 class="font-bold text-slate-400 text-sm">Vault Logs Empty</h3>
                            <p class="text-xs text-slate-500 mt-1">Activities logged locally from either Web or Android device show up here.</p>
                        </div>
                    `;
                }
            })
            .catch(err => {
                wrapper.innerHTML = `
                    <div class="text-rose-400 text-center py-12 border border-rose-950/40 rounded-3xl bg-rose-950/5/10">
                        <i class="fa-solid fa-triangle-exclamation text-3xl mb-3 block"></i>
                        <h3 class="font-bold text-sm">Failed to retrieve audit trail</h3>
                        <p class="text-xs text-rose-500/85 mt-1">Host connection unreachable or database node exception. ` + err + `</p>
                    </div>
                `;
            });
        }

        function clearHistory() {
            if (!confirm("Are you fully certain to wipe all database audit trails permanently? This action cannot be reversed.")) {
                return;
            }

            fetch('/api/history/clear', { method: 'POST' })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    alertToast("Success", "Local SQLite audit trail wiped completely.", 'green');
                    loadHistoryList();
                } else {
                    alertToast("Error", "Failed to clear repository logs.", 'red');
                }
            })
            .catch(err => {
                alertToast("Error", "Database unreachable: " + err, 'red');
            });
        }
    </script>
</body>
</html>
""".trimIndent()
}
