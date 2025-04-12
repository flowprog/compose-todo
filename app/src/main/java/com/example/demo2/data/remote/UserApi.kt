package com.example.demo2.data.remote

import com.example.demo2.data.model.User
import retrofit2.http.*

interface UserApi {
    @POST("users/register")
    suspend fun register(@Body user: User): User

    @POST("users/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @GET("users/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): User

    @PUT("users/{id}")
    suspend fun updateUserProfile(@Path("id") userId: String, @Body user: User): User
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
) 