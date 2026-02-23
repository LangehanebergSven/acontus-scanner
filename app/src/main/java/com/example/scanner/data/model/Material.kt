package com.example.scanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class Material(
    @PrimaryKey val materialId: String,
    val name: String,
    val ean: String? = null
)