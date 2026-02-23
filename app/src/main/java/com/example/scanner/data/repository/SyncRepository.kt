package com.example.scanner.data.repository

import android.util.Log
import com.example.scanner.data.local.dao.SqlLogDao
import com.example.scanner.data.model.ScannedItem
import com.example.scanner.data.model.SqlLog
import com.example.scanner.data.source.DatabaseConnector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository for handling offline SQL query logging and synchronization.
 */
class SyncRepository(
    private val sqlLogDao: SqlLogDao,
    private val databaseConnector: DatabaseConnector
) {
    private val sqlDateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)

    /**
     * Submits a scanned item to the remote database.
     * If the direct connection fails, the query is logged for offline synchronization.
     */
    suspend fun submitScannedItem(item: ScannedItem, employeeId: String) {
        val sql = generateInsertSql(item, employeeId)
        val success = databaseConnector.executeQuery(sql)
        
        if (!success) {
            Log.w("SyncRepository", "Direct execution failed, query logged for offline sync: $sql")
            logQuery(sql)
        }
    }

    private fun generateInsertSql(item: ScannedItem, employeeId: String): String {
        // TODO: Replace with the actual table name and column names of your ERP system
        val tableName = "ERP_WaWi_Scans_New"
        
        val articleId = item.articleId?.let { "'$it'" } ?: "NULL"
        val materialId = item.materialId?.let { "'$it'" } ?: "NULL"
        val itemType = if (item.articleId != null) "Article" else "Material"
        val warehouseId = item.warehouseId
        val bookingReasonId = item.bookingReasonId
        val batchStr = item.batchNumber?.let { "'$it'" } ?: "NULL"
        val mhdFormatted = item.bestBeforeDate?.let { "'${sqlDateFormatter.format(it)}'" } ?: "NULL"
        val quantity = item.quantity
        val contentQuantity = "NULL"
        val booked = "0"
        val scannedAt = sqlDateFormatter.format(item.scannedAt)
        val now = sqlDateFormatter.format(Date())

        return """
            INSERT INTO $tableName (
                ArtikelNr, 
                MaterialNr, 
                Typ, 
                PersonalNr, 
                LagerID, 
                BuchungsgrundID, 
                Charge, 
                MHD, 
                Menge,
                Inhaltsmenge,
                gebucht,
                timestamp,
                syncTimestamp
            ) VALUES (
                $articleId,
                $materialId,
                '$itemType',
                $employeeId,
                $warehouseId,
                $bookingReasonId,
                '$batchStr',
                '$mhdFormatted',
                $quantity,
                $contentQuantity,
                $booked,
                '$scannedAt',
                '$now'
            )
        """.trimIndent()
    }

    /**
     * Logs an SQL query for later execution.
     */
    suspend fun logQuery(sql: String) {
        val log = SqlLog(sqlQuery = sql)
        sqlLogDao.insert(log)
    }

    /**
     * Attempts to synchronize all logged queries with the remote database.
     * If a query is successful, it's removed from the log.
     *
     * @return The number of successfully executed queries.
     */
    suspend fun synchronizePendingQueries(): Int {
        val pendingLogs = sqlLogDao.getAll()
        val successfulLogIds = mutableListOf<Long>()
        var successCount = 0

        for (log in pendingLogs) {
            val success = databaseConnector.executeQuery(log.sqlQuery)
            if (success) {
                successfulLogIds.add(log.id)
                successCount++
            }
        }

        if (successfulLogIds.isNotEmpty()) {
            sqlLogDao.deleteLogsByIds(successfulLogIds)
        }

        return successCount
    }
}