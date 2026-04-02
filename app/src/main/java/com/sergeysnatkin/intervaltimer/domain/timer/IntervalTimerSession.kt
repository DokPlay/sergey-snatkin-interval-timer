package com.sergeysnatkin.intervaltimer.domain.timer

import com.sergeysnatkin.intervaltimer.domain.model.Workout

data class IntervalTimerSession(
    val workout: Workout,
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
        // Keep finished time and add only the current uninterrupted running segment.
        return (accumulatedElapsedMillis + (nowMillis - start)).coerceAtLeast(0L)
    }

    private fun completeIfNeeded(): IntervalTimerSession {
        val total = workout.totalDurationMillis
        return if (accumulatedElapsedMillis >= total) complete() else this
    }
}
