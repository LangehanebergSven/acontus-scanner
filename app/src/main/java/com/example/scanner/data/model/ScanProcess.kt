package com.example.scanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "scan_processes")
data class ScanProcess(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val warehouseId: String,
    val bookingReasonId: String,
    val createdAt: Date = Date()
)
