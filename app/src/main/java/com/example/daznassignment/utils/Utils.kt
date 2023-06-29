package com.example.daznassignment.utils

import android.content.Context
import com.example.daznassignment.data.VideoData
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import java.io.IOException


suspend fun getJsonDataFromAsset(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return jsonString
}

suspend fun readJsonFromLocal(jsonPath:String, dataClass:Any, context: Context):Resource<Any> {
    return try {
        val json = getJsonDataFromAsset(context, jsonPath)
        Resource.Success((Gson().fromJson(json, dataClass::class.java)))
    }catch (e:Exception){
        Resource.Error(e.message.toString())
    }
}