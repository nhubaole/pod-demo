package com.buildlab.repository

import GeneralService
import java.io.File

class PrintOnDemandRepository(
    private val service: GeneralService = GeneralService()
) {

    suspend fun fetchDatasets() =
        service.getDatasets().datasets

    suspend fun addDataset(
        datasetName: String,
        regMale: File,
        regFemale: File,
        origMale: File,
        origFemale: File
    ) = service.addDataset(datasetName, regMale, regFemale, origMale, origFemale)

    suspend fun printSticker(
        datasetName: String,
        sticker: File,
        gender: String = "both",
        keepWhite: Boolean = false
    ) = service.printSticker(datasetName, sticker, gender, keepWhite)

    suspend fun downloadImage(url: String) =
        service.downloadImage(url)
}