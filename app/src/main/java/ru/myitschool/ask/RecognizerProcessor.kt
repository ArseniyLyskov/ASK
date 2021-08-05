package ru.myitschool.ask

import android.content.Context
import android.util.Log

object RecognizerProcessor {
    const val ACTION_NONE = 0
    const val ACTION_SWITCH_TO_LISTENING_MODE = 1
    const val ACTION_SWITCH_TO_SLEEP_MODE = 2

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
        Log.d(Constants.MY_TAG, errorCause)
    }

    fun processResult(context: Context, recognized: String, sleepMode: Boolean): Int {

        fun isContainsKeyWordIgnoreCase(keyWordsResId: Int): Boolean {
            val keyWords: Array<String> = context.resources.getStringArray(keyWordsResId)
            keyWords.forEach {
                if (recognized.contains(it, true))
                    return true
            }
            return false
        }

        fun isContainsNumber(): Boolean {
            recognized.forEach { c ->
                if (c.isDigit())
                    return true
            }
            return false
        }

        fun Int.canBeVolumeLevel(): Boolean {
            return this in 1..10
        }

        fun getNumber(): Int {
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
                    return number.toInt()
                }
            }
            return number.toInt()
        }

        if ((sleepMode && isContainsKeyWordIgnoreCase(R.array.keywords_name)) || !sleepMode) {
            when {
                isContainsKeyWordIgnoreCase(R.array.keywords_wake_up) -> return ACTION_SWITCH_TO_LISTENING_MODE
                isContainsKeyWordIgnoreCase(R.array.keywords_sleep) -> return ACTION_SWITCH_TO_SLEEP_MODE
                isContainsKeyWordIgnoreCase(R.array.keywords_reply) -> SoundEffects.getInstance()
                    .executeEffect(SoundEffects.SOUND_SUCCESS)
            }
        }

        if (sleepMode)
            return ACTION_NONE

        SoundEffects.getInstance().run {
            when {
                isContainsKeyWordIgnoreCase(R.array.keywords_mute) -> {
                    setMute(true)
                    executeEffect(SoundEffects.SOUND_SUCCESS)
                }
                isContainsKeyWordIgnoreCase(R.array.keywords_unmute) -> {
                    setMute(false)
                    executeEffect(SoundEffects.SOUND_SUCCESS)
                }
            }
        }

        if (isContainsKeyWordIgnoreCase(R.array.keywords_volume)) {
            SoundEffects.getInstance().run {
                when {

                    isContainsKeyWordIgnoreCase(R.array.keywords_max) -> {
                        setVolumeLevel(getMaxVolumeLevel())
                        executeEffect(SoundEffects.SOUND_SUCCESS)
                    }
                    isContainsKeyWordIgnoreCase(R.array.keywords_middle) -> {
                        setVolumeLevel((getMaxVolumeLevel() / 2f).toInt())
                        executeEffect(SoundEffects.SOUND_SUCCESS)
                    }
                    isContainsKeyWordIgnoreCase(R.array.keywords_min) -> {
                        setVolumeLevel(getMinVolumeLevel().toInt())
                        executeEffect(SoundEffects.SOUND_SUCCESS)
                    }

                    isContainsKeyWordIgnoreCase(R.array.keywords_loudly) -> {
                        setVolumeLevel((getMaxVolumeLevel() * ((getTenPointVolumeLevel() + 3) / 10f)).toInt())
                        executeEffect(SoundEffects.SOUND_SUCCESS)
                    }
                    isContainsKeyWordIgnoreCase(R.array.keywords_quieter) -> {
                        setVolumeLevel((getMaxVolumeLevel() * ((getTenPointVolumeLevel() - 3) / 10f)).toInt())
                        executeEffect(SoundEffects.SOUND_SUCCESS)
                    }

                    isContainsNumber() ->
                        if (getNumber().canBeVolumeLevel()) {
                            setVolumeLevel((getMaxVolumeLevel() * (getNumber() / 10f)).toInt())
                            executeEffect(SoundEffects.SOUND_SUCCESS)
                        }
                }
            }
        }

        return ACTION_NONE
    }
}