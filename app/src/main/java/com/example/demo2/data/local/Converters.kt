package com.example.demo2.data.local

import androidx.room.TypeConverter
import com.example.demo2.TodoPriority

class Converters {
    @TypeConverter
    fun fromPriority(priority: TodoPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(value: String): TodoPriority {
        return TodoPriority.valueOf(value)
    }
} 