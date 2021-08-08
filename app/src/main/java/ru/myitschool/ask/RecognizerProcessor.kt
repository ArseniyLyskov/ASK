package ru.myitschool.ask

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

object RecognizerProcessor {

    fun processError(errorCode: Int) {
        val errorCause = "Error: " + when (errorCode) {
            1 -> "Network operation timed out."
            2 -> "Other network related errors."
            3 -> "Audio recording error."
            4 -> "Server sends error status."
            5 -> "Other client side errors."
            6 -> "No speech input"
            7 -> "No recognition result matched."
            8 -> "RecognitionService busy."
            9 -> "Insufficient permissions"
            10 -> "Too many requests from the same client."
            11 -> "Server has been disconnected, e.g. because the app has crashed."
            12 -> "Requested language is not available to be used with the current recognizer."
            else -> "Unknown error"
        }

        if (errorCode !in 6..7) {
            SoundEffects.getInstance().executeEffect(SoundEffects.SOUND_FAILURE)
        }

        Log.d(Constants.MY_TAG, errorCause)
    }

    fun processResult(context: Context, recognized: String, sleepMode: Boolean) {

        val actionIfHeard = { keyWordsResId: Int,
                              notifyHeard: Boolean,
                              action: () -> Unit ->
            if (isContainsKeyWordIgnoreCase(context, recognized, keyWordsResId)) {
                action.invoke()
                if (notifyHeard)
                    SoundEffects.getInstance().executeEffect(SoundEffects.SOUND_SUCCESS)
            }
        }

        SoundEffects.getInstance().run {

            if ((sleepMode && isContainsKeyWordIgnoreCase(context, recognized, R.array.keywords_name)) || !sleepMode) {
                actionIfHeard(R.array.keywords_wake_up, false) {
                    val intent = Intent(context, ContinuousSpeechRecognition::class.java)
                    intent.putExtra(
                        Constants.INTENT_EXTRA_SWITCH_RECOGNIZER_MODE,
                        Constants.INTENT_EXTRA_MODE_LISTENING
                    )
                    context.startService(intent)
                }
                actionIfHeard(R.array.keywords_sleep, false) {
                    val intent = Intent(context, ContinuousSpeechRecognition::class.java)
                    intent.putExtra(
                        Constants.INTENT_EXTRA_SWITCH_RECOGNIZER_MODE,
                        Constants.INTENT_EXTRA_MODE_SLEEP
                    )
                    context.startService(intent)
                }
                actionIfHeard(R.array.keywords_reply, true) {}
            }

            if (sleepMode)
                return

            actionIfHeard(R.array.keywords_mute, true) { setMute(true) }
            actionIfHeard(R.array.keywords_unmute, true) { setMute(false) }

            actionIfHeard(R.array.keywords_on, false) {
                actionIfHeard(R.array.keywords_volume, true) { setMute(false) }
            }

            actionIfHeard(R.array.keywords_off, false) {
                actionIfHeard(R.array.keywords_volume, true) { setMute(true) }

                actionIfHeard(R.array.keywords_alarm, true) {
                    interactWithAlarm(context, false, System.currentTimeMillis(), false, 0)
                }
                actionIfHeard(R.array.keywords_timer, true) {
                    interactWithAlarm(context, false, System.currentTimeMillis(), false, 0)
                }
            }

            actionIfHeard(R.array.keywords_alarm, false) {
                if (isContainsTime(recognized)) {
                    val timeString = getStringOfTime(recognized)
                    if (timeString.canBeAlarmTime()) {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = System.currentTimeMillis()
                            set(Calendar.HOUR_OF_DAY, timeString.toInt() / 100)
                            set(Calendar.MINUTE, timeString.toInt() % 100)
                        }
                        interactWithAlarm(context, true, calendar.timeInMillis, true, 300_000)
                        executeEffect(SoundEffects.SOUND_SUCCESS)
                    } else {
                        executeEffect(SoundEffects.SOUND_FAILURE)
                    }
                }
            }

            actionIfHeard(R.array.keywords_timer, false) {
                if (isContainsTime(recognized)) {
                    val timeString = getStringOfTime(recognized)
                    if (timeString.canBeTimerTime()) {
                        val plusMillis =
                            (timeString.toInt() / 100 * 60_000) + (timeString.toInt() % 100 * 1_000)
                        interactWithAlarm(context, true, System.currentTimeMillis() + plusMillis, false, 0)
                        executeEffect(SoundEffects.SOUND_SUCCESS)
                    } else {
                        executeEffect(SoundEffects.SOUND_FAILURE)
                    }
                }
            }

            actionIfHeard(R.array.keywords_volume, false) {
                actionIfHeard(R.array.keywords_max, true) { setVolumeLevel(getMaxVolumeLevel()) }
                actionIfHeard(R.array.keywords_middle, true) { setVolumeLevel((getMaxVolumeLevel() / 2f).toInt()) }
                actionIfHeard(R.array.keywords_min, true) { setVolumeLevel(getMinVolumeLevel().toInt()) }
                actionIfHeard(R.array.keywords_loudly, true) {
                    setVolumeLevel((getMaxVolumeLevel() * ((getTenPointVolumeLevel() + 3) / 10f)).toInt())
                }
                actionIfHeard(R.array.keywords_quieter, true) {
                    setVolumeLevel((getMaxVolumeLevel() * ((getTenPointVolumeLevel() - 3) / 10f)).toInt())
                }

                if (isContainsNumber(recognized)) {
                    val number = getStringOfNumber(recognized)
                    if (number.canBeVolumeLevel()) {
                        setVolumeLevel((getMaxVolumeLevel() * (number.toInt() / 10f)).toInt())
                        executeEffect(SoundEffects.SOUND_SUCCESS)
                    } else {
                        executeEffect(SoundEffects.SOUND_FAILURE)
                    }
                }
            }
        }
    }

    private fun interactWithAlarm(
        context: Context, turnOn: Boolean,
        triggerAtMillis: Long, repeating: Boolean, interval: Long
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).run {
            putExtra(Constants.INTENT_EXTRA_IS_TURNING_ALARM_ON, turnOn)
        }
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (repeating)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, interval, pendingIntent)
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    private fun isContainsKeyWordIgnoreCase(context: Context, recognized: String, keyWordsResId: Int): Boolean {
        val keyWords: Array<String> = context.resources.getStringArray(keyWordsResId)
        keyWords.forEach {
            if (recognized.contains(it, true))
                return true
        }
        return false
    }

    private fun isContainsNumber(recognized: String): Boolean {
        recognized.forEach { c ->
            if (c.isDigit())
                return true
        }
        return false
    }

    private fun isContainsTime(recognized: String): Boolean {
        for (i in 0 until recognized.length - 3) {
            if (recognized[i].isDigit() && recognized[i + 1].isDigit()
                && recognized[i + 2].isDigit() && recognized[i + 3].isDigit()
            )
                return true
        }
        return false
    }

    private fun String.canBeVolumeLevel(): Boolean {
        return this.toInt() in 1..10
    }

    private fun String.canBeAlarmTime(): Boolean {
        val hours = this.toInt() / 100
        val minutes = this.toInt() % 100
        return (length == 4 && hours in 0 until 24 && minutes in 0 until 60)
    }

    private fun String.canBeTimerTime(): Boolean {
        val minutes = this.toInt() / 100
        val seconds = this.toInt() % 100
        return (length == 4 && minutes in 0 until 60 && seconds in 0 until 60)
    }

    private fun getStringOfNumber(recognized: String): String {
        var number = ""
        recognized.forEachIndexed { index, c ->
            if (c.isDigit()) {
                number += "$c"
                for (i in index + 1 until recognized.length) {
                    if (recognized[i].isDigit())
                        number += "${recognized[i]}"
                    else
                        break
                }
                return number
            }
        }
        return number
    }

    private fun getStringOfTime(recognized: String): String {
        var number = ""
        for (i in 0 until recognized.length - 3) {
            if (recognized[i].isDigit() && recognized[i + 1].isDigit()
                && recognized[i + 2].isDigit() && recognized[i + 3].isDigit()
            ) {
                number += recognized.slice(i..(i + 3))
                return number
            }
        }
        return number
    }
}