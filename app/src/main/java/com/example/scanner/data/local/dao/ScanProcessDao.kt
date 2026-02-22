package com.example.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.scanner.data.model.ScanProcess

@Dao
interface ScanProcessDao {
    @Insert
    suspend fun insert(scanProcess: ScanProcess): Long

    @Query("SELECT * FROM scan_processes WHERE id = :processId")
    suspend fun getById(processId: Long): ScanProcess?

    @Query("SELECT * FROM scan_processes WHERE employeeId = :employeeId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestProcessForEmployee(employeeId: String): ScanProcess?
}
