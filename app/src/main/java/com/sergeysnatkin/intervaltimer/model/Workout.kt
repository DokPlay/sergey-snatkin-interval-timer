package com.sergeysnatkin.intervaltimer.model

import kotlinx.serialization.Serializable

@Serializable
data class Workout(
    val id: Int,
    val title: String,
    val totalTimeSeconds: Int,
    val intervals: List<WorkoutInterval>,
)
