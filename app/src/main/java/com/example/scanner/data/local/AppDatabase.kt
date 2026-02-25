package com.example.scanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.scanner.data.local.dao.*
import com.example.scanner.data.model.*

@Database(
    entities = [
        Employee::class,
        Article::class,
        Material::class,
        Warehouse::class,
        BookingReason::class,
        ScanProcess::class,
        ScannedItem::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun articleDao(): ArticleDao
    abstract fun materialDao(): MaterialDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun bookingReasonDao(): BookingReasonDao
    abstract fun scanProcessDao(): ScanProcessDao
    abstract fun scannedItemDao(): ScannedItemDao
}
