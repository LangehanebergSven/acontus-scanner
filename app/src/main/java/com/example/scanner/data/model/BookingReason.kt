package com.example.scanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "booking_reasons")
data class BookingReason(
    @PrimaryKey val bookingReasonId: String,
    val reason: String,
    val type: String,        // "Artikel" or "Material"
    val movementType: String // e.g., "Eingang", "Ausgang", "Inventur"
)
