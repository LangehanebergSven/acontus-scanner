package com.example.scanner.scanner

import kotlinx.coroutines.flow.StateFlow

interface BarcodeScanner {
    val scannedBarcode: StateFlow<String?>
    fun startScanning()
    fun stopScanning()
    fun release()
}
