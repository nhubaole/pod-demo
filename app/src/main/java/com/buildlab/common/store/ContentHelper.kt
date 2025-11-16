package com.buildlab.common.store

import com.buildlab.common.support.FileManager
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ContentHelper(
    filePath: String,
    fileManager: FileManager,
) {
    private val file by lazy { fileManager.getFile(filePath) }

    fun read(): String {
        try {
            val stream = FileInputStream(file)
            val reader = InputStreamReader(stream)

            val content = reader.readText()

            reader.close()
            stream.close()

            return content
        } catch (e: Exception) {
            //e.printStackTrace()
        }
        return ""
    }

    fun write(content: String) {
        try {
            val stream = FileOutputStream(file)
            val writer = OutputStreamWriter(stream)

            writer.write(content)

            writer.close()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}