package com.example.scanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val employeeId: String, // Technical ID from ERP
    val employeeLoginNumber: String,    // Login number (formerly HemmePersonalNr)
    val name: String
)