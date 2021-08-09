package ru.myitschool.ask.adapters

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.myitschool.ask.Constants
import ru.myitschool.ask.R
import ru.myitschool.ask.models.MusicComposition

class MusicRecyclerAdapter(private val music: ArrayList<MusicComposition>) :
    RecyclerView.Adapter<MusicRecyclerAdapter.MusicViewHolder>() {

    class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleView: TextView = itemView.findViewById(R.id.item_title)
        var artistView: TextView = itemView.findViewById(R.id.item_artist)
        lateinit var path: String

        init {
            itemView.setOnLongClickListener {

                // Воспроизведение музыки при долгом нажатии
                // TODO: реализация через SoundEffects, совместимость с recognizer'ом
                Log.d(Constants.MY_TAG, path)
                MediaPlayer.create(itemView.context, Uri.parse("file:///$path")).start()

                return@setOnLongClickListener true
            }

            itemView.setOnClickListener {
                // TODO: при коротком нажатии перемещение музыки в раздел прослшиваемых recognizer'ом
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)
        return MusicViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.titleView.text = "Title: ${music[position].title}"
        holder.artistView.text = "Artist: ${music[position].artist}"
        holder.path = music[position].path
    }

    override fun getItemCount() = music.size

}