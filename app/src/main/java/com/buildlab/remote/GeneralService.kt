package com.buildlab.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GeneralService(
    private val httpClient: OkHttpClient
) {
    fun getMockupPreview(): String {
        val request = Request.Builder()
            .url("")
            .post(
                JSONObject().apply {

                }.toString().toRequestBody("application/json".toMediaType())
            ).build()

        val responseStr = httpClient.newCall(request).execute().body?.string()
            ?: throw Exception("Response is empty")

        return JSONObject(responseStr)
            .apply {

            }
            .getJSONObject("data")
            .getString("")
    }
}