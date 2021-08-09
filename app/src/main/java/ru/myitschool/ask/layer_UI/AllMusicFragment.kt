package ru.myitschool.ask.layer_UI

import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import ru.myitschool.ask.Constants
import ru.myitschool.ask.R
import ru.myitschool.ask.adapters.MusicRecyclerAdapter
import ru.myitschool.ask.models.MusicComposition

class AllMusicFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_music, container, false)
    }

    override fun onResume() {
        super.onResume()

        // Возврат при отсутствии разрешения на чтение хранилища устройства
        val activity = requireActivity()
        if (!Constants.isAllPermissionsGranted(requireContext())) {
            Constants.requestPermissions(activity)
            val tabLayout = activity.findViewById<TabLayout>(R.id.tab_layout)
            tabLayout.selectTab(tabLayout.getTabAt(0), true)
            return
        }

        // Получение музыки и вывод на экран через адаптер в recyclerView
        val recyclerView: RecyclerView = activity.findViewById(R.id.all_music_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = MusicRecyclerAdapter(getAllMusic())
    }

    private fun getAllMusic(): ArrayList<MusicComposition> {
        // Чтение всей музыки с устройства

        val music = ArrayList<MusicComposition>()

        val contentResolver = requireActivity().contentResolver
        val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val songCursor = contentResolver.query(songUri, null, null, null, null)

        if (songCursor != null && songCursor.moveToFirst()) {
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songPath = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)

            do {
                val currentTitle = songCursor.getString(songTitle)
                val currentArtist = songCursor.getString(songArtist)
                val currentPath = songCursor.getString(songPath)
                // Добавление считанной музыки
                music.add(MusicComposition(currentTitle, currentArtist, currentPath))
            } while (songCursor.moveToNext()) // пока есть что считывать
        }

        return music
    }

}