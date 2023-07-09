package com.example.daznassignment.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.example.daznassignment.data.PlaybackCount
import com.example.daznassignment.data.VideoDataItem
import com.example.daznassignment.databinding.FragmentPlaybackBinding
import com.example.daznassignment.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*


@AndroidEntryPoint
class PlaybackFragment : Fragment() {


    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentPlaybackBinding.inflate(layoutInflater)
    }
    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var mediaItemUrl = ""
    private var playbackPosition = 0L
    private val TAG = TAG_PLAYBACKFRAGMENT


    private val viewModel by activityViewModels<VideoViewModel>()
    private var videos = listOf<VideoDataItem?>()
    private var videoList = listOf<MediaItem>()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var pastIndex = 0

    private var pauseCount = 0
    private var nextCount = 0
    private var prevCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(FRAGMENT_RESULT_REQUEST_KEY) { _, bundle ->
            mediaItemIndex = bundle.getInt(FRAGMENT_RESULT_INDEX_KEY)
            mediaItemUrl = bundle.getString(FRAGMENT_RESULT_URL_KEY).toString()
        }

        viewModel.videos.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    it.data?.let {video ->
                        videos = video
                    }
                    binding.progressBar.visibility = View.GONE
                    videoList = it.data!!.map { video ->
                        video?.uri?.let { v ->
                            MediaItem.fromUri(v)
                        }!!
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE

                }
            }
        }
    }



    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun getExoAnalytics(exoPlayer: ExoPlayer) {
        exoPlayer.addAnalyticsListener(object : AnalyticsListener {
            override fun onMediaItemTransition(
                eventTime: AnalyticsListener.EventTime,
                mediaItem: MediaItem?,
                reason: Int
            ) {
                super.onMediaItemTransition(eventTime, mediaItem, reason)
                val index = videoList.indexOf(mediaItem)
                if (pastIndex < index) {
                    nextCount++
                    binding.playbackCount = PlaybackCount(pauseCount, nextCount, prevCount)
                    Log.d(TAG, "onMediaItemTransition: Next video")
                    sendAnalytics(ACTION_PREVIOUS, mediaItem.toString())
                } else {
                    prevCount++
                    binding.playbackCount = PlaybackCount(pauseCount, nextCount, prevCount)
                    Log.d(TAG, "onMediaItemTransition: Prev video")
                    sendAnalytics(ACTION_NEXT, mediaItem.toString())
                }
            }

            override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
                super.onIsPlayingChanged(eventTime, isPlaying)
                if (isPlaying) {
                    pastIndex = exoPlayer.currentMediaItemIndex
                    sendAnalytics(ACTION_PLAY, exoPlayer.currentMediaItem.toString())
                    Log.d(TAG, "onMediaItemTransition: Playing video")
                } else {
                    pauseCount++
                    binding.playbackCount = PlaybackCount(pauseCount, nextCount, prevCount)
                    sendAnalytics(ACTION_PAUSE, exoPlayer.currentMediaItem.toString())
                    Log.d(TAG, "onMediaItemTransition: Paused video")
                }
            }
        })
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
                exoPlayer.setMediaItems(videoList, mediaItemIndex, playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                getExoAnalytics(exoPlayer)
                exoPlayer.prepare()
            }
    }


    private fun sendAnalytics(event: String, videoUrl: String) {
        firebaseAnalytics.logEvent(event) {
            param("VideoURL", videoUrl)
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