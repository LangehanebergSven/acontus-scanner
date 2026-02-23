package com.example.scanner.ui.scanning

import java.util.Date

data class ScannedItemUi(
    val id: Long,
    val itemId: String,
    val itemName: String,
    val itemType: String, // "Artikel" or "Material"
    val quantity: Int,
    val scannedAt: Date,
    // Configuration details
    val warehouseName: String,
    val bookingReasonName: String,
    val batchNumber: String?,
    val bestBeforeDate: String? // Formatted date
)
