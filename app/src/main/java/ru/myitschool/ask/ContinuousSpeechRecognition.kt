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

    private fun buildNotification(context: Context): Notification {
        val notificationBuilder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("ASK is listening...")
            .setContentText("Click to manage")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationIntent = Intent(context, AskActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        notificationBuilder.setContentIntent(pendingIntent)
        return notificationBuilder.build()
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
        startForeground(Constants.NOTIFICATION_ID, buildNotification(this))

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        continueSpeechRecognition()

        SoundEffects.getInstance().executeEffect(SoundEffects.SOUND_ON)

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
        val errorCause = "Error: " + when (p0) {
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

        continueSpeechRecognition()
    }

    override fun onResults(p0: Bundle?) {
        var results = "Heard: \n"
        val data = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
        for (i in 0 until data.size) {
            results += "Version ${i + 1}: ${data[i]}\n"
        }
        Log.d(Constants.MY_TAG, results)

        if (results.contains("мотылёк", true))
            SoundEffects.getInstance().executeEffect(SoundEffects.SOUND_SUCCESS)

        continueSpeechRecognition()
    }

    override fun onPartialResults(p0: Bundle?) {}

    override fun onEvent(p0: Int, p1: Bundle?) {}
}