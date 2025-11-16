package com.buildlab.common.support

interface NetworkSupporter {
    fun isNetworkConnected(): Boolean

    suspend fun awaitConnection()
}