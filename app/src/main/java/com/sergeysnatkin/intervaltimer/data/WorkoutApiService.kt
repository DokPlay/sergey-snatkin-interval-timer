package com.sergeysnatkin.intervaltimer.data

import com.sergeysnatkin.intervaltimer.data.dto.IntervalTimerResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface WorkoutApiService {
    @GET("api/interval-timers/{id}")
    suspend fun getWorkout(
        @Path("id") id: Int,
        @Header("App-Token") appToken: String = APP_TOKEN,
        @Header("Authorization") authorization: String = AUTHORIZATION,
    ): IntervalTimerResponseDto

    companion object {
        const val APP_TOKEN = "test-app-token"
        const val AUTHORIZATION = "Bearer test-token"
    }
}
