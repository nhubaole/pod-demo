@file:Suppress(IMPORTANT)

package com.buildlab.utils

import org.json.JSONArray
import org.json.JSONObject
import java.util.function.Consumer

fun <I> JSONArray.forEachItem(action: Consumer<I>) {
    for (i in 0 until length()) {
        action.accept(get(i) as I)
    }
}

fun <E> JSONObject.getList(key: String, transformer: ((JSONObject) -> E)): List<E> {
    val collection = mutableListOf<E>()
    try {
        val jsonArr = getJSONArray(key)
        for (i in 0 until jsonArr.length()) {
            collection.add(transformer(jsonArr.getJSONObject(i)))
        }
    } catch (_: Exception) {
    }
    return collection.toList()
}

fun <E> JSONObject.getList(key: String): List<E> {
    val collection = mutableListOf<E>()
    try {
        val jsonArr = getJSONArray(key)
        for (i in 0 until jsonArr.length()) {
            collection.add(jsonArr.get(i) as E)
        }
    } catch (_: Exception) {
    }
    return collection.toList()
}

fun <E> JSONObject.getSet(key: String, transformer: ((JSONObject) -> E)): Set<E> {
    val collection = mutableSetOf<E>()
    try {
        val jsonArr = getJSONArray(key)
        for (i in 0 until jsonArr.length()) {
            collection.add(transformer(jsonArr.getJSONObject(i)))
        }
    } catch (_: Exception) {
    }
    return collection.toSet()
}

fun <E> JSONObject.getSet(key: String): Set<E> {
    val collection = mutableSetOf<E>()
    try {
        val jsonArr = getJSONArray(key)
        for (i in 0 until jsonArr.length()) {
            collection.add(jsonArr.get(i) as E)
        }
    } catch (_: Exception) {
    }
    return collection.toSet()
}

fun <E> Collection<E>.toJsonArray(transformer: ((E) -> JSONObject)): JSONArray {
    val jsonArr = JSONArray()
    forEach { jsonArr.put(transformer(it)) }
    return jsonArr
}

fun <E> Collection<E>.toJsonArray(): JSONArray {
    val jsonArr = JSONArray()
    forEach { jsonArr.put(it) }
    return jsonArr
}

fun <E> JSONObject.putCollection(
    key: String, values: Collection<E>
) {
    put(key, values.toJsonArray())
}

fun <E> JSONObject.putCollection(
    key: String, values: Collection<E>,
    transformer: ((E) -> JSONObject)
) {
    put(key, values.toJsonArray(transformer))
}