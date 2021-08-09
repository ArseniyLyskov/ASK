package ru.myitschool.ask.layer_UI

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import ru.myitschool.ask.Constants
import ru.myitschool.ask.R
import ru.myitschool.ask.SoundEffects
import ru.myitschool.ask.adapters.SectionsPagerAdapter
import ru.myitschool.ask.separate_processes.ContinuousSpeechRecognition

class AskActivity : AppCompatActivity() {

    fun turnRecognizer(turnOn: Boolean) {
        // Отправка сервису интента с инфо о смене вкл/выкл
        val intent = Intent(this, ContinuousSpeechRecognition::class.java)
        intent.putExtra(Constants.INTENT_EXTRA_SWITCH_RECOGNIZER_MODE, Constants.INTENT_EXTRA_MODE_NONE)
        if (turnOn)
            startService(intent)
        else
            stopService(intent)
    }

    private fun setupTabLayout(context: Context) {
        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        viewPager.adapter = SectionsPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.icon = ContextCompat.getDrawable(context, Constants.getTabContent()[position])
        }.attach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask)
        setupTabLayout(this)

        // Инициализация синглтона с параметром, коряво)
        SoundEffects.createInstance(this)
    }
}