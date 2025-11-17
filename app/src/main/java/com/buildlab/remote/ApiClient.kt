package com.buildlab.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    const val BASE_URL = "http://13.250.17.198:7123/"
}