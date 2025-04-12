package com.example.demo2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val password: String,
    val avatar: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 