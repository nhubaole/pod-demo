package com.buildlab.pod_demo

data class DatasetItem(
    val dataSetName: String,
    val originalName: String,
    val path: String
)

data class DatasetsResponse(
    val datasets: List<DatasetItem>
)
