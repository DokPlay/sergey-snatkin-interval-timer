package com.sergeysnatkin.intervaltimer.domain.timer

/**
 * High-level timer lifecycle for the workout screen.
 */
enum class TimerStatus {
    Idle,
    Running,
    Paused,
    Completed,
}
