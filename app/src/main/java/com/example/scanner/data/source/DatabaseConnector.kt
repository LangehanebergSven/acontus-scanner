package com.example.scanner.data.source

import com.example.scanner.data.model.Article
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.Employee
import com.example.scanner.data.model.Material
import com.example.scanner.data.model.Warehouse

/**
 * Interface for direct database connections.
 */
interface DatabaseConnector {
    suspend fun executeQuery(sql: String): Boolean
    
    suspend fun fetchWarehouses(): List<Warehouse>
    suspend fun fetchBookingReasons(): List<BookingReason>
    suspend fun fetchArticles(): List<Article>
    suspend fun fetchMaterials(): List<Material>
    suspend fun fetchEmployees(): List<Employee>
}