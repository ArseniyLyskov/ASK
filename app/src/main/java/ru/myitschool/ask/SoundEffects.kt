package ru.myitschool.ask

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build


class SoundEffects private constructor(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val soundOn = buildMediaPlayer(context, R.raw.ask_on_sound)
    private val soundOff = buildMediaPlayer(context, R.raw.ask_off_sound)
    private val soundSuccess = buildMediaPlayer(context, R.raw.ask_success_sound)
    private val soundFailure = buildMediaPlayer(context, R.raw.ask_failure_sound)

    companion object {
        private lateinit var INSTANCE: SoundEffects
        const val SOUND_ON = 1
        const val SOUND_OFF = 2
        const val SOUND_SUCCESS = 3
        const val SOUND_FAILURE = 4

        fun createInstance(context: Context) {
            INSTANCE = SoundEffects(context)
        }

        fun getInstance() = INSTANCE
    }

    fun executeEffect(effectType: Int) {
        when (effectType) {
            SOUND_ON -> soundOn.start()
            SOUND_OFF -> soundOff.start()
            SOUND_SUCCESS -> soundSuccess.start()
            SOUND_FAILURE -> soundFailure.start()
        }
    }

    private fun buildMediaPlayer(context: Context, resid: Int): MediaPlayer {
        val mediaPlayer = MediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build()
            )
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING)
        }
        val file = context.resources.openRawResourceFd(resid)
        mediaPlayer.setDataSource(
            file.fileDescriptor,
            file.startOffset,
            file.length
        )
        mediaPlayer.prepare()
        return mediaPlayer
    }

    fun setOtherSoundsMuting(mute: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                0
            )
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                0
            )
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_SYSTEM,
                if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                0
            )
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute)
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, mute)
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, mute)
        }
    }

}