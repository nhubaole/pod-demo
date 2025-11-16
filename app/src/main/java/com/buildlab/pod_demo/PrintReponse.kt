package com.buildlab.pod_demo

import com.google.gson.annotations.SerializedName

data class PrintResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("dataset_name")
    val datasetName: String,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("male_image_url")
    val maleImageUrl: String?,

    @SerializedName("female_image_url")
    val femaleImageUrl: String?,

    @SerializedName("image_url")
    val imageUrl: String?
)
