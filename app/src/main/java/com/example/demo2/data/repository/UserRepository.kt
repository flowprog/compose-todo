package com.example.demo2.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.demo2.data.local.UserDao
import com.example.demo2.data.model.User
import com.example.demo2.data.remote.LoginRequest
import com.example.demo2.data.remote.UserApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserRepository(
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val context: Context
) {
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    suspend fun register(user: User): Result<User> = try {
        val response = userApi.register(user)
        userDao.insertUser(response)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun login(email: String, password: String): Result<User> = try {
        val response = userApi.login(LoginRequest(email, password))
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = response.token
        }
        userDao.insertUser(response.user)
        Result.success(response.user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUser(user: User): Result<User> = try {
        val response = userApi.updateUserProfile(user.id, user)
        userDao.updateUser(response)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getUserProfile(userId: String): Flow<User?> {
        return userDao.getUserById(userId)
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
} 