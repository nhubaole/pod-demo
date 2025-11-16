@file:Suppress(IMPORTANT)

package com.buildlab.utils

import java.util.Calendar
import java.util.TimeZone

private val LOCAL_TIME_ZONE = TimeZone.getTimeZone("Asia/Bangkok")
private val GLOBAL_TIME_ZONE = TimeZone.getTimeZone("UTC")

fun getLocalCalendar(): Calendar {
    return Calendar.getInstance(LOCAL_TIME_ZONE)
}

fun getGlobalCalendar(): Calendar {
    return Calendar.getInstance(GLOBAL_TIME_ZONE)
}

fun convertGlobalMillisToLocalSecs(timeInMillis: Long): Long {
    return timeInMillis / 1000 + (7 * 60 * 60)
}

class SimpleDate private constructor(
    private val calendar: Calendar
) {
    companion object {
        fun fromLocal(timeInMillis: Long): SimpleDate {
            return SimpleDate(getLocalCalendar().apply {
                this.timeInMillis = timeInMillis
            })
        }

        fun fromGlobal(timeInMillis: Long): SimpleDate {
            return SimpleDate(getGlobalCalendar().apply {
                this.timeInMillis = timeInMillis
            })
        }
    }

    fun getHour() = calendar.get(Calendar.HOUR_OF_DAY)

    fun getMinute() = calendar.get(Calendar.MINUTE)

    fun getSecond() = calendar.get(Calendar.SECOND)

    fun getDay() = calendar.get(Calendar.DAY_OF_MONTH)

    fun getMonth() = calendar.get(Calendar.MONTH)

    fun getYear() = calendar.get(Calendar.YEAR)
}