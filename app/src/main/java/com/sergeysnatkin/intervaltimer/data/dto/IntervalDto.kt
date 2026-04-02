package com.sergeysnatkin.intervaltimer.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class IntervalDto(
    val title: String,
    val time: Int,
)
