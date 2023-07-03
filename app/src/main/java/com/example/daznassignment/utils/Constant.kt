package com.example.daznassignment.utils

import androidx.datastore.preferences.core.intPreferencesKey

const val ACTION_PLAY = "play"
const val ACTION_PAUSE = "pause"
const val ACTION_NEXT = "next"
const val ACTION_PREVIOUS = "previous"
const val TAG_PLAYBACKFRAGMENT = "PlaybackFragment"
const val TAG_HOMEFRAGMENT = "HomeFragment"
const val FRAGMENT_RESULT_REQUEST_KEY = "video_data"
const val FRAGMENT_RESULT_DATA_KEY = "index"
const val DATASTORE_NAME = "video_analytics"
val PLAY_COUNT_KEY = intPreferencesKey("play_count")
val PAUSE_COUNT_KEY = intPreferencesKey("pause_count")
val NEXT_COUNT_KEY = intPreferencesKey("next_count")
val PREV_COUNT_KEY = intPreferencesKey("prev_count")