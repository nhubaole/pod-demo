package com.buildlab.pod_demo

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ApiService(
    private val client: OkHttpClient = ApiClient.client
) {

    private val gson = Gson()

    // ---------------------- GET /datasets ----------------------

    suspend fun getDatasets(): DatasetsResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(ApiClient.BASE_URL + "datasets")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val json = response.body?.string() ?: "{}"
            gson.fromJson(json, DatasetsResponse::class.java)
        }
    }

    // ---------------------- POST /add ----------------------

    suspend fun addDataset(
        datasetName: String,
        regMale: File,
        regFemale: File,
        origMale: File,
        origFemale: File,
        color: String = "#64634A",
        tolerance: Int = 30
    ): String = withContext(Dispatchers.IO) {

        val form = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("dataset_name", datasetName)
            .addFormDataPart("color", color)
            .addFormDataPart("tolerance", tolerance.toString())
            .addFormDataPart("regzone_male_image", regMale.name, regMale.asRequestBody("image/png".toMediaType()))
            .addFormDataPart("regzone_female_image", regFemale.name, regFemale.asRequestBody("image/png".toMediaType()))
            .addFormDataPart("origin_male_image", origMale.name, origMale.asRequestBody("image/png".toMediaType()))
            .addFormDataPart("origin_female_image", origFemale.name, origFemale.asRequestBody("image/png".toMediaType()))
            .build()

        val request = Request.Builder()
            .url(ApiClient.BASE_URL + "add")
            .post(form)
            .build()

        client.newCall(request).execute().use {
            it.body?.string() ?: ""
        }
    }

    // ---------------------- POST /print ----------------------

    suspend fun printSticker(
        datasetName: String,
        sticker: File,
        gender: String = "both",
        keepWhite: Boolean = false
    ): PrintResponse = withContext(Dispatchers.IO) {

        val form = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("dataset_name", datasetName)
            .addFormDataPart("gender", gender)
            .addFormDataPart("keep_white", keepWhite.toString())
            .addFormDataPart(
                "sticker_image",
                sticker.name,
                sticker.asRequestBody("image/png".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(ApiClient.BASE_URL + "print")
            .post(form)
            .build()

        client.newCall(request).execute().use { response ->
            val json = response.body?.string() ?: "{}"
            gson.fromJson(json, PrintResponse::class.java)
        }
    }

    // ---------------------- Download Image ----------------------

    suspend fun downloadImage(url: String): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            response.body?.bytes() ?: ByteArray(0)
        }
    }
}
