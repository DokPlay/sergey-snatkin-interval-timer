package com.sergeysnatkin.intervaltimer.domain.timer

import com.sergeysnatkin.intervaltimer.domain.model.WorkoutInterval
import com.sergeysnatkin.intervaltimer.domain.model.WorkoutTimer

/**
 * Computed timer snapshot that is safe to render from a ViewModel.
 */
data class TimerSnapshot(
    val status: TimerStatus,
    val elapsedMillis: Long,
    val remainingMillis: Long,
    val overallProgress: Float,
    val currentIntervalIndex: Int,
    val currentIntervalElapsedMillis: Long,
    val currentIntervalRemainingMillis: Long,
    val currentIntervalProgress: Float,
    val currentInterval: WorkoutInterval?,
    val nextInterval: WorkoutInterval?,
) {
    val isAtStart: Boolean = elapsedMillis == 0L && status == TimerStatus.Idle
    val isCompleted: Boolean = status == TimerStatus.Completed
}

/**
 * Maps an absolute elapsed time to a rendered workout snapshot.
 */
object TimerSnapshotCalculator {
    fun calculate(
        workout: WorkoutTimer,
        status: TimerStatus,
        elapsedMillis: Long,
    ): TimerSnapshot {
        val clampedElapsed = workout.clampedElapsedMillis(elapsedMillis)
        val total = workout.totalDurationMillis
        val remaining = (total - clampedElapsed).coerceAtLeast(0L)
        val overallProgress = if (total == 0L) 0f else clampedElapsed.toFloat() / total.toFloat()

        val intervalInfo = findInterval(workout, clampedElapsed)

        return TimerSnapshot(
            status = status,
            elapsedMillis = clampedElapsed,
            remainingMillis = remaining,
            overallProgress = overallProgress,
            currentIntervalIndex = intervalInfo.currentIndex,
            currentIntervalElapsedMillis = intervalInfo.elapsedInCurrent,
            currentIntervalRemainingMillis = intervalInfo.remainingInCurrent,
            currentIntervalProgress = intervalInfo.progressInCurrent,
            currentInterval = intervalInfo.currentInterval,
            nextInterval = intervalInfo.nextInterval,
        )
    }

    private fun findInterval(
        workout: WorkoutTimer,
        elapsedMillis: Long,
    ): IntervalInfo {
        var consumed = 0L

        workout.intervals.forEachIndexed { index, interval ->
            val intervalEnd = consumed + interval.durationMillis
            val isLast = index == workout.intervals.lastIndex

            if (elapsedMillis < intervalEnd || (isLast && elapsedMillis >= intervalEnd)) {
                val elapsedInCurrent = (elapsedMillis - consumed).coerceIn(0L, interval.durationMillis)
                val remainingInCurrent = (interval.durationMillis - elapsedInCurrent).coerceAtLeast(0L)
                val progress = if (interval.durationMillis == 0L) 0f else elapsedInCurrent.toFloat() / interval.durationMillis.toFloat()

                return IntervalInfo(
                    currentIndex = index,
                    elapsedInCurrent = elapsedInCurrent,
                    remainingInCurrent = remainingInCurrent,
                    progressInCurrent = progress,
                    currentInterval = interval,
                    nextInterval = workout.intervals.getOrNull(index + 1),
                )
            }

            consumed = intervalEnd
        }

        val lastIndex = workout.intervals.lastIndex
        val lastInterval = workout.intervals[lastIndex]
        return IntervalInfo(
            currentIndex = lastIndex,
            elapsedInCurrent = lastInterval.durationMillis,
            remainingInCurrent = 0L,
            progressInCurrent = 1f,
            currentInterval = lastInterval,
            nextInterval = null,
        )
    }

    private data class IntervalInfo(
        val currentIndex: Int,
        val elapsedInCurrent: Long,
        val remainingInCurrent: Long,
        val progressInCurrent: Float,
        val currentInterval: WorkoutInterval,
        val nextInterval: WorkoutInterval?,
    )
}
