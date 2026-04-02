package com.sergeysnatkin.intervaltimer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WorkoutSoundPlayer(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    fun playSingleCue() {
        scope.launch { playCueInternal() }
    }

    fun playCompletionCue() {
        scope.launch {
            playCueInternal()
            delay(220)
            playCueInternal()
        }
    }

    fun release() {
        scope.cancel()
    }

    private fun playCueInternal() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ?: return
        val player = MediaPlayer.create(appContext, uri, null, audioAttributes, 0) ?: return

        player.setOnCompletionListener { completedPlayer ->
            completedPlayer.release()
        }
        player.start()
    }
}
