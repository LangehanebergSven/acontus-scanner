package com.example.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.scanner.data.model.BookingReason

@Dao
interface BookingReasonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookingReasons: List<BookingReason>)

    @Query("SELECT * FROM booking_reasons")
    suspend fun getAll(): List<BookingReason>

    @Query("DELETE FROM booking_reasons")
    suspend fun clearAll()
}
