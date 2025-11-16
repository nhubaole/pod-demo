@file:Suppress(IMPORTANT)

package com.buildlab.utils

import java.util.Locale.US


fun Double.toString(place: Long): String {
    return String.format(US, "%.${place}f", this)
}

fun Double.take(place: Long): Double {
    return String.format(US, "%.${place}f", this).toDouble()
}

fun String.safeToInt(): Int {
    return try {
        toInt()
    } catch (e: Exception) {
        0
    }
}

fun String.safeToDouble(): Double {
    return try {
        toDouble()
    } catch (e: Exception) {
        0.0
    }
}