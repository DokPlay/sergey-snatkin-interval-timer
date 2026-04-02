package com.sergeysnatkin.intervaltimer.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@OptIn(ExperimentalSerializationApi::class)
object WorkoutApiFactory {
    private const val BASE_URL = "https://71-cl5.tz.testing.place/"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val contentType = "application/json".toMediaType()

    fun createService(): WorkoutApiService {
        val client = OkHttpClient.Builder()
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(WorkoutApiService::class.java)
    }
}
