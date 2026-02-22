package com.example.scanner.data.di

import android.content.Context
import androidx.room.Room
import com.example.scanner.data.local.AppDatabase
import com.example.scanner.data.local.dao.ArticleDao
import com.example.scanner.data.local.dao.BookingReasonDao
import com.example.scanner.data.local.dao.EmployeeDao
import com.example.scanner.data.local.dao.MaterialDao
import com.example.scanner.data.local.dao.ScanProcessDao
import com.example.scanner.data.local.dao.ScannedItemDao
import com.example.scanner.data.local.dao.SqlLogDao
import com.example.scanner.data.local.dao.WarehouseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "scanner_database"
        ).build()
    }

    @Provides
    fun provideEmployeeDao(appDatabase: AppDatabase): EmployeeDao = appDatabase.employeeDao()

    @Provides
    fun provideArticleDao(appDatabase: AppDatabase): ArticleDao = appDatabase.articleDao()

    @Provides
    fun provideMaterialDao(appDatabase: AppDatabase): MaterialDao = appDatabase.materialDao()

    @Provides
    fun provideWarehouseDao(appDatabase: AppDatabase): WarehouseDao = appDatabase.warehouseDao()

    @Provides
    fun provideBookingReasonDao(appDatabase: AppDatabase): BookingReasonDao = appDatabase.bookingReasonDao()

    @Provides
    fun provideScanProcessDao(appDatabase: AppDatabase): ScanProcessDao = appDatabase.scanProcessDao()

    @Provides
    fun provideScannedItemDao(appDatabase: AppDatabase): ScannedItemDao = appDatabase.scannedItemDao()

    @Provides
    fun provideSqlLogDao(appDatabase: AppDatabase): SqlLogDao = appDatabase.sqlLogDao()
}
