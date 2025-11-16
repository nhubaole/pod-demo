package com.buildlab.pod_demo

import java.io.File

class PrintOnDemandRepository(private val api: ApiService = ApiService()) {

    suspend fun fetchDatasets(): List<DatasetItem> {
        return api.getDatasets().datasets
    }

    suspend fun addDataset(
        datasetName: String,
        regMale: File,
        regFemale: File,
        origMale: File,
        origFemale: File
    ): String {
        return api.addDataset(datasetName, regMale, regFemale, origMale, origFemale)
    }

    suspend fun printSticker(
        datasetName: String,
        sticker: File,
        gender: String = "both",
        keepWhite: Boolean = false
    ): PrintResponse {
        return api.printSticker(datasetName, sticker, gender, keepWhite)
    }

    suspend fun downloadImage(url: String) : ByteArray {
        return api.downloadImage(url)
    }
}
