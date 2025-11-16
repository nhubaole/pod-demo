package com.buildlab.common.concurrency

import kotlinx.coroutines.CoroutineDispatcher

class AppDispatchers(
    private val ioDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher,
) {
    companion object {
        private lateinit var INSTANCE: AppDispatchers

        fun initialize(
            ioDispatcher: CoroutineDispatcher,
            mainDispatcher: CoroutineDispatcher,
        ) {
            if (Companion::INSTANCE.isInitialized) return
            INSTANCE = AppDispatchers(
                ioDispatcher = ioDispatcher,
                mainDispatcher = mainDispatcher,
            )
        }

        val IO: CoroutineDispatcher get() = INSTANCE.ioDispatcher
        val MAIN: CoroutineDispatcher get() = INSTANCE.mainDispatcher
    }
}