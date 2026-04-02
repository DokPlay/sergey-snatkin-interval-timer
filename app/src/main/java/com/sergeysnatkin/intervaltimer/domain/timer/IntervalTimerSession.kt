package com.sergeysnatkin.intervaltimer.domain.timer

import com.sergeysnatkin.intervaltimer.domain.model.WorkoutTimer

/**
 * Immutable session that can be safely reduced inside a ViewModel.
 *
 * It keeps the accumulated elapsed time separate from the current running segment,
 * so the UI can survive pauses, resumes, and process recreation more easily.
 */
data class IntervalTimerSession(
    val workout: WorkoutTimer,
    val status: TimerStatus = TimerStatus.Idle,
    val accumulatedElapsedMillis: Long = 0L,
    val runningSinceMillis: Long? = null,
) {
    init {
        require(accumulatedElapsedMillis >= 0L) { "Accumulated elapsed time cannot be negative." }
    }

    val elapsedMillis: Long
        get() = accumulatedElapsedMillis

    fun start(nowMillis: Long): IntervalTimerSession {
        if (status == TimerStatus.Completed) return reset()
        if (status == TimerStatus.Running) return this

        return copy(
            status = TimerStatus.Running,
            runningSinceMillis = nowMillis,
        )
    }

    fun pause(nowMillis: Long): IntervalTimerSession {
        if (status != TimerStatus.Running) return this

        val updatedElapsed = computeRunningElapsed(nowMillis)
        return copy(
            status = TimerStatus.Paused,
            accumulatedElapsedMillis = updatedElapsed.coerceAtMost(workout.totalDurationMillis),
            runningSinceMillis = null,
        ).completeIfNeeded()
    }

    fun resume(nowMillis: Long): IntervalTimerSession =
        when (status) {
            TimerStatus.Paused -> copy(
                status = TimerStatus.Running,
                runningSinceMillis = nowMillis,
            )
            TimerStatus.Idle -> start(nowMillis)
            TimerStatus.Completed -> reset().start(nowMillis)
            TimerStatus.Running -> this
        }

    fun tick(nowMillis: Long): IntervalTimerSession {
        if (status != TimerStatus.Running) return this

        val updatedElapsed = computeRunningElapsed(nowMillis)
        return copy(
            accumulatedElapsedMillis = updatedElapsed,
            runningSinceMillis = nowMillis,
        ).completeIfNeeded()
    }

    fun reset(): IntervalTimerSession = copy(
        status = TimerStatus.Idle,
        accumulatedElapsedMillis = 0L,
        runningSinceMillis = null,
    )

    fun complete(): IntervalTimerSession = copy(
        status = TimerStatus.Completed,
        accumulatedElapsedMillis = workout.totalDurationMillis,
        runningSinceMillis = null,
    )

    fun snapshot(nowMillis: Long): TimerSnapshot = TimerSnapshotCalculator.calculate(
        workout = workout,
        status = status,
        elapsedMillis = currentElapsedMillis(nowMillis),
    )

    fun isRunning(): Boolean = status == TimerStatus.Running

    private fun currentElapsedMillis(nowMillis: Long): Long = when (status) {
        TimerStatus.Running -> computeRunningElapsed(nowMillis)
        TimerStatus.Completed -> workout.totalDurationMillis
        else -> accumulatedElapsedMillis
    }

    private fun computeRunningElapsed(nowMillis: Long): Long {
        val start = runningSinceMillis ?: return accumulatedElapsedMillis
        return (accumulatedElapsedMillis + (nowMillis - start)).coerceAtLeast(0L)
    }

    private fun completeIfNeeded(): IntervalTimerSession {
        val total = workout.totalDurationMillis
        return if (accumulatedElapsedMillis >= total) complete() else this
    }
}
