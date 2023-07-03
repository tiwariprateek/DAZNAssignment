package com.example.daznassignment.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import com.example.daznassignment.data.PlaybackAnalytics
import com.example.daznassignment.data.VideoDataItem
import com.example.daznassignment.databinding.FragmentPlaybackBinding
import com.example.daznassignment.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class PlaybackFragment : Fragment() {
    private lateinit var events: PlaybackAnalytics
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentPlaybackBinding.inflate(layoutInflater)
    }
    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private val TAG = TAG_PLAYBACKFRAGMENT


    private val viewModel by activityViewModels<VideoViewModel>()
    private lateinit var videoData: VideoDataItem
    private var videoList = listOf<MediaItem>()
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val Context.dataStore by preferencesDataStore(
        name = DATASTORE_NAME
    )
    private var playCount = 0
    private var pauseCount = 0
    private var nextCount = 0
    private var prevCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


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
        lifecycleScope.launch {
            events = getLocalEventCount()
            playCount = events.playCount
            pauseCount = events.pauseCount
            nextCount = events.nextCount
            prevCount = events.prevCount
            withContext(Main){
                binding.pauseCount.text = pauseCount.toString()
                binding.backwardCount.text = prevCount.toString()
                binding.forwardCount.text = nextCount.toString()
            }
        }



        setFragmentResultListener(FRAGMENT_RESULT_REQUEST_KEY) { _, bundle ->
            mediaItemIndex = bundle.getInt(FRAGMENT_RESULT_DATA_KEY)
        }

        Log.d(TAG, "onViewCreated: viewmodel $viewModel")
        viewModel.videos.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    videoList = it.data!!.map { video ->
                        video?.uri?.let { v ->
                            MediaItem.fromUri(v)
                        }!!
                    }

                }
                is Resource.Error -> {
                    Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    Snackbar.make(requireView(), "Loading", Snackbar.LENGTH_SHORT).show()

                }
            }
        }


    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun getExoAnalytics(exoPlayer: ExoPlayer) {
        exoPlayer.addAnalyticsListener(object : AnalyticsListener {
            override fun onMediaItemTransition(
                eventTime: EventTime,
                mediaItem: MediaItem?,
                reason: Int
            ) {
                super.onMediaItemTransition(eventTime, mediaItem, reason)
                val index = videoList.indexOf(mediaItem)
                Log.d(TAG, "onMediaItemTransition: index $index currentMedia $mediaItemIndex")
                if (mediaItemIndex > index) {
                    sendAnalytics(ACTION_PREVIOUS, mediaItem.toString())
                    saveEventsLocally(PlaybackAnalytics(playCount = prevCount++))
                } else {
                    sendAnalytics(ACTION_NEXT, mediaItem.toString())
                    saveEventsLocally(PlaybackAnalytics(playCount = nextCount++))
                }
                Log.d(TAG, "onMediaItemTransition: Next video $reason")
            }

            override fun onIsPlayingChanged(eventTime: EventTime, isPlaying: Boolean) {
                super.onIsPlayingChanged(eventTime, isPlaying)
                if (isPlaying) {
                    saveEventsLocally(PlaybackAnalytics(playCount = playCount++))
                    sendAnalytics(ACTION_PLAY, exoPlayer.currentMediaItem.toString())
                    Log.d(TAG, "onMediaItemTransition: Playing video")
                } else {
                    saveEventsLocally(PlaybackAnalytics(pauseCount = pauseCount++))
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

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(
            requireActivity().window,
            binding.videoView
        ).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun sendAnalytics(event: String, videoUrl: String) {
        firebaseAnalytics.logEvent(event) {
            param("VideoURL", videoUrl)
        }
    }

    private suspend fun getLocalEventCount():PlaybackAnalytics {
        val data = requireContext().dataStore.data.first()
        return PlaybackAnalytics(
            playCount = data[PLAY_COUNT_KEY] ?: 0,
            pauseCount = data[PAUSE_COUNT_KEY] ?: 0,
            nextCount = data[NEXT_COUNT_KEY] ?: 0,
            prevCount = data[PREV_COUNT_KEY] ?: 0
        )
}

    private fun saveEventsLocally(events:PlaybackAnalytics) {
        lifecycleScope.launch(IO) {
            requireContext().dataStore.edit {
                it[PLAY_COUNT_KEY] = events.playCount
                it[PAUSE_COUNT_KEY] = events.pauseCount
                it[NEXT_COUNT_KEY] = events.nextCount
                it[PREV_COUNT_KEY] = events.prevCount
            }
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