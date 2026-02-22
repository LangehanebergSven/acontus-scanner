package com.example.scanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "scanned_items")
data class ScannedItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scanProcessId: Long,
    val articleId: String?,
    val materialId: String?,
    val quantity: Int,
    val contentQuantity: Int?,
    val bestBeforeDate: Date?,
    val batchNumber: String?,
    val warehouseId: String,
    val bookingReasonId: String,
    val scannedAt: Date = Date()
)
