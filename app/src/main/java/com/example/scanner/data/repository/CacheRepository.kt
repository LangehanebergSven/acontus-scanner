package com.example.scanner.data.repository

import com.example.scanner.data.local.dao.ArticleDao
import com.example.scanner.data.local.dao.EmployeeDao
import com.example.scanner.data.local.dao.MaterialDao
import com.example.scanner.data.local.dao.WarehouseDao
import com.example.scanner.data.local.dao.BookingReasonDao
import java.util.concurrent.TimeUnit

/**
 * Repository to manage the master data cache (48-hour expiration).
 */
class CacheRepository(
    private val employeeDao: EmployeeDao,
    private val articleDao: ArticleDao,
    private val materialDao: MaterialDao,
    private val warehouseDao: WarehouseDao,
    private val bookingReasonDao: BookingReasonDao
    // private val remoteDataSource: RemoteDataSource // To be added later
) {
    // Dummy timestamp for the last sync. In a real app, this would be persisted.
    private var lastSyncTimestamp: Long = 0

    private fun isCacheExpired(): Boolean {
        val fortyEightHoursInMillis = TimeUnit.HOURS.toMillis(48)
        return (System.currentTimeMillis() - lastSyncTimestamp) > fortyEightHoursInMillis
    }

    suspend fun invalidateCache() {
        employeeDao.clearAll()
        articleDao.clearAll()
        materialDao.clearAll()
        warehouseDao.clearAll()
        bookingReasonDao.clearAll()
        lastSyncTimestamp = 0 // Force a new sync on next data request
    }

    // Example function for fetching employees
    // This would be repeated for Articles, Materials, etc.
    /*
    suspend fun getEmployees(): List<Employee> {
        if (isCacheExpired()) {
            // Fetch from remote source
            val remoteEmployees = remoteDataSource.fetchEmployees()
            // Clear old cache and insert new data
            employeeDao.clearAll()
            employeeDao.insertAll(remoteEmployees)
            lastSyncTimestamp = System.currentTimeMillis()
        }
        return employeeDao.getAll()
    }
    */
}
