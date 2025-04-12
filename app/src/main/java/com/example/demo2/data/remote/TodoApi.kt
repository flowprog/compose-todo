package com.example.demo2.data.remote

import com.example.demo2.TodoItem
import retrofit2.http.*

interface TodoApi {
    @GET("todos")
    suspend fun getAllTodos(): List<TodoItem>

    @GET("todos/{id}")
    suspend fun getTodoById(@Path("id") id: String): TodoItem

    @POST("todos")
    suspend fun createTodo(@Body todo: TodoItem): TodoItem

    @PUT("todos/{id}")
    suspend fun updateTodo(@Path("id") id: String, @Body todo: TodoItem): TodoItem

    @DELETE("todos/{id}")
    suspend fun deleteTodo(@Path("id") id: String)
}
