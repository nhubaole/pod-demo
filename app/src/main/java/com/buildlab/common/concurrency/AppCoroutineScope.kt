package com.buildlab.common.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class AppCoroutineScope : CoroutineScope {
    override val coroutineContext: CoroutineContext by lazy {
        AppDispatchers.MAIN + SupervisorJob()
    }
}