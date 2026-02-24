package com.example.scanner.data.source

import android.util.Log
import com.example.scanner.data.model.Article
import com.example.scanner.data.model.BookingReason
import com.example.scanner.data.model.Employee
import com.example.scanner.data.model.Material
import com.example.scanner.data.model.Warehouse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.DriverManager
import java.sql.ResultSet
import javax.inject.Inject

class DatabaseConnectorImpl @Inject constructor() : DatabaseConnector {

    // jtds is very old, but works. the modern jdbc driver for mssql doesn't work
    private val dbUrl = "jdbc:jtds:sqlserver://192.168.2.3:1433/Daten_Hemme_Schmargendorf_251221;ssl=require"
    private val user = "daten_user"
    private val password = "\$Axyzwert123"

    init {        // Explicitly load the driver
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
        } catch (e: Exception) {
            Log.e("DatabaseConnector", "Error loading JDBC driver: ${e.message}")
        }
    }

    private suspend fun <T> executeSelect(query: String, mapper: (ResultSet) -> T): List<T> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<T>()
        try {
            DriverManager.getConnection(dbUrl, user, password).use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery(query).use { resultSet ->
                        while (resultSet.next()) {
                            resultList.add(mapper(resultSet))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DatabaseConnector", "Error executing query: $query", e)
        }
        return@withContext resultList
    }

    override suspend fun executeQuery(sql: String): Boolean = withContext(Dispatchers.IO) {
        try {
            DriverManager.getConnection(dbUrl, user, password).use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeUpdate(sql)
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            Log.e("DatabaseConnector", "Error executing update: $sql", e)
            return@withContext false
        }
    }

    override suspend fun fetchWarehouses(): List<Warehouse> {
        val query = "SELECT LagerId, Lagerbezeichnung FROM WarenwirtschaftLagerstammdaten where Aktiv = 1"
        return executeSelect(query) { rs ->
            Warehouse(
                warehouseId = rs.getString("LagerId") ?: "",
                name = rs.getString("Lagerbezeichnung") ?: ""
            )
        }
    }

    override suspend fun fetchBookingReasons(): List<BookingReason> {
        val query = "SELECT ERP_WaWi_Buchungsgrund.id, ERP_WaWi_Buchungsgrund.Name, ERP_WaWi_Typ.Name AS Typ, ERP_WaWi_Bewegungsart.Name AS Bewegung " +
                "FROM ERP_WaWi_Buchungsgrund " +
                "JOIN ERP_WaWi_Typ ON ERP_WaWi_Buchungsgrund.TypId = ERP_WaWi_Typ.Id " +
                "JOIN ERP_WaWi_Bewegungsart ON ERP_WaWi_Bewegungsart.Id = ERP_WaWi_Buchungsgrund.BewegungsartId"

        return executeSelect(query) { rs ->
            BookingReason(
                bookingReasonId = rs.getString("id") ?: "",
                reason = rs.getString("Name") ?: ""
            )
        }
    }

    override suspend fun fetchArticles(): List<Article> {
        val query = "SELECT ArtikelNr, Artikel, EAN FROM Artikel WHERE vorhanden = 1"
        return executeSelect(query) { rs ->
            Article(
                articleId = rs.getString("ArtikelNr") ?: "",
                name = rs.getString("Artikel") ?: "",
                ean = rs.getString("EAN")
            )
        }
    }

    override suspend fun fetchMaterials(): List<Material> {
        val query = "SELECT MaterialNr, Materialbezeichnung, EAN FROM Materialtabelle"
        return executeSelect(query) { rs ->
            Material(
                materialId = rs.getString("MaterialNr") ?: "",
                name = rs.getString("Materialbezeichnung") ?: "",
                ean = rs.getString("EAN")
            )
        }
    }

    override suspend fun fetchEmployees(): List<Employee> {
        val query = "SELECT PersonalNr, HemmePersonalNr, (Vorname + ' ' + Nachname) AS Name FROM Personal"
        return executeSelect(query) { rs ->
            Employee(
                employeeId = rs.getString("PersonalNr") ?: "",
                employeeLoginNumber = rs.getString("HemmePersonalNr") ?: "",
                name = rs.getString("Name") ?: ""
            )
        }
    }
}