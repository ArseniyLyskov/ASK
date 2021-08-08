package ru.myitschool.ask.layer_UI

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ru.myitschool.ask.Constants
import ru.myitschool.ask.ContinuousSpeechRecognition
import ru.myitschool.ask.R
import ru.myitschool.ask.SoundEffects

class AskActivity : AppCompatActivity() {

    fun turnRecognizer(turnOn: Boolean) {
        val intent = Intent(this, ContinuousSpeechRecognition::class.java)
        intent.putExtra(Constants.INTENT_EXTRA_SWITCH_RECOGNIZER_MODE, Constants.INTENT_EXTRA_MODE_NONE)
        if (turnOn)
            startService(intent)
        else
            stopService(intent)
    }

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
        setContentView(R.layout.activity_ask)

        requestPermissionsIfNeeded(this)
        checkListeningAvailability(this)
        SoundEffects.createInstance(this)
    }
}