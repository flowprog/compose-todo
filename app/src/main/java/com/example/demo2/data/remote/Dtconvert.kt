package com.example.demo2.data.remote

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter2 : TypeAdapter<LocalDateTime?>() {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Throws(IOException::class)
    public override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    @Throws(IOException::class)
    public override fun read(`in`: JsonReader): LocalDateTime? {
        if (`in`.peek() === JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        val dateStr: String = `in`.nextString()
        return LocalDateTime.parse(dateStr, formatter)
    }
}