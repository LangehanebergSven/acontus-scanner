package com.example.scanner.data.di

import com.example.scanner.data.local.dao.ArticleDao
import com.example.scanner.data.local.dao.BookingReasonDao
import com.example.scanner.data.local.dao.EmployeeDao
import com.example.scanner.data.local.dao.MaterialDao
import com.example.scanner.data.local.dao.SqlLogDao
import com.example.scanner.data.local.dao.WarehouseDao
import com.example.scanner.data.repository.CacheRepository
import com.example.scanner.data.repository.SyncRepository
import com.example.scanner.data.source.DatabaseConnector
import com.example.scanner.data.source.DatabaseConnectorImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDatabaseConnector(): DatabaseConnector {
        return DatabaseConnectorImpl()
    }

    @Provides
    @Singleton
    fun provideCacheRepository(
        employeeDao: EmployeeDao,
        articleDao: ArticleDao,
        materialDao: MaterialDao,
        warehouseDao: WarehouseDao,
        bookingReasonDao: BookingReasonDao
    ): CacheRepository {
        return CacheRepository(employeeDao, articleDao, materialDao, warehouseDao, bookingReasonDao)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        sqlLogDao: SqlLogDao,
        databaseConnector: DatabaseConnector
    ): SyncRepository {
        return SyncRepository(sqlLogDao, databaseConnector)
    }
}
