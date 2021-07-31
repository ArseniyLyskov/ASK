package ru.myitschool.ask

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ru.myitschool.ask.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), RecognitionListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var speechRecognizer: SpeechRecognizer

    companion object {
        private const val MY_TAG = "MyTag"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 10)
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
        speechRecognizer.setRecognitionListener(this)

        binding.button.setOnClickListener {
            binding.text.text = ""
            binding.button.visibility = View.INVISIBLE
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5)
            }
            speechRecognizer.startListening(intent)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        super.onDestroy()
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(MY_TAG, "onReadyForSpeech")
        binding.text.text = "Ready for speech"
    }

    override fun onBeginningOfSpeech() {
        Log.d(MY_TAG, "onBeginningOfSpeech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        Log.d(MY_TAG, "onRmsChanged")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.d(MY_TAG, "onBufferReceived")
    }

    override fun onEndOfSpeech() {
        Log.d(MY_TAG, "onEndOfSpeech")
    }

    override fun onError(error: Int) {
        Log.d(MY_TAG, "error $error")
        binding.text.text = "error $error"
        binding.button.visibility = View.VISIBLE
    }

    override fun onResults(results: Bundle) {
        var str = "Heard: \n"
        Log.d(MY_TAG, "onResults $results")
        val data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
        for (i in 0 until data.size) {
            Log.d(MY_TAG, "result " + data[i])
            str += "Version ${i + 1}: ${data[i]}\n"
        }
        binding.text.text = str
        binding.button.visibility = View.VISIBLE
    }

    override fun onPartialResults(partialResults: Bundle?) {
        Log.d(MY_TAG, "onPartialResults")
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d(MY_TAG, "onEvent $eventType")
    }
}