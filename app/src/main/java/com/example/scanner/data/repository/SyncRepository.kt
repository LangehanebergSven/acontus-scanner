package com.example.scanner.data.repository

import com.example.scanner.data.local.dao.SqlLogDao
import com.example.scanner.data.model.SqlLog
import com.example.scanner.data.source.DatabaseConnector

/**
 * Repository for handling offline SQL query logging and synchronization.
 */
class SyncRepository(
    private val sqlLogDao: SqlLogDao,
    private val databaseConnector: DatabaseConnector
) {

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
