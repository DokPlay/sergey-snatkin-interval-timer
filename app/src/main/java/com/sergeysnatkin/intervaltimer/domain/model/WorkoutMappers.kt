package com.sergeysnatkin.intervaltimer.domain.model

import com.sergeysnatkin.intervaltimer.model.Workout

fun Workout.toWorkoutTimer(): WorkoutTimer {
    return WorkoutTimer(
        id = id.toLong(),
        title = title,
        intervals = intervals.map { interval ->
            WorkoutInterval(
                title = interval.title,
                durationSeconds = interval.timeSeconds,
            )
        },
    )
}
