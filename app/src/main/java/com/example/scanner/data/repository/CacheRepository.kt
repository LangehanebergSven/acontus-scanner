package com.example.scanner.data.repository

import com.example.scanner.data.local.dao.ArticleDao
import com.example.scanner.data.local.dao.EmployeeDao
import com.example.scanner.data.local.dao.MaterialDao
import com.example.scanner.data.local.dao.WarehouseDao
import com.example.scanner.data.local.dao.BookingReasonDao
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.Warehouse
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Repository to manage the master data cache (48-hour expiration).
 */
class CacheRepository @Inject constructor(
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

    suspend fun getWarehouses(): List<Warehouse> {
        // In a real app, here you would check isCacheExpired() and fetch from a remote source
        // For now, we just add dummy data if the DB is empty.
        if (warehouseDao.getAll().isEmpty()) {
            addDummyWarehouses()
        }
        return warehouseDao.getAll()
    }

    suspend fun getWarehouseById(warehouseId: String): Warehouse? {
        return warehouseDao.getById(warehouseId)
    }

    suspend fun getBookingReasons(): List<BookingReason> {
        if (bookingReasonDao.getAll().isEmpty()) {
            addDummyBookingReasons()
        }
        return bookingReasonDao.getAll()
    }

    suspend fun getBookingReasonById(bookingReasonId: String): BookingReason? {
        return bookingReasonDao.getById(bookingReasonId)
    }

    private suspend fun addDummyWarehouses() {
        val dummyWarehouses = listOf(
            Warehouse("W01", "Hauptlager"),
            Warehouse("W02", "Produktionslager"),
            Warehouse("W03", "Versandlager")
        )
        warehouseDao.insertAll(dummyWarehouses)
    }

    private suspend fun addDummyBookingReasons() {
        val dummyReasons = listOf(
            BookingReason("B01", "Wareneingang"),
            BookingReason("B02", "Warenausgang"),
            BookingReason("B03", "Umlagerung"),
            BookingReason("B04", "Inventur")
        )
        bookingReasonDao.insertAll(dummyReasons)
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
