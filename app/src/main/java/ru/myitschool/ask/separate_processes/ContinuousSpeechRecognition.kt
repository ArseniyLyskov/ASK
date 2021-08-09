package ru.myitschool.ask.separate_processes

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
import ru.myitschool.ask.Constants
import ru.myitschool.ask.R
import ru.myitschool.ask.SoundEffects
import ru.myitschool.ask.layer_UI.AskActivity


class ContinuousSpeechRecognition : Service(), RecognitionListener {
    private lateinit var speechRecognizer: SpeechRecognizer
    private var sleepMode = false
    private val recognizingIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(
            RecognizerIntent.EXTRA_MAX_RESULTS,
            5
        )
    }

    companion object {
        private var running = false

        fun isRunning() = running
    }

    private fun continueSpeechRecognition() {
        // Перезапуск встроенного recognizer, неспособного
        // на постоянное прослушивание, но отлично совместимого
        // с ранними версиями
        // + проверка на аудиофокус и заглушка встроенных
        // звуковых сигналов старта и конца записи
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
        // Возвращение уведомления пользователю о обновлённом sleep/listening
        // состоянии recognizer'а
        val notificationBuilder =
            NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ask_icon_app)
                .setContentTitle(
                    if (sleepMode) getString(R.string.sleep_notification_title)
                    else getString(R.string.listening_notification_title)
                )
                .setContentText(
                    if (sleepMode) getString(R.string.sleep_notification_text)
                    else getString(R.string.listening_notification_text)
                )
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)


        val notificationIntent = Intent(context, AskActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
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
        running = true
        Log.d(Constants.MY_TAG, "CSR: onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
        SoundEffects.getInstance().run {
            executeEffect(SoundEffects.SOUND_OFF)
            setOtherSoundsMuting(false)
        }
        running = false
        Log.d(Constants.MY_TAG, "CSR: onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(
            Constants.MY_TAG,
            "CSR: onStartCommand ${intent?.getStringExtra(Constants.INTENT_EXTRA_SWITCH_RECOGNIZER_MODE)}"
        )

        if (intent != null) { // Поведение в зависимости от интента
            when (intent.getStringExtra(Constants.INTENT_EXTRA_SWITCH_RECOGNIZER_MODE)) {
                Constants.INTENT_EXTRA_MODE_LISTENING -> { // интент - приказ слушать
                    sleepMode = false
                    notifyUserModeSwitched()
                }
                Constants.INTENT_EXTRA_MODE_SLEEP -> { // интент - приказ спать
                    sleepMode = true
                    notifyUserModeSwitched()
                }
                Constants.INTENT_EXTRA_MODE_NONE -> { // интент - запуск
                    createNotificationChannelIfNeeded()
                    startForeground(Constants.NOTIFICATION_ID, buildNotification(this, sleepMode))

                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
                    continueSpeechRecognition()

                    SoundEffects.getInstance()
                        .run {
                            setVolumeLevel(((getMaxVolumeLevel() - getMinVolumeLevel()) / 2f + getMinVolumeLevel()).toInt())
                            executeEffect(SoundEffects.SOUND_ON)
                        }
                }
            }
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
        // Отправка строки разпознанной речи на обработку
        // классу-обработчику. //TODO: обрабатывать в другом потоке / корутине
        RecognizerProcessor.processResult(this, recognizedString, sleepMode)

        continueSpeechRecognition()
    }

    override fun onPartialResults(p0: Bundle?) {}

    override fun onEvent(p0: Int, p1: Bundle?) {}
}