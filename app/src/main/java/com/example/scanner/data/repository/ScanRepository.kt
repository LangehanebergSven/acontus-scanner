package com.example.scanner.data.repository

import com.example.scanner.data.local.dao.ScanProcessDao
import com.example.scanner.data.local.dao.ScannedItemDao
import com.example.scanner.data.model.ScanProcess
import com.example.scanner.data.model.ScannedItem
import javax.inject.Inject

/**
 * Repository for managing local scan sessions and scanned items.
 */
class ScanRepository @Inject constructor(
    private val scanProcessDao: ScanProcessDao,
    private val scannedItemDao: ScannedItemDao
) {

    suspend fun startNewProcess(
        employeeId: String,
        warehouseId: String,
        bookingReasonId: String
    ): Long {
        val newProcess = ScanProcess(
            employeeId = employeeId,
            warehouseId = warehouseId,
            bookingReasonId = bookingReasonId
        )
        return scanProcessDao.insert(newProcess)
    }

    suspend fun getProcessById(processId: Long): ScanProcess? {
        return scanProcessDao.getById(processId)
    }

    suspend fun getLatestProcessForEmployee(employeeId: String): ScanProcess? {
        return scanProcessDao.getLatestProcessForEmployee(employeeId)
    }

    suspend fun getScannedItemsForProcess(processId: Long): List<ScannedItem> {
        return scannedItemDao.getItemsForProcess(processId)
    }

    suspend fun addScannedItem(item: ScannedItem) {
        scannedItemDao.insert(item)
    }

    suspend fun updateScannedItem(item: ScannedItem) {
        scannedItemDao.update(item)
    }

    suspend fun deleteScannedItems(items: List<ScannedItem>) {
        // Assuming ScannedItem has a unique 'id' field
        val itemIds = items.map { it.id }
        scannedItemDao.deleteItemsByIds(itemIds)
    }
}
