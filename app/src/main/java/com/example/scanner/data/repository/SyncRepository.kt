package com.example.scanner.data.repository

import com.example.scanner.data.model.ScannedItem
import com.example.scanner.data.source.DatabaseConnector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Repository for handling synchronization.
 */
class SyncRepository @Inject constructor(
    private val databaseConnector: DatabaseConnector
) {
    private val sqlDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMANY)

    /**
     * Submits a scanned item to the remote database.
     * Throws an Exception if the direct connection fails.
     */
    suspend fun submitScannedItem(item: ScannedItem, employeeId: String) {
        val sql = generateInsertSql(item, employeeId)
        val success = databaseConnector.executeQuery(sql)
        
        if (!success) {
            throw Exception("Fehler beim Einfügen in Datenbank")
        }
    }

    /**
     * Helper to retry sync logic if needed, but no offline logs anymore.
     * Kept for interface compatibility or can be removed if unused.
     * Currently simply returns 0 as no logs are processed.
     */
    suspend fun uploadOfflineLogs(): Int {
        return 0
    }

    private fun generateInsertSql(item: ScannedItem, employeeId: String): String {
        val tableName = "ERP_WaWi_Scans_New"
        
        val articleId = item.articleId?.let { "'$it'" } ?: "NULL"
        val materialId = item.materialId?.let { "'$it'" } ?: "NULL"
        val itemType = if (item.articleId != null) "Article" else "Material"
        val warehouseId = "'${item.warehouseId}'"
        val bookingReasonId = "'${item.bookingReasonId}'"
        val batchStr = item.batchNumber?.let { "'$it'" } ?: "NULL"
        val mhdFormatted = item.bestBeforeDate?.let { "'${sqlDateFormatter.format(it)}'" } ?: "NULL"
        val quantity = item.quantity
        val contentQuantity = item.contentQuantity ?: "NULL"
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
                GebuchtAm,
                Timestamp,
                SyncTimestamp
            ) VALUES (
                $articleId,
                $materialId,
                '$itemType',
                '$employeeId',
                $warehouseId,
                $bookingReasonId,
                $batchStr,
                $mhdFormatted,
                $quantity,
                $contentQuantity,
                NULL,
                '$scannedAt',
                '$now'
            )
        """.trimIndent()
    }
}