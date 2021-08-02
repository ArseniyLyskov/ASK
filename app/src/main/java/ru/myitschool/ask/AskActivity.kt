package ru.myitschool.ask

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ru.myitschool.ask.databinding.ActivityMainBinding

class AskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private fun isAllPermissionsGranted() = Constants.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionsAvailability(activity: Activity) {
        if (!isAllPermissionsGranted()) {
            ActivityCompat.requestPermissions(activity, Constants.REQUIRED_PERMISSIONS, 10)
        }
    }

    private fun checkListeningAvailability(context: Context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissionsAvailability(this)
        checkListeningAvailability(this)

        binding.start.setOnClickListener {
            createNotificationChannelIfNeeded()
            startService(Intent(this, ContinuousSpeechRecognition::class.java))
        }

        binding.stop.setOnClickListener {
        }
    }
}