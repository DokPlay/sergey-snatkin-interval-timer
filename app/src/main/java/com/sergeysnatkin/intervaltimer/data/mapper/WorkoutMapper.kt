package com.sergeysnatkin.intervaltimer.data.mapper

import com.sergeysnatkin.intervaltimer.data.dto.IntervalTimerResponseDto
import com.sergeysnatkin.intervaltimer.model.Workout
import com.sergeysnatkin.intervaltimer.model.WorkoutInterval

fun IntervalTimerResponseDto.toWorkout(): Workout {
    return Workout(
        id = timer.timerId,
        title = timer.title,
        totalTimeSeconds = timer.totalTime,
        intervals = timer.intervals.map { intervalDto ->
            WorkoutInterval(
                title = intervalDto.title,
                timeSeconds = intervalDto.time,
            )
        },
    )
}
