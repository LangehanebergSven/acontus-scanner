package com.example.scanner.data.di

import com.example.scanner.data.local.dao.ScanProcessDao
import com.example.scanner.data.local.dao.ScannedItemDao
import com.example.scanner.data.local.dao.SqlLogDao
import com.example.scanner.data.repository.ScanRepository
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
    fun provideScanRepository(
        scanProcessDao: ScanProcessDao,
        scannedItemDao: ScannedItemDao
    ): ScanRepository {
        return ScanRepository(scanProcessDao, scannedItemDao)
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
