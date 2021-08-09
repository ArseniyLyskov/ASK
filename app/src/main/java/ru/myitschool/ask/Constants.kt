package ru.myitschool.ask

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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

    // Разрешения и их обработка

    fun isAllPermissionsGranted(context: Context) = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(activity: Activity) {
        if (!isAllPermissionsGranted(activity.applicationContext)) {
            ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, 10)
        }
    }

    private val REQUIRED_PERMISSIONS =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.FOREGROUND_SERVICE
            )
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                arrayOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } else {
                arrayOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO,
                )
            }
        }

    fun getTabContent(): IntArray { // Настройка иконок TabLayout
        return intArrayOf(
            R.drawable.ask_icon_power,
            R.drawable.ask_icon_alarm,
            R.drawable.ask_icon_music,
            R.drawable.ask_icon_storage,
            R.drawable.ask_icon_question
        )
    }

}
