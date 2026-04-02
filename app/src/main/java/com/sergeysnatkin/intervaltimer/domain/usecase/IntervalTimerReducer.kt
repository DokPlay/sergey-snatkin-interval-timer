package com.sergeysnatkin.intervaltimer.domain.usecase

import com.sergeysnatkin.intervaltimer.domain.model.WorkoutTimer
import com.sergeysnatkin.intervaltimer.domain.timer.IntervalTimerSession
import com.sergeysnatkin.intervaltimer.domain.timer.TimerSnapshot
import com.sergeysnatkin.intervaltimer.domain.timer.TimerSnapshotCalculator
import com.sergeysnatkin.intervaltimer.domain.timer.TimerStatus

/**
 * Convenience API for ViewModel code.
 */
class IntervalTimerReducer {
    fun createSession(workout: WorkoutTimer): IntervalTimerSession = IntervalTimerSession(workout = workout)

    fun snapshot(session: IntervalTimerSession, nowMillis: Long): TimerSnapshot = session.snapshot(nowMillis)

    fun start(session: IntervalTimerSession, nowMillis: Long): IntervalTimerSession = session.start(nowMillis)

    fun pause(session: IntervalTimerSession, nowMillis: Long): IntervalTimerSession = session.pause(nowMillis)

    fun resume(session: IntervalTimerSession, nowMillis: Long): IntervalTimerSession = session.resume(nowMillis)

    fun tick(session: IntervalTimerSession, nowMillis: Long): IntervalTimerSession = session.tick(nowMillis)

    fun reset(session: IntervalTimerSession): IntervalTimerSession = session.reset()

    fun complete(session: IntervalTimerSession): IntervalTimerSession = session.complete()

    fun calculate(workout: WorkoutTimer, status: TimerStatus, elapsedMillis: Long): TimerSnapshot =
        TimerSnapshotCalculator.calculate(workout, status, elapsedMillis)
}
