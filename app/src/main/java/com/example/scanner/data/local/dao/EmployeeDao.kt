package com.example.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.scanner.data.model.Employee

@Dao
interface EmployeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(employees: List<Employee>)

    @Query("SELECT * FROM employees WHERE employeeId = :employeeId")
    suspend fun getEmployeeById(employeeId: String): Employee?

    @Query("SELECT * FROM employees WHERE employeeLoginNumber = :employeeLoginNumber")
    suspend fun getEmployeeByLoginNumber(employeeLoginNumber: String): Employee?

    @Query("DELETE FROM employees")
    suspend fun clearAll()
}