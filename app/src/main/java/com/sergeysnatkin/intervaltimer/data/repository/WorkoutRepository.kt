package com.sergeysnatkin.intervaltimer.data

import com.sergeysnatkin.intervaltimer.domain.model.Workout

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String, val throwable: Throwable? = null) : AppResult<Nothing>
}

interface WorkoutRepository {
    suspend fun getWorkout(id: Int): AppResult<Workout>
}
