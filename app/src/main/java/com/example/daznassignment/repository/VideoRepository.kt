package com.example.daznassignment.repository

import android.content.Context
import com.example.daznassignment.data.VideoData
import com.example.daznassignment.utils.readJsonFromLocal
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class VideoRepository @Inject constructor(@ApplicationContext val context: Context) {


    suspend fun getVideoData() = readJsonFromLocal("video_uri.json", VideoData(),context)



}