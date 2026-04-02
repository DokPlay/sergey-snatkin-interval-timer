package com.sergeysnatkin.intervaltimer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sergeysnatkin.intervaltimer.IntervalTimerUiState
import com.sergeysnatkin.intervaltimer.domain.timer.TimerSnapshot
import com.sergeysnatkin.intervaltimer.domain.timer.TimerStatus
import com.sergeysnatkin.intervaltimer.model.Workout
import com.sergeysnatkin.intervaltimer.model.WorkoutInterval
import com.sergeysnatkin.intervaltimer.ui.theme.AppColors

private enum class IntervalVisualState {
    Upcoming,
    Active,
    Completed,
}

@Composable
fun WorkoutScreen(
    uiState: IntervalTimerUiState,
    onStartOrResume: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onOpenLoader: () -> Unit,
) {
    val workout = requireNotNull(uiState.workout)
    val snapshot = requireNotNull(uiState.timerSnapshot)

    Scaffold(
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            ControlsBar(
                status = uiState.timerStatus,
                onStartOrResume = onStartOrResume,
                onPause = onPause,
                onReset = onReset,
                onOpenLoader = onOpenLoader,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                end = 24.dp,
                bottom = innerPadding.calculateBottomPadding() + 132.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                TopBar(
                    title = workout.title,
                    trailing = topTrailingText(workout, snapshot, uiState.timerStatus),
                    onOpenLoader = onOpenLoader,
                )
            }
            item {
                TimerCard(
                    workout = workout,
                    snapshot = snapshot,
                    status = uiState.timerStatus,
                )
            }
            item {
                IntervalsHeader(intervalCount = workout.intervals.size)
            }
            itemsIndexed(workout.intervals) { index, interval ->
                IntervalRow(
                    index = index,
                    interval = interval,
                    status = intervalVisualState(index, snapshot),
                    timerStatus = uiState.timerStatus,
                    progress = if (index == snapshot.currentIntervalIndex) snapshot.currentIntervalProgress else 0f,
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    trailing: String,
    onOpenLoader: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, AppColors.Border, CircleShape),
            shape = CircleShape,
            color = Color.White,
            onClick = onOpenLoader,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "←",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.TextPrimary,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary,
            )
            Text(
                text = trailing,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun TimerCard(
    workout: Workout,
    snapshot: TimerSnapshot,
    status: TimerStatus,
) {
    val borderColor = when (status) {
        TimerStatus.Idle -> AppColors.Border
        TimerStatus.Running -> AppColors.Primary
        TimerStatus.Paused -> AppColors.Pause
        TimerStatus.Completed -> AppColors.Secondary
    }
    val gradientColor = when (status) {
        TimerStatus.Idle -> Color.White
        TimerStatus.Running -> AppColors.PrimaryTint
        TimerStatus.Paused -> AppColors.PauseTint
        TimerStatus.Completed -> AppColors.SecondaryTint
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(gradientColor, Color.White),
                ),
            )
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(24.dp),
    ) {
        StatusPill(
            text = stateLabel(status),
            background = when (status) {
                TimerStatus.Idle -> AppColors.PrimaryTint
                TimerStatus.Running -> AppColors.PrimaryTint
                TimerStatus.Paused -> AppColors.PauseTint
                TimerStatus.Completed -> AppColors.SecondaryTint
            },
            foreground = borderColor,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = heroTitle(snapshot, status),
            style = MaterialTheme.typography.titleLarge,
            color = if (status == TimerStatus.Idle) AppColors.TextSecondary else AppColors.TextPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = heroTime(workout, snapshot, status),
            style = MaterialTheme.typography.displayLarge,
            color = AppColors.TextPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = heroSupportingText(workout, snapshot, status),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            progress = { if (status == TimerStatus.Idle) 0f else snapshot.overallProgress },
            color = borderColor,
            trackColor = AppColors.DisabledBackground,
        )
        if (status == TimerStatus.Completed) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value = formatSeconds(workout.totalTimeSeconds),
                    label = "Общее время",
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value = workout.intervals.size.toString(),
                    label = "Интервалов",
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    background: Color,
    foreground: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = foreground,
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.TextPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
        )
    }
}

@Composable
private fun IntervalRow(
    index: Int,
    interval: WorkoutInterval,
    status: IntervalVisualState,
    timerStatus: TimerStatus,
    progress: Float,
) {
    val borderColor = when {
        status == IntervalVisualState.Active && timerStatus == TimerStatus.Paused -> AppColors.Pause
        status == IntervalVisualState.Active -> AppColors.Primary
        else -> Color.Transparent
    }
    val progressColor = if (timerStatus == TimerStatus.Paused) AppColors.PauseTint else AppColors.PrimaryTint

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .alpha(if (status == IntervalVisualState.Completed) 0.45f else 1f),
    ) {
        if (status == IntervalVisualState.Active) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .background(progressColor),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IntervalLeading(index = index, status = status)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = interval.title,
                    style = MaterialTheme.typography.labelLarge.merge(
                        TextStyle(
                            textDecoration = if (status == IntervalVisualState.Completed) {
                                TextDecoration.LineThrough
                            } else {
                                TextDecoration.None
                            },
                        ),
                    ),
                    color = AppColors.TextPrimary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = formatSeconds(interval.timeSeconds),
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    status != IntervalVisualState.Active -> AppColors.TextSecondary
                    timerStatus == TimerStatus.Paused -> AppColors.Pause
                    else -> AppColors.Primary
                },
            )
        }
    }
}

@Composable
private fun IntervalLeading(
    index: Int,
    status: IntervalVisualState,
) {
    val background = when (status) {
        IntervalVisualState.Upcoming -> AppColors.Background
        IntervalVisualState.Active -> AppColors.Primary
        IntervalVisualState.Completed -> AppColors.SecondaryTint
    }
    val foreground = when (status) {
        IntervalVisualState.Active -> Color.White
        IntervalVisualState.Completed -> AppColors.Secondary
        IntervalVisualState.Upcoming -> AppColors.TextSecondary
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (status == IntervalVisualState.Completed) "✓" else (index + 1).toString(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = foreground,
        )
    }
}

@Composable
private fun IntervalsHeader(intervalCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Интервалы",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextPrimary,
        )
        Text(
            text = "$intervalCount интервалов",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextTertiary,
        )
    }
}

@Composable
private fun ControlsBar(
    status: TimerStatus,
    onStartOrResume: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onOpenLoader: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, AppColors.Background),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Background)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
        when (status) {
            TimerStatus.Idle -> {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    onClick = onStartOrResume,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary,
                        contentColor = Color.White,
                    ),
                ) {
                    ButtonContent(icon = "▶", text = "Старт")
                }
            }

            TimerStatus.Running -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        onClick = onPause,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Pause,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        ButtonContent(icon = "❚❚", text = "Пауза")
                    }
                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        onClick = onReset,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, AppColors.Error.copy(alpha = 0.18f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = AppColors.Error,
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        ButtonContent(icon = null, text = "Сбросить тренировку", emphasis = AppColors.Error)
                    }
                }
            }

            TimerStatus.Paused -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        onClick = onStartOrResume,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        ButtonContent(icon = "▶", text = "Продолжить")
                    }
                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        onClick = onReset,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, AppColors.Error.copy(alpha = 0.18f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = AppColors.Error,
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        ButtonContent(icon = null, text = "Сбросить тренировку", emphasis = AppColors.Error)
                    }
                }
            }

            TimerStatus.Completed -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        onClick = onStartOrResume,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Secondary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        ButtonContent(icon = "▶", text = "Запустить заново")
                    }
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        onClick = onOpenLoader,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        ButtonContent(icon = null, text = "Новая тренировка")
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun ButtonContent(
    icon: String?,
    text: String,
    emphasis: Color = Color.Unspecified,
) {
    Row(
        modifier = Modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium,
                color = if (emphasis == Color.Unspecified) Color.Unspecified else emphasis,
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (emphasis == Color.Unspecified) Color.Unspecified else emphasis,
        )
    }
}

private fun topTrailingText(
    workout: Workout,
    snapshot: TimerSnapshot,
    status: TimerStatus,
): String {
    return when (status) {
        TimerStatus.Idle -> formatSeconds(workout.totalTimeSeconds)
        TimerStatus.Running -> "● ${formatMillis(snapshot.elapsedMillis)}"
        TimerStatus.Paused -> "❚❚ Пауза"
        TimerStatus.Completed -> "Завершена"
    }
}

private fun stateLabel(status: TimerStatus): String {
    return when (status) {
        TimerStatus.Idle -> "Готово к старту"
        TimerStatus.Running -> "Выполняется"
        TimerStatus.Paused -> "На паузе"
        TimerStatus.Completed -> "Тренировка завершена"
    }
}

private fun heroTitle(snapshot: TimerSnapshot, status: TimerStatus): String {
    return when (status) {
        TimerStatus.Completed -> "Отличная работа!"
        else -> snapshot.currentInterval?.title ?: "Интервал"
    }
}

private fun heroTime(
    workout: Workout,
    snapshot: TimerSnapshot,
    status: TimerStatus,
): String {
    return when (status) {
        TimerStatus.Idle -> formatSeconds(workout.totalTimeSeconds)
        TimerStatus.Completed -> "0:00"
        else -> formatMillis(snapshot.currentIntervalRemainingMillis)
    }
}

private fun heroSupportingText(
    workout: Workout,
    snapshot: TimerSnapshot,
    status: TimerStatus,
): String {
    return when (status) {
        TimerStatus.Idle -> "Общее время"
        TimerStatus.Completed -> "${formatSeconds(workout.totalTimeSeconds)} из ${formatSeconds(workout.totalTimeSeconds)}"
        else -> "Прошло ${formatMillis(snapshot.elapsedMillis)} из ${formatSeconds(workout.totalTimeSeconds)}"
    }
}

private fun intervalsHeader(
    snapshot: TimerSnapshot,
    intervalCount: Int,
    status: TimerStatus,
): String {
    return when (status) {
        TimerStatus.Completed -> "Интервалы $intervalCount из $intervalCount ✓"
        TimerStatus.Idle -> "Интервалы $intervalCount интервалов"
        else -> "Интервалы ${snapshot.currentIntervalIndex + 1} из $intervalCount"
    }
}

private fun intervalVisualState(index: Int, snapshot: TimerSnapshot): IntervalVisualState {
    if (snapshot.status == TimerStatus.Idle) {
        return if (index == 0) IntervalVisualState.Active else IntervalVisualState.Upcoming
    }

    return when {
        index < snapshot.currentIntervalIndex -> IntervalVisualState.Completed
        index == snapshot.currentIntervalIndex -> {
            if (snapshot.status == TimerStatus.Completed && snapshot.currentIntervalProgress >= 1f) {
                IntervalVisualState.Completed
            } else {
                IntervalVisualState.Active
            }
        }
        else -> IntervalVisualState.Upcoming
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    return formatMillis(totalSeconds * 1_000L)
}

private fun formatMillis(totalMillis: Long): String {
    val totalSeconds = (totalMillis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
