package com.example.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.scanner.data.model.ScanProcess

@Dao
interface ScanProcessDao {
    @Insert
    suspend fun insert(scanProcess: ScanProcess): Long

    @Update
    suspend fun update(scanProcess: ScanProcess)

    @Query("SELECT * FROM scan_processes WHERE id = :id")
    suspend fun getById(id: Long): ScanProcess?

    @Query("SELECT * FROM scan_processes WHERE employeeId = :employeeId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestProcessForEmployee(employeeId: String): ScanProcess?
}
