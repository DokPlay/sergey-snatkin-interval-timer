package com.sergeysnatkin.intervaltimer.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimerDto(
    @SerialName("timer_id") val timerId: Int,
    val title: String,
    @SerialName("total_time") val totalTime: Int,
    val intervals: List<IntervalDto>,
)
