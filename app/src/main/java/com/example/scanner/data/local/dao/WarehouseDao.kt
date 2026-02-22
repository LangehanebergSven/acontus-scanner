package com.example.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.scanner.data.model.Warehouse

@Dao
interface WarehouseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(warehouses: List<Warehouse>)

    @Query("SELECT * FROM warehouses")
    suspend fun getAll(): List<Warehouse>

    @Query("DELETE FROM warehouses")
    suspend fun clearAll()
}
