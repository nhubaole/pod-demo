@file:Suppress(IMPORTANT)

package com.buildlab.common.store

import com.buildlab.common.support.FileManager
import com.buildlab.utils.IMPORTANT
import com.buildlab.utils.Logger
import com.buildlab.utils.getList
import com.buildlab.utils.getSet
import com.buildlab.utils.putCollection
import org.json.JSONObject

open class JsonDataStore(
    fileStore: String,
    fileManager: FileManager,
) : DataStore {
    private val contentHelper = ContentHelper(fileStore, fileManager)
    private var json = JSONObject()

    override fun load() {
        try {
            json = JSONObject(contentHelper.read())
        } catch (e: Exception) {
            Logger.stacktrace("JsonDataStore", "Failed to read ${this.javaClass.simpleName}", e)
        }
    }

    override fun save() {
        contentHelper.write(json.toString())
    }

    protected fun <T> String.setCollection(
        values: Collection<T>, transformer: ((T) -> JSONObject)? = null
    ) {
        if (transformer != null) {
            json.putCollection(this, values, transformer)
        } else {
            json.putCollection(this, values)
        }
    }

    protected fun <T> String.getCollectionAsSet(
        transformer: ((JSONObject) -> T)? = null
    ): Set<T> {
        return if (transformer != null) {
            json.getSet(this, transformer)
        } else {
            json.getSet(this)
        }
    }

    protected fun <T> String.getCollectionAsList(
        transformer: ((JSONObject) -> T)? = null
    ): List<T> {
        return if (transformer != null) {
            json.getList(this, transformer)
        } else {
            json.getList(this)
        }
    }

    protected fun String.setValue(value: Any) {
        json.put(this, value)
    }

    protected fun String.getInt(): Int {
        return json.optInt(this, 0)
    }

    protected fun String.getLong(): Long {
        return json.optLong(this, 0)
    }

    protected fun String.getFloat(): Float {
        return json.optDouble(this, 0.0).toFloat()
    }

    protected fun String.getDouble(): Double {
        return json.optDouble(this, 0.0)
    }

    protected fun String.getBoolean(): Boolean {
        return json.optBoolean(this, false)
    }

    protected fun String.getString(): String {
        return json.optString(this)
    }
}