package com.example.daznassignment.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.daznassignment.R
import com.example.daznassignment.data.VideoDataItem
import com.example.daznassignment.databinding.VideoRowBinding
import wseemann.media.FFmpegMediaMetadataRetriever


class VideoListAdapter(private val onNoteClicked: (VideoDataItem) -> Unit) :
    ListAdapter<VideoDataItem, VideoListAdapter.CharacterViewHolder>(ComparatorDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = VideoRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val video = getItem(position)
        video?.let {
            holder.bind(it)
        }
    }

    inner class CharacterViewHolder(private val binding: VideoRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(video: VideoDataItem) {
            val url = "https://static.videezy.com/system/resources/previews/000/000/248/original/http-bing.mp4"
            val requestOptions = RequestOptions()
            binding.videoTitle.text = video.name
            Glide.with(itemView.context)
                .load(video.uri)
                .error(R.drawable.youtube)
//                .apply(requestOptions)
//                .thumbnail(Glide.with(itemView.context).load(url))
                .into(binding.videoThumbnail)
            binding.root.setOnClickListener {
                onNoteClicked(video)
            }
        }

        private fun getThumbnails(uri:String):Bitmap{
            val mmr = FFmpegMediaMetadataRetriever()
            mmr.setDataSource(uri)
            mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM)
            mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST)
            val b = mmr.getFrameAtTime(
                2000000,
                FFmpegMediaMetadataRetriever.OPTION_CLOSEST
            ) // frame at 2 seconds
            val artwork = mmr.embeddedPicture
            mmr.release()
            return b
        }

    }

    class ComparatorDiffUtil : DiffUtil.ItemCallback<VideoDataItem>() {
        override fun areItemsTheSame(oldItem: VideoDataItem, newItem: VideoDataItem): Boolean {
            return oldItem.uri== newItem.uri
        }

        override fun areContentsTheSame(oldItem: VideoDataItem, newItem: VideoDataItem): Boolean {
            return oldItem == newItem
        }
    }
}