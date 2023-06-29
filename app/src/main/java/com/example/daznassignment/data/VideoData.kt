package com.example.daznassignment.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class VideoData(

	@field:SerializedName("VideoData")
	val videoData: List<VideoDataItem?>? = null
) : Parcelable

@Parcelize
data class VideoDataItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("uri")
	val uri: String? = null
) : Parcelable
