package com.sergeysnatkin.intervaltimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sergeysnatkin.intervaltimer.ui.IntervalTimerApp
import com.sergeysnatkin.intervaltimer.ui.theme.IntervalTimerTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<IntervalTimerViewModel> {
        IntervalTimerViewModel.Factory(
            context = applicationContext,
            owner = this,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            IntervalTimerTheme {
                IntervalTimerApp(
                    uiState = uiState,
                    onWorkoutIdChanged = viewModel::onWorkoutIdChanged,
                    onLoadWorkout = viewModel::loadWorkout,
                    onStartOrResume = viewModel::startOrResume,
                    onPause = viewModel::pauseWorkout,
                    onReset = viewModel::resetWorkout,
                    onOpenLoader = viewModel::returnToLoader,
                )
            }
        }
    }
}
