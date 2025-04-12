package com.example.demo2.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE priority = :priority AND isCompleted = :isCompleted")
    fun getTodosByPriority(priority: String, isCompleted: Boolean): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE isCompleted = :isCompleted")
    fun getTodosByCompletion(isCompleted: Boolean): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :todoId")
    suspend fun getTodoById(todoId: String): TodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodos(todos: List<TodoEntity>)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query("DELETE FROM todos WHERE id = :todoId")
    suspend fun deleteTodoById(todoId: String)

    @Query("DELETE FROM todos")
    suspend fun deleteAllTodos()

    @Query("UPDATE todos SET isCompleted = :isCompleted WHERE id = :todoId")
    suspend fun updateTodoCompletionStatus(todoId: String, isCompleted: Boolean)
}