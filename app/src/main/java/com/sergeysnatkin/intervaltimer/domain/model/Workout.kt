package com.sergeysnatkin.intervaltimer.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutInterval(
    val title: String,
    val timeSeconds: Int,
) {
    init {
        require(timeSeconds > 0) { "Interval duration must be greater than zero." }
    }

    val durationMillis: Long
        get() = timeSeconds * 1_000L
}

@Serializable
data class Workout(
    val id: Int,
    val title: String,
    val totalTimeSeconds: Int,
    val intervals: List<WorkoutInterval>,
) {
    init {
        require(intervals.isNotEmpty()) { "Workout must contain at least one interval." }
    }

    val totalDurationMillis: Long
        get() = totalTimeSeconds * 1_000L

    fun clampElapsedMillis(elapsedMillis: Long): Long = elapsedMillis.coerceIn(0L, totalDurationMillis)
}
