package ru.myitschool.ask.separate_processes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import ru.myitschool.ask.Constants

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private lateinit var alarmPlayer: MediaPlayer

        fun isAlarmPlayerInitialized() = Companion::alarmPlayer.isInitialized
    }

    private fun buildAlarmPlayer(context: Context): MediaPlayer {
        // Установка мелодии в соответствии с системной мелодией
        // будильника конкретного устройства

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val mediaPlayer = MediaPlayer()
        // К разным версиям андроид - разный подход
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build()
            )
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING)
        }
        mediaPlayer.setDataSource(context, alarmUri)
        mediaPlayer.prepare()
        return mediaPlayer
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra(Constants.INTENT_EXTRA_IS_TURNING_ALARM_ON, false)) {
            // Получен интент о запуске
            alarmPlayer = buildAlarmPlayer(context)
            alarmPlayer.start()
        } else {
            // Получен интент о остановке

            // Не через alarmManager.cancel(), т.к.
            // используется !И! для остановки уже звенящего будильника

            if (isAlarmPlayerInitialized())
                alarmPlayer.stop()

            val cancelIntent =
                Intent(context, AlarmReceiver::class.java).run {
                    putExtra(Constants.INTENT_EXTRA_IS_TURNING_ALARM_ON, true)
                }
            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // А после проверки плеера на необходимость
            // остановки уже .cancel()
            alarmManager.cancel(pendingIntent)
        }
    }

}