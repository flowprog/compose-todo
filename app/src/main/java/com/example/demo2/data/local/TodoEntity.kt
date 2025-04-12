package com.example.demo2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.demo2.TodoItem
import com.example.demo2.TodoPriority
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID


@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val priority: TodoPriority,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toTodoItem(): TodoItem {
        val instant = Instant.ofEpochMilli(createdAt)
        return TodoItem(
            id = id,
            title = title,
            priority = priority,
            isCompleted = isCompleted,
            createdAt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        )
    }

    companion object {
        fun fromTodoItem(todoItem: TodoItem): TodoEntity {
            val instant: Instant = todoItem.createdAt.atZone(ZoneId.systemDefault()).toInstant()
            return TodoEntity(
                id = todoItem.id,
                title = todoItem.title,
                priority = todoItem.priority,
                isCompleted = todoItem.isCompleted,
                createdAt = instant.toEpochMilli()
            )
        }
    }
} 