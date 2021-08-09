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
    private var muteSelf = false

    companion object {
        private lateinit var INSTANCE: SoundEffects
        const val SOUND_ON = 1
        const val SOUND_OFF = 2
        const val SOUND_SUCCESS = 3
        const val SOUND_FAILURE = 4

        // Очень синглтон

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

    fun setVolumeLevel(volumeLevel: Int) {
        val correctedLevel =
            when {
                volumeLevel < 1 -> getMinVolumeLevel().toInt()
                volumeLevel > getMaxVolumeLevel() -> getMaxVolumeLevel()
                else -> volumeLevel
            }
        audioManager.setStreamVolume(AudioManager.STREAM_RING, correctedLevel, AudioManager.FLAG_SHOW_UI)
    }

    fun setMute(mute: Boolean) {
        muteSelf = mute
    }

    fun getTenPointVolumeLevel() =
        audioManager.getStreamVolume(AudioManager.STREAM_RING) / getMaxVolumeLevel().toFloat() * 10f

    fun getMaxVolumeLevel() =
        audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)

    fun getMinVolumeLevel() =
        audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) / 10f

    // Проверка на аудиофокус и заглушка встроенных
    // звуковых сигналов старта и конца записи
    fun setOtherSoundsMuting(mute: Boolean) {
        fun muteStreamsExceptRing(streams: IntArray) {
            // Разные версии - разные подходы
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                streams.forEach { stream ->
                    audioManager.adjustStreamVolume(
                        stream,
                        if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                        0
                    )
                }
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_RING,
                    if (muteSelf) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                    0
                )
            } else {
                streams.forEach { stream ->
                    audioManager.setStreamMute(stream, mute)
                }
                audioManager.setStreamMute(AudioManager.STREAM_RING, muteSelf)
            }
        }

        muteStreamsExceptRing(
            intArrayOf(
                AudioManager.STREAM_MUSIC,
                AudioManager.STREAM_NOTIFICATION,
                AudioManager.STREAM_ALARM,
                AudioManager.STREAM_SYSTEM,
                AudioManager.STREAM_DTMF
            )
        )
    }

    private fun buildMediaPlayer(context: Context, resid: Int): MediaPlayer {
        // Создание плеера в аудиопотоке, в котором вещает recognizer

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

}