package com.buildlab.pod_demo

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    // CHANGE THIS TO YOUR MAC LAN IP
    const val BASE_URL = "http://192.168.1.9:8000/"
}
