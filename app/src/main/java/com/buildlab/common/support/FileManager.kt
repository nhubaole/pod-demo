package com.buildlab.common.support

import java.io.File

interface FileManager {
    fun getFile(fileName: String): File
    fun removeFile(fileName: String)
}