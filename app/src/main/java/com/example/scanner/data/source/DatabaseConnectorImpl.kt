package com.example.scanner.data.source

import javax.inject.Inject

// This is a dummy implementation for now.
// In a real app, this would contain the logic to connect to the remote database.
class DatabaseConnectorImpl @Inject constructor() : DatabaseConnector {
    override suspend fun executeQuery(sql: String): Boolean {
        // Simulate network delay
        kotlinx.coroutines.delay(500)
        // For now, assume success
        return true
    }
}
