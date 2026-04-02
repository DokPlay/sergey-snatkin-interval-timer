package com.sergeysnatkin.intervaltimer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sergeysnatkin.intervaltimer.IntervalTimerUiState
import com.sergeysnatkin.intervaltimer.ui.screens.LoadWorkoutScreen
import com.sergeysnatkin.intervaltimer.ui.screens.WorkoutScreen
import com.sergeysnatkin.intervaltimer.ui.theme.AppColors

@Composable
fun IntervalTimerApp(
    uiState: IntervalTimerUiState,
    onWorkoutIdChanged: (String) -> Unit,
    onLoadWorkout: () -> Unit,
    onStartOrResume: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onOpenLoader: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.Background,
    ) {
        if (uiState.isWorkoutLoaded) {
            BackHandler(onBack = onOpenLoader)
            WorkoutScreen(
                uiState = uiState,
                onStartOrResume = onStartOrResume,
                onPause = onPause,
                onReset = onReset,
                onOpenLoader = onOpenLoader,
            )
        } else {
            LoadWorkoutScreen(
                uiState = uiState,
                onWorkoutIdChanged = onWorkoutIdChanged,
                onLoadWorkout = onLoadWorkout,
            )
        }
    }
}
