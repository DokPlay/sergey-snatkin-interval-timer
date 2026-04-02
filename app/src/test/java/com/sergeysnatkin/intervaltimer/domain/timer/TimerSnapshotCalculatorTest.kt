package com.sergeysnatkin.intervaltimer.domain.timer

import com.sergeysnatkin.intervaltimer.domain.model.WorkoutInterval
import com.sergeysnatkin.intervaltimer.domain.model.Workout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerSnapshotCalculatorTest {

    private val workout = Workout(
        id = 68,
        title = "Тест",
        totalTimeSeconds = 30,
        intervals = listOf(
            WorkoutInterval("Первый", 10),
            WorkoutInterval("Второй", 20),
        ),
    )

    @Test
    fun `maps elapsed time to active interval`() {
        val snapshot = TimerSnapshotCalculator.calculate(
            workout = workout,
            status = TimerStatus.Running,
            elapsedMillis = 12_000L,
        )

        assertEquals(1, snapshot.currentIntervalIndex)
        assertEquals(18_000L, snapshot.currentIntervalRemainingMillis)
        assertTrue(snapshot.currentIntervalProgress > 0f)
    }

    @Test
    fun `clamps values at workout end`() {
        val snapshot = TimerSnapshotCalculator.calculate(
            workout = workout,
            status = TimerStatus.Completed,
            elapsedMillis = 99_000L,
        )

        assertEquals(workout.totalDurationMillis, snapshot.elapsedMillis)
        assertEquals(0L, snapshot.remainingMillis)
        assertEquals(1f, snapshot.overallProgress)
    }
}
