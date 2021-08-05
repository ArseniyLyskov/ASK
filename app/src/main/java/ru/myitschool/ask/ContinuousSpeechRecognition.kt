package ru.myitschool.ask

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat


class ContinuousSpeechRecognition : Service(), RecognitionListener {
    private lateinit var speechRecognizer: SpeechRecognizer
    private var sleepMode = false
    private val recognizingIntent = initRecognizingIntent()

    private fun initRecognizingIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5
            )
        }
    }

    private fun continueSpeechRecognition() {
        SoundEffects.getInstance().setOtherSoundsMuting(true)
        speechRecognizer.run {
            destroy()
            setRecognitionListener(this@ContinuousSpeechRecognition)
            startListening(recognizingIntent)
        }
    }

    private fun createNotificationChannelIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val id = Constants.NOTIFICATION_CHANNEL_ID
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, id, importance)

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(context: Context, sleepMode: Boolean): Notification {
        val notificationBuilder =
            if (sleepMode) NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.sleep_notification_title))
                .setContentText(getString(R.string.sleep_notification_text))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            else NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.listening_notification_title))
                .setContentText(getString(R.string.listening_notification_text))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationIntent = Intent(context, AskActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        notificationBuilder.setContentIntent(pendingIntent)
        return notificationBuilder.build()
    }

    private fun notifyUserModeSwitched() {
        startForeground(Constants.NOTIFICATION_ID, buildNotification(this, sleepMode))
        SoundEffects.getInstance().executeEffect(if (sleepMode) SoundEffects.SOUND_OFF else SoundEffects.SOUND_ON)
    }


    // Service methods


    override fun onCreate() {
        super.onCreate()
        Log.d(Constants.MY_TAG, "CSR: onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
        SoundEffects.getInstance().run {
            executeEffect(SoundEffects.SOUND_OFF)
            setOtherSoundsMuting(false)
        }
        Log.d(Constants.MY_TAG, "CSR: onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(Constants.MY_TAG, "CSR: onStartCommand")

        createNotificationChannelIfNeeded()
        startForeground(Constants.NOTIFICATION_ID, buildNotification(this, sleepMode))

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        continueSpeechRecognition()

        SoundEffects.getInstance()
            .run {
                setVolumeLevel(((getMaxVolumeLevel() - getMinVolumeLevel()) / 2f + getMinVolumeLevel()).toInt())
                executeEffect(SoundEffects.SOUND_ON)
            }

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    // RecognitionListener methods


    override fun onReadyForSpeech(p0: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(p0: Float) {}

    override fun onBufferReceived(p0: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onError(p0: Int) {
        RecognizerProcessor.processError(p0)

        continueSpeechRecognition()
    }

    override fun onResults(p0: Bundle?) {
        val data = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
        var recognizedString = ""
        data.forEach {
            recognizedString += "\n$it"
        }
        Log.d(Constants.MY_TAG, recognizedString)
        when (RecognizerProcessor.processResult(this, recognizedString, sleepMode)) {
            RecognizerProcessor.ACTION_SWITCH_TO_LISTENING_MODE -> {
                sleepMode = false
                notifyUserModeSwitched()
            }
            RecognizerProcessor.ACTION_SWITCH_TO_SLEEP_MODE -> {
                sleepMode = true
                notifyUserModeSwitched()
            }
        }

        continueSpeechRecognition()
    }

    override fun onPartialResults(p0: Bundle?) {}

    override fun onEvent(p0: Int, p1: Bundle?) {}
}