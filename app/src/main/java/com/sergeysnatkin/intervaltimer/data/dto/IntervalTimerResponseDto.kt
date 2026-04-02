package com.sergeysnatkin.intervaltimer.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IntervalTimerResponseDto(
    @SerialName("timer") val timer: TimerDto,
)
