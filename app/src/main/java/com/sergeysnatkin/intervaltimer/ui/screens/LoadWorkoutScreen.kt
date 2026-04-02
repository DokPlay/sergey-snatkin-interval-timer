package com.sergeysnatkin.intervaltimer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sergeysnatkin.intervaltimer.IntervalTimerUiState
import com.sergeysnatkin.intervaltimer.ui.theme.AppColors

@Composable
fun LoadWorkoutScreen(
    uiState: IntervalTimerUiState,
    onWorkoutIdChanged: (String) -> Unit,
    onLoadWorkout: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .imePadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Интервальный\nтаймер",
                style = MaterialTheme.typography.headlineLarge,
                color = AppColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Введите ID тренировки для загрузки программы интервалов",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary,
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.workoutIdInput,
                onValueChange = onWorkoutIdChanged,
                label = { Text("ID тренировки") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onLoadWorkout() }),
                isError = uiState.loadError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Border,
                    unfocusedBorderColor = AppColors.Border,
                    focusedLabelColor = AppColors.TextTertiary,
                    unfocusedLabelColor = AppColors.TextTertiary,
                    cursorColor = AppColors.Primary,
                    focusedTextColor = AppColors.TextPrimary,
                    unfocusedTextColor = AppColors.TextPrimary,
                    errorBorderColor = AppColors.Error,
                    errorLabelColor = AppColors.Error,
                ),
            )
            if (uiState.loadError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.loadError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Error,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (uiState.isLoading) {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    onClick = {},
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.Primary),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = AppColors.PrimaryTintStrong,
                        contentColor = AppColors.Primary,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier.wrapContentWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = AppColors.Primary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Загрузка…",
                                color = AppColors.Primary,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            } else {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    onClick = onLoadWorkout,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        if (uiState.loadError != null) "Повторить" else "Загрузить",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
            }
        }
    }
}
