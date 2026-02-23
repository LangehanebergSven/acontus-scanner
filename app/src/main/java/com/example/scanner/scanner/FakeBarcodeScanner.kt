package com.example.scanner.scanner

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class FakeBarcodeScanner @Inject constructor() : BarcodeScanner {
    private val _scannedBarcode = MutableStateFlow<String?>(null)
    override val scannedBarcode: StateFlow<String?> = _scannedBarcode

    override fun startScanning() {
        // Simulate a scan after 2 seconds
        // In a real implementation, this would be where the Keyence SDK is initialized
    }

    override fun stopScanning() {
        // In a real implementation, this would be where the Keyence SDK is de-initialized
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    // This method is for testing purposes only
    fun emitBarcode(barcode: String) {
        _scannedBarcode.value = barcode
    }
}
