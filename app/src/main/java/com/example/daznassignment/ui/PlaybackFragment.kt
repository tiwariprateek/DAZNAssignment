package com.example.daznassignment.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import com.example.daznassignment.data.VideoDataItem
import com.example.daznassignment.databinding.FragmentPlaybackBinding
import com.example.daznassignment.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PlaybackFragment : Fragment() {
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentPlaybackBinding.inflate(layoutInflater)
    }
    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private val TAG = "PlaybackFragment"


    private val viewModel by activityViewModels<VideoViewModel>()
    private lateinit var videoData :VideoDataItem
    private var videoList = listOf<MediaItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener("video_data"){_, bundle ->
            mediaItemIndex = bundle.getInt("index")
        }
        Log.d(TAG, "onViewCreated: viewmodel $viewModel")
        viewModel.videos.observe(viewLifecycleOwner){
            when(it) {
                is Resource.Success -> {
                    videoList = it.data!!.map {
                            video -> video?.uri?.let {v ->
                            MediaItem.fromUri(v) }!!
                    }

                }
                is Resource.Error -> {
                    Snackbar.make(requireView(),it.message.toString(),Snackbar.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    Snackbar.make(requireView(),"Loading",Snackbar.LENGTH_SHORT).show()

                }
            }
        }




    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
                exoPlayer.setMediaItems(videoList, mediaItemIndex, playbackPosition)
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