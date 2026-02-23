package com.example.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.scanner.data.model.ScannedItem

@Dao
interface ScannedItemDao {
    @Insert
    suspend fun insert(item: ScannedItem): Long

    @Update
    suspend fun update(item: ScannedItem)

    @Delete
    suspend fun delete(item: ScannedItem)

    @Query("SELECT * FROM scanned_items WHERE scanProcessId = :processId ORDER BY scannedAt DESC")
    suspend fun getItemsForProcess(processId: Long): List<ScannedItem>

    @Query("DELETE FROM scanned_items WHERE id IN (:itemIds)")
    suspend fun deleteItemsByIds(itemIds: List<Long>)
}
