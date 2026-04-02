package com.sergeysnatkin.intervaltimer.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutInterval(
    val title: String,
    val timeSeconds: Int,
)
