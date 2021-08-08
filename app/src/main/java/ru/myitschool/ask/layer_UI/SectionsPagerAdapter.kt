package ru.myitschool.ask.layer_UI

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.myitschool.ask.Constants

class SectionsPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = Constants.getTabContent().size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PowerFragment()
            1 -> AlarmFragment()
            2 -> ChosenMusicFragment()
            3 -> AllMusicFragment()
            4 -> CommandsFragment()
            else -> throw Exception("No fragment for this position")
        }
    }
}