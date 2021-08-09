package ru.myitschool.ask.layer_UI

import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import ru.myitschool.ask.Constants
import ru.myitschool.ask.R
import ru.myitschool.ask.separate_processes.ContinuousSpeechRecognition


class PowerFragment : Fragment() {
    private lateinit var powerButton: ImageButton

    private fun isRecognitionAvailable(context: Context): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    private fun showErrorDialog(context: Context) {
        // Вывод сообщения об ошибке
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.error))
            .setMessage(R.string.error_recognition_unavailable)
            .setPositiveButton("ОК") { dialog, _ ->
                dialog.cancel()
            }
        builder.create().show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recognizer, container, false)

        powerButton = view.findViewById(R.id.power_button)

        powerButton.setOnClickListener {

            // Сервис не запускается без разрешений
            if (!Constants.isAllPermissionsGranted(requireContext())) {
                Constants.requestPermissions(requireActivity())
                return@setOnClickListener
            }

            // И не запускается при отсутствии встроенных средств распознавания
            // (Здесь, как правило, недопущены эмуляторы)
            if (!isRecognitionAvailable(requireContext())) {
                showErrorDialog(requireContext())
                return@setOnClickListener
            }

            // "Анимирование" кнопки power (on/off)
            it.setBackgroundResource(R.drawable.power_button_color_transition)
            val transition = powerButton.background as TransitionDrawable
            val transitionTime = Constants.COLOR_TRANSITION_TIME
            transition.resetTransition()

            if (ContinuousSpeechRecognition.isRunning()) {
                transition.startTransition(0)
                transition.reverseTransition(transitionTime)
            } else {
                transition.startTransition(transitionTime)
            }

            // Смена вкл/выкл сервиса
            (activity as AskActivity).turnRecognizer(turnOn = !ContinuousSpeechRecognition.isRunning())
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Отображение актуального состояния сервиса
        if (ContinuousSpeechRecognition.isRunning()) {
            powerButton.setBackgroundResource(R.drawable.oval_blue)
        } else {
            powerButton.setBackgroundResource(R.drawable.oval_grey)
        }
    }

}