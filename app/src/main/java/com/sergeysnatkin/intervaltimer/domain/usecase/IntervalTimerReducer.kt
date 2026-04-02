package com.sergeysnatkin.intervaltimer.domain.usecase

import com.sergeysnatkin.intervaltimer.domain.model.Workout
import com.sergeysnatkin.intervaltimer.domain.timer.IntervalTimerSession
import com.sergeysnatkin.intervaltimer.domain.timer.TimerSnapshot
import com.sergeysnatkin.intervaltimer.domain.timer.TimerSnapshotCalculator
import com.sergeysnatkin.intervaltimer.domain.timer.TimerStatus

class IntervalTimerReducer {
    fun createSession(workout: Workout): IntervalTimerSession = IntervalTimerSession(workout = workout)

    fun snapshot(session: IntervalTimerSession, nowMillis: Long): TimerSnapshot = session.snapshot(nowMillis)

    fun start(session: IntervalTimerSession, nowMillis: Long): IntervalTimerSession = session.start(nowMillis)

    fun pause(session: IntervalTimerSession, nowMillis: Long): IntervalTimerSession = session.pause(nowMillis)

    fun resume(session: IntervalTimerSession, nowMillis: Long): IntervalTimerSession = session.resume(nowMillis)

    fun tick(session: IntervalTimerSession, nowMillis: Long): IntervalTimerSession = session.tick(nowMillis)

    fun reset(session: IntervalTimerSession): IntervalTimerSession = session.reset()

    fun complete(session: IntervalTimerSession): IntervalTimerSession = session.complete()

    fun calculate(workout: Workout, status: TimerStatus, elapsedMillis: Long): TimerSnapshot =
        TimerSnapshotCalculator.calculate(workout, status, elapsedMillis)
}
