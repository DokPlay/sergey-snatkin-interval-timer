package com.sergeysnatkin.intervaltimer.domain.model

/**
 * A single interval in the workout plan.
 */
data class WorkoutInterval(
    val title: String,
    val durationSeconds: Int,
) {
    init {
        require(durationSeconds > 0) { "Interval duration must be greater than zero." }
    }

    val durationMillis: Long
        get() = durationSeconds.toLong() * 1_000L
}

/**
 * Workout plan loaded from the API.
 */
data class WorkoutTimer(
    val id: Long,
    val title: String,
    val intervals: List<WorkoutInterval>,
) {
    init {
        require(intervals.isNotEmpty()) { "Workout timer must contain at least one interval." }
    }

    val totalDurationMillis: Long = intervals.fold(0L) { acc, interval -> acc + interval.durationMillis }

    val totalDurationSeconds: Int
        get() = (totalDurationMillis / 1_000L).toInt()

    val normalizedIntervals: List<WorkoutInterval> = intervals.toList()

    fun clampedElapsedMillis(elapsedMillis: Long): Long = elapsedMillis.coerceIn(0L, totalDurationMillis)
}
