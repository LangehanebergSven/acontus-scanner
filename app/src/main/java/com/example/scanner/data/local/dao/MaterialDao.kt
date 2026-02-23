package com.example.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.scanner.data.model.Material

@Dao
interface MaterialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materials: List<Material>)

    @Query("SELECT * FROM materials WHERE materialId = :materialId")
    suspend fun getMaterialById(materialId: String): Material?

    @Query("SELECT * FROM materials WHERE ean = :ean")
    suspend fun getMaterialByEan(ean: String): Material?

    @Query("SELECT * FROM materials WHERE name LIKE '%' || :query || '%' OR materialId LIKE '%' || :query || '%' OR ean LIKE '%' || :query || '%'")
    suspend fun searchMaterials(query: String): List<Material>

    @Query("SELECT COUNT(*) FROM materials")
    suspend fun getCount(): Int

    @Query("DELETE FROM materials")
    suspend fun clearAll()
}