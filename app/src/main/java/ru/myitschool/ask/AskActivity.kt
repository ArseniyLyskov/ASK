package ru.myitschool.ask

import android.app.Activity
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

    private fun requestPermissionsIfNeeded(activity: Activity) {
        if (!isAllPermissionsGranted()) {
            ActivityCompat.requestPermissions(activity, Constants.REQUIRED_PERMISSIONS, 10)
        } //TODO: check result
    }

    private fun checkListeningAvailability(context: Context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return //TODO: explanation
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissionsIfNeeded(this)
        checkListeningAvailability(this)
        SoundEffects.createInstance(this)

        binding.start.setOnClickListener {
            startService(Intent(this, ContinuousSpeechRecognition::class.java))
        }

        binding.stop.setOnClickListener {
        }
    }
}