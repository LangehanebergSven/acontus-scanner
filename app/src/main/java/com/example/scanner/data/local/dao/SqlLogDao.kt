package com.example.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.scanner.data.model.SqlLog

@Dao
interface SqlLogDao {
    @Insert
    suspend fun insert(log: SqlLog)

    @Query("SELECT * FROM sql_logs ORDER BY createdAt ASC")
    suspend fun getAll(): List<SqlLog>

    @Query("DELETE FROM sql_logs WHERE id IN (:logIds)")
    suspend fun deleteLogsByIds(logIds: List<Long>)
}
