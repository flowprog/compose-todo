package com.example.demo2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PendingOperationDao {
    @Insert
    suspend fun insertOperation(operation: PendingOperation)

    @Query("SELECT * FROM pending_operations ORDER BY id ASC")
    suspend fun getAllPendingOperations(): List<PendingOperation>

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteOperation(id: Long)

    @Query("DELETE FROM pending_operations WHERE todoId = :todoId")
    suspend fun deleteByTodo(todoId: String)
}
