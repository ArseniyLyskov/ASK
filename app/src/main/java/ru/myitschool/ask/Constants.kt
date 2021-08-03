package ru.myitschool.ask

import android.Manifest

object Constants {

    const val MY_TAG = "MyTag"
    const val NOTIFICATION_CHANNEL_ID = "ID_ASK_CHANNEL"
    const val NOTIFICATION_ID = 1

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

}