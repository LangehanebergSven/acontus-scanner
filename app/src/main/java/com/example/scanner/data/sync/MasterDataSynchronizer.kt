package com.example.scanner.data.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.scanner.data.local.dao.ArticleDao
import com.example.scanner.data.local.dao.BookingReasonDao
import com.example.scanner.data.local.dao.EmployeeDao
import com.example.scanner.data.local.dao.MaterialDao
import com.example.scanner.data.local.dao.WarehouseDao
import com.example.scanner.data.model.Article
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.Employee
import com.example.scanner.data.model.Material
import com.example.scanner.data.model.Warehouse
import com.example.scanner.data.source.DatabaseConnector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterDataSynchronizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val articleDao: ArticleDao,
    private val materialDao: MaterialDao,
    private val warehouseDao: WarehouseDao,
    private val bookingReasonDao: BookingReasonDao,
    private val employeeDao: EmployeeDao,
    private val databaseConnector: DatabaseConnector
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("scanner_prefs", Context.MODE_PRIVATE)
    private val KEY_LAST_SYNC = "last_master_data_sync"

    fun isSyncNeeded(): Boolean {
        val lastSync = prefs.getLong(KEY_LAST_SYNC, 0L)
        val fortyEightHoursInMillis = TimeUnit.HOURS.toMillis(48)
        return (System.currentTimeMillis() - lastSync) > fortyEightHoursInMillis
    }

    suspend fun syncIfNeeded() {
        if (isSyncNeeded()) {
            performSync()
        } else {
            // Check if DB is empty (first start case or cleared data) or if master data is missing
            if (warehouseDao.getAll().isEmpty() || articleDao.getCount() == 0 || materialDao.getCount() == 0) {
                performSync()
            }
        }
    }

    suspend fun performSync() = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch from Remote ERP Database via MSSQL JDBC
            val warehouses = databaseConnector.fetchWarehouses()
            val bookingReasons = databaseConnector.fetchBookingReasons()
            val articles = databaseConnector.fetchArticles()
            val materials = databaseConnector.fetchMaterials()
            val employees = databaseConnector.fetchEmployees()

            // 2. Clear local cache
            clearAllData()

            // 3. Insert fresh data from ERP into local DB
            if (warehouses.isNotEmpty()) warehouseDao.insertAll(warehouses)
            if (bookingReasons.isNotEmpty()) bookingReasonDao.insertAll(bookingReasons)
            if (articles.isNotEmpty()) articleDao.insertAll(articles)
            if (materials.isNotEmpty()) materialDao.insertAll(materials)
            if (employees.isNotEmpty()) employeeDao.insertAll(employees)

            // 4. Update timestamp
            prefs.edit { putLong(KEY_LAST_SYNC, System.currentTimeMillis()) }
            Log.i("MasterDataSynchronizer", "Master data synchronization successful.")
        } catch (e: Exception) {
            Log.e("MasterDataSynchronizer", "Master data synchronization failed.", e)
            // Fallback could be implemented here or left as is to retry later
        }
    }

    private suspend fun clearAllData() {
        employeeDao.clearAll()
        articleDao.clearAll()
        materialDao.clearAll()
        warehouseDao.clearAll()
        bookingReasonDao.clearAll()
    }
}