package com.buildlab.app

import android.app.Application
import com.buildlab.common.concurrency.AppDispatchers
import kotlinx.coroutines.Dispatchers

class MainApplication: Application() {
    init {
        AppDispatchers.initialize(
            ioDispatcher = Dispatchers.IO,
            mainDispatcher = Dispatchers.Main,
        )
    }
}