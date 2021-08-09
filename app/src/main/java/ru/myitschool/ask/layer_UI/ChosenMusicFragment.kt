package ru.myitschool.ask.layer_UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.myitschool.ask.R

class ChosenMusicFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chosen_music, container, false)
        //TODO: здесь будет реализован список музыки, отобранный пользователем для прослушивания recognizer'ом
        // "Включи гимн России!" - если в списке отобранных произведений есть "гимн россии" - включается
        return view
    }

}