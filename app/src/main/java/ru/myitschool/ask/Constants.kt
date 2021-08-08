package ru.myitschool.ask

import android.Manifest

object Constants {

    const val MY_TAG = "MyTag"
    const val NOTIFICATION_CHANNEL_ID = "ID_ASK_CHANNEL"
    const val NOTIFICATION_ID = 1
    const val INTENT_EXTRA_IS_TURNING_ALARM_ON = "INTENT_EXTRA_IS_TURNING_ALARM_ON"
    const val INTENT_EXTRA_SWITCH_RECOGNIZER_MODE = "INTENT_EXTRA_SWITCH_RECOGNIZER_MODE"
    const val INTENT_EXTRA_MODE_LISTENING = "INTENT_EXTRA_MODE_LISTENING"
    const val INTENT_EXTRA_MODE_SLEEP = "INTENT_EXTRA_MODE_SLEEP"
    const val INTENT_EXTRA_MODE_NONE = "INTENT_EXTRA_MODE_NONE"
    const val COLOR_TRANSITION_TIME = 3000

    val REQUIRED_PERMISSIONS =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.FOREGROUND_SERVICE
            )
        } else {
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO,
            )
        }

    fun getTabContent(): IntArray {
        return intArrayOf(
            R.drawable.ask_icon_power,
            R.drawable.ask_icon_alarm,
            R.drawable.ask_icon_music,
            R.drawable.ask_icon_storage,
            R.drawable.ask_icon_question
        )
    }

}
