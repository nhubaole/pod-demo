package com.buildlab.pod_demo

data class PrintResponse(
    val success: Boolean,
    val datasetName: String,
    val gender: String,
    val maleImageUrl: String?,
    val femaleImageUrl: String?,
    val imageUrl: String?
)
