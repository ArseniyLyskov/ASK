import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import ru.myitschool.ask.Constants
import ru.myitschool.ask.ContinuousSpeechRecognition
import ru.myitschool.ask.R
import ru.myitschool.ask.layer_UI.AskActivity


class RecognizerFragment : Fragment() {
    private lateinit var powerButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recognizer, container, false)

        powerButton = view.findViewById(R.id.power_button)

        powerButton.setOnClickListener {
            powerButton.setBackgroundResource(R.drawable.power_button_color_transition)
            val transition = powerButton.background as TransitionDrawable
            val transitionTime = Constants.COLOR_TRANSITION_TIME
            transition.resetTransition()

            if (ContinuousSpeechRecognition.isRunning()) {
                transition.startTransition(0)
                transition.reverseTransition(transitionTime)
            } else {
                transition.startTransition(transitionTime)
            }

            (activity as AskActivity).turnRecognizer(turnOn = !ContinuousSpeechRecognition.isRunning())
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (ContinuousSpeechRecognition.isRunning()) {
            powerButton.setBackgroundResource(R.drawable.oval_blue)
        } else {
            powerButton.setBackgroundResource(R.drawable.oval_grey)
        }
    }

}