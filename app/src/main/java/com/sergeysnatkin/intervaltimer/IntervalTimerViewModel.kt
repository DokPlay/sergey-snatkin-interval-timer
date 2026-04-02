package com.sergeysnatkin.intervaltimer

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.sergeysnatkin.intervaltimer.audio.WorkoutSoundPlayer
import com.sergeysnatkin.intervaltimer.data.AppResult
import com.sergeysnatkin.intervaltimer.data.WorkoutApiFactory
import com.sergeysnatkin.intervaltimer.data.WorkoutRepository
import com.sergeysnatkin.intervaltimer.data.repository.WorkoutRepositoryImpl
import com.sergeysnatkin.intervaltimer.domain.model.toWorkoutTimer
import com.sergeysnatkin.intervaltimer.domain.timer.IntervalTimerSession
import com.sergeysnatkin.intervaltimer.domain.timer.TimerSnapshot
import com.sergeysnatkin.intervaltimer.domain.timer.TimerStatus
import com.sergeysnatkin.intervaltimer.domain.usecase.IntervalTimerReducer
import com.sergeysnatkin.intervaltimer.model.Workout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class IntervalTimerUiState(
    val workoutIdInput: String = DEFAULT_WORKOUT_ID,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val workout: Workout? = null,
    val timerStatus: TimerStatus = TimerStatus.Idle,
    val timerSnapshot: TimerSnapshot? = null,
) {
    val isWorkoutLoaded: Boolean = workout != null && timerSnapshot != null

    companion object {
        const val DEFAULT_WORKOUT_ID = "68"
    }
}

class IntervalTimerViewModel(
    appContext: Context,
    private val repository: WorkoutRepository,
    private val soundPlayer: WorkoutSoundPlayer,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val reducer = IntervalTimerReducer()
    private val json = Json { ignoreUnknownKeys = true }
    private val preferences = appContext.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    private val _uiState = MutableStateFlow(restoreUiState())
    val uiState: StateFlow<IntervalTimerUiState> = _uiState.asStateFlow()

    private var session: IntervalTimerSession? = restoreSession(_uiState.value.workout)
    private var tickerJob: Job? = null
    private var lastSnapshot: TimerSnapshot? = _uiState.value.timerSnapshot

    init {
        refreshFromClock(playSignals = false)
        if (session?.status == TimerStatus.Running) {
            startTicker()
        }
    }

    fun onWorkoutIdChanged(value: String) {
        val digitsOnly = value.filter(Char::isDigit).take(6)
        _uiState.update {
            it.copy(
                workoutIdInput = digitsOnly,
                loadError = null,
            )
        }
        persistState()
    }

    fun loadWorkout() {
        if (_uiState.value.isLoading) return

        val workoutId = _uiState.value.workoutIdInput.toIntOrNull()
        if (workoutId == null || workoutId <= 0) {
            _uiState.update { it.copy(loadError = "Введите корректный ID тренировки.") }
            persistState()
            return
        }

        stopTicker()
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        persistState()

        viewModelScope.launch {
            when (val result = repository.getWorkout(workoutId)) {
                is AppResult.Success -> handleWorkoutLoaded(result.data)
                is AppResult.Error -> {
                    session = null
                    lastSnapshot = null
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = result.message,
                            workout = null,
                            timerStatus = TimerStatus.Idle,
                            timerSnapshot = null,
                        )
                    }
                    persistState()
                }
            }
        }
    }

    fun startOrResume() {
        val currentSession = session ?: return
        val now = now()
        val nextSession = when (currentSession.status) {
            TimerStatus.Idle -> reducer.start(currentSession, now)
            TimerStatus.Paused -> reducer.resume(currentSession, now)
            TimerStatus.Completed -> reducer.resume(currentSession, now)
            TimerStatus.Running -> currentSession
        }

        session = nextSession
        render(nextSession, playSignals = currentSession.status != TimerStatus.Running)
        if (nextSession.status == TimerStatus.Running) {
            startTicker()
        }
    }

    fun pauseWorkout() {
        val currentSession = session ?: return
        if (currentSession.status != TimerStatus.Running) return

        session = reducer.pause(currentSession, now())
        stopTicker()
        render(requireNotNull(session), playSignals = false)
    }

    fun resetWorkout() {
        val currentSession = session ?: return
        stopTicker()
        session = reducer.reset(currentSession)
        render(requireNotNull(session), playSignals = false)
    }

    fun returnToLoader() {
        stopTicker()
        session = null
        lastSnapshot = null
        _uiState.update {
            it.copy(
                isLoading = false,
                loadError = null,
                workout = null,
                timerStatus = TimerStatus.Idle,
                timerSnapshot = null,
            )
        }
        clearPersistedWorkout()
        persistState()
    }

    override fun onCleared() {
        stopTicker()
        soundPlayer.release()
        super.onCleared()
    }

    private fun handleWorkoutLoaded(workout: Workout) {
        val nextSession = reducer.createSession(workout.toWorkoutTimer())
        session = nextSession
        lastSnapshot = null
        _uiState.update {
            it.copy(
                isLoading = false,
                loadError = null,
                workout = workout,
            )
        }
        render(nextSession, playSignals = false)
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return

        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_INTERVAL_MS)
                refreshFromClock(playSignals = true)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun refreshFromClock(playSignals: Boolean) {
        val currentSession = session ?: return
        val updatedSession = if (currentSession.status == TimerStatus.Running) {
            reducer.tick(currentSession, now())
        } else {
            currentSession
        }

        session = updatedSession
        render(updatedSession, playSignals = playSignals)

        if (updatedSession.status != TimerStatus.Running) {
            stopTicker()
        }
    }

    private fun render(session: IntervalTimerSession, playSignals: Boolean) {
        val snapshot = reducer.snapshot(session, now())
        val previousSnapshot = lastSnapshot
        val workout = _uiState.value.workout ?: return

        if (playSignals) {
            when {
                previousSnapshot == null -> Unit
                snapshot.status == TimerStatus.Completed && previousSnapshot.status != TimerStatus.Completed -> {
                    soundPlayer.playCompletionCue()
                }
                session.status == TimerStatus.Running &&
                    snapshot.currentIntervalIndex != previousSnapshot.currentIntervalIndex -> {
                    soundPlayer.playSingleCue()
                }
                session.status == TimerStatus.Running && previousSnapshot.status != TimerStatus.Running -> {
                    soundPlayer.playSingleCue()
                }
            }
        }

        lastSnapshot = snapshot
        _uiState.update {
            it.copy(
                isLoading = false,
                loadError = null,
                workout = workout,
                timerStatus = session.status,
                timerSnapshot = snapshot,
            )
        }
        persistState()
    }

    private fun restoreUiState(): IntervalTimerUiState {
        val workout = readString(KEY_WORKOUT_JSON)
            ?.takeIf(String::isNotBlank)
            ?.let { json.decodeFromString<Workout>(it) }
        val status = readString(KEY_TIMER_STATUS)
            ?.let { enumValueOf<TimerStatus>(it) }
            ?: TimerStatus.Idle

        return IntervalTimerUiState(
            workoutIdInput = readString(KEY_INPUT_ID).orEmpty().ifBlank {
                IntervalTimerUiState.DEFAULT_WORKOUT_ID
            },
            workout = workout,
            timerStatus = status,
            timerSnapshot = workout?.let {
                reducer.calculate(
                    workout = it.toWorkoutTimer(),
                    status = status,
                    elapsedMillis = readLong(KEY_ACCUMULATED_ELAPSED),
                )
            },
        )
    }

    private fun restoreSession(workout: Workout?): IntervalTimerSession? {
        if (workout == null) return null

        return IntervalTimerSession(
            workout = workout.toWorkoutTimer(),
            status = readString(KEY_TIMER_STATUS)
                ?.let { enumValueOf<TimerStatus>(it) }
                ?: TimerStatus.Idle,
            accumulatedElapsedMillis = readLong(KEY_ACCUMULATED_ELAPSED),
            runningSinceMillis = readNullableLong(KEY_RUNNING_SINCE),
        )
    }

    private fun persistState() {
        val state = _uiState.value
        savedStateHandle[KEY_INPUT_ID] = state.workoutIdInput
        savedStateHandle[KEY_WORKOUT_JSON] = state.workout?.let(json::encodeToString)
        savedStateHandle[KEY_TIMER_STATUS] = session?.status?.name
        savedStateHandle[KEY_ACCUMULATED_ELAPSED] = session?.accumulatedElapsedMillis ?: 0L
        savedStateHandle[KEY_RUNNING_SINCE] = session?.runningSinceMillis

        preferences.edit().apply {
            putString(KEY_INPUT_ID, state.workoutIdInput)

            val workoutJson = state.workout?.let(json::encodeToString)
            if (workoutJson == null) {
                remove(KEY_WORKOUT_JSON)
                remove(KEY_TIMER_STATUS)
                remove(KEY_RUNNING_SINCE)
                remove(KEY_ACCUMULATED_ELAPSED)
            } else {
                putString(KEY_WORKOUT_JSON, workoutJson)
                putString(KEY_TIMER_STATUS, session?.status?.name)
                putLong(KEY_ACCUMULATED_ELAPSED, session?.accumulatedElapsedMillis ?: 0L)
                val runningSince = session?.runningSinceMillis
                if (runningSince == null) {
                    remove(KEY_RUNNING_SINCE)
                } else {
                    putLong(KEY_RUNNING_SINCE, runningSince)
                }
            }
        }.apply()
    }

    private fun now(): Long = SystemClock.elapsedRealtime()

    private fun clearPersistedWorkout() {
        preferences.edit().apply {
            remove(KEY_WORKOUT_JSON)
            remove(KEY_TIMER_STATUS)
            remove(KEY_ACCUMULATED_ELAPSED)
            remove(KEY_RUNNING_SINCE)
        }.apply()
    }

    private fun readString(key: String): String? {
        return savedStateHandle.get<String>(key) ?: preferences.getString(key, null)
    }

    private fun readLong(key: String): Long {
        return when {
            savedStateHandle.contains(key) -> savedStateHandle.get<Long>(key) ?: 0L
            preferences.contains(key) -> preferences.getLong(key, 0L)
            else -> 0L
        }
    }

    private fun readNullableLong(key: String): Long? {
        return when {
            savedStateHandle.contains(key) -> savedStateHandle.get<Long>(key)
            preferences.contains(key) -> preferences.getLong(key, 0L)
            else -> null
        }
    }

    class Factory(
        private val context: Context,
        owner: SavedStateRegistryOwner,
    ) : AbstractSavedStateViewModelFactory(owner, null) {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle,
        ): T {
            return IntervalTimerViewModel(
                appContext = context,
                repository = WorkoutRepositoryImpl(WorkoutApiFactory.createService()),
                soundPlayer = WorkoutSoundPlayer(context),
                savedStateHandle = handle,
            ) as T
        }
    }

    private companion object {
        const val KEY_INPUT_ID = "input_id"
        const val KEY_WORKOUT_JSON = "workout_json"
        const val KEY_TIMER_STATUS = "timer_status"
        const val KEY_ACCUMULATED_ELAPSED = "accumulated_elapsed"
        const val KEY_RUNNING_SINCE = "running_since"
        const val TICK_INTERVAL_MS = 200L
        const val PREFERENCES_NAME = "interval_timer_state"
    }
}
