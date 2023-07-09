package com.example.daznassignment.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daznassignment.data.VideoData
import com.example.daznassignment.data.VideoDataItem
import com.example.daznassignment.repository.VideoRepository
import com.example.daznassignment.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(private val repository: VideoRepository)  :ViewModel() {


    private val _videos = MutableLiveData<Resource<List<VideoDataItem?>>>()
    val videos: LiveData<Resource<List<VideoDataItem?>>>
        get() = _videos


    init {
        viewModelScope.launch(Default) {
            getVideosData()
        }
    }


    private suspend fun getVideosData(){
        _videos.postValue(Resource.Loading())
        val data = repository.getVideoData()
        when(data){
            is Resource.Success -> {
                val videos = data.data as VideoData
                if (videos.videoData!!.isNotEmpty())
                    videos.videoData.let {
                        _videos.postValue(Resource.Success(it))
                    }
            }
            is Resource.Error -> _videos.postValue(Resource.Error(data.message))
            is Resource.Loading -> _videos.postValue(Resource.Loading())
        }
    }


}