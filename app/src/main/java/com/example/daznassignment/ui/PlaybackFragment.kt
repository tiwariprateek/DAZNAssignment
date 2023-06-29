package com.example.daznassignment.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.daznassignment.R
import com.example.daznassignment.adapters.VideoListAdapter
import com.example.daznassignment.databinding.FragmentHomeBinding
import com.example.daznassignment.databinding.FragmentPlaybackBinding

class PlaybackFragment : Fragment() {
    private var player: ExoPlayer? = null
    private var _binding: FragmentPlaybackBinding? = null
    private val binding get() = _binding!!

    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L


    private val viewModel by viewModels<VideoViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaybackBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
                val mediaItem = MediaItem.fromUri("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears_uhd.mpd")
                exoPlayer.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(requireActivity().window, binding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }



    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            mediaItemIndex = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }
    override fun onStart() {
        super.onStart()
        initializePlayer()

    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        initializePlayer()

    }
    public override fun onPause() {
        super.onPause()
        releasePlayer()

    }


    public override fun onStop() {
        super.onStop()
        releasePlayer()

    }

}