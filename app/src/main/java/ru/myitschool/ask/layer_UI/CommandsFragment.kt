package ru.myitschool.ask.layer_UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.myitschool.ask.R

class CommandsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_commands, container, false)
        //TODO: к завершению проекта описать здесь голосовые команды и прочее "how to"
        return view
    }
}