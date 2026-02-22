package com.example.scanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sql_logs")
data class SqlLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sqlQuery: String,
    val createdAt: Date = Date()
)
