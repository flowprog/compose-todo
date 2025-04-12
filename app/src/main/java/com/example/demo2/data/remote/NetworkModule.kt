package com.example.demo2.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// LocalDateTime 的 TypeAdapter
class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            // 将LocalDateTime转换为ISO格式的字符串(带Z的UTC时间)
            val instant = value.atZone(ZoneId.systemDefault()).toInstant()
            out.value(instant.toString())
        }
    }

    override fun read(input: JsonReader): LocalDateTime? {
        if (input.peek() == com.google.gson.stream.JsonToken.NULL) {
            input.nextNull()
            return null
        }
        val dateStr = input.nextString()
        // 从ISO格式字符串解析回LocalDateTime
        return Instant.parse(dateStr).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }
}

object NetworkModule {
    private const val BASE_URL = "http://your-url.com/api/" //replace your url.

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val userApi: UserApi = retrofit.create(UserApi::class.java)
    val todoApi: TodoApi = retrofit.create(TodoApi::class.java)
}