package com.example.scanner.data.source

/**
 * Interface for direct database connections.
 * This acts as a stub for the actual implementation that will handle
 * raw SQL queries, connection management, and manual data mapping.
 */
interface DatabaseConnector {

    /**
     * Executes a given SQL query.
     *
     * @param sql The raw SQL query to be executed.
     * @return A boolean indicating whether the execution was successful.
     *         For offline logging, this might always return true and handle
     *         failures internally.
     */
    suspend fun executeQuery(sql: String): Boolean

    // Here you would add methods for fetching data, e.g.:
    // suspend fun <T> fetch(query: String, mapper: (ResultSet) -> T): List<T>
}
