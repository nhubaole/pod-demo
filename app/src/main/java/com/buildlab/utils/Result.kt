@file:Suppress(IMPORTANT)

package com.buildlab.utils

sealed class Result<out T> {
    class Error(val throwable: Throwable) : Result<Nothing>()
    class Success<out T>(val data: T) : Result<T>()

    companion object {
        fun <T> success(data: T) = Success(data);
        fun error(throwable: Throwable) = Error(throwable)
    }
}