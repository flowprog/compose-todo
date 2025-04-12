package com.example.demo2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.demo2.TodoItem

@Entity(tableName = "pending_operations")
data class PendingOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationType: OperationType,
    val todoId: String,
    val todoData: String // JSON string of TodoItem
)

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE
}
