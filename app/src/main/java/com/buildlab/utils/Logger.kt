@file:Suppress(IMPORTANT)

package com.buildlab.utils

import java.util.Date

interface LogPrinter {
    fun i(tag: String, msg: String)

    fun d(tag: String, msg: String)

    fun w(tag: String, msg: String)

    fun w(tag: String, msg: String, throwable: Throwable)

    fun e(tag: String, msg: String)

    fun e(tag: String, msg: String, throwable: Throwable)
}

object Logger {
    internal var printer: LogPrinter = getDefaultPrinter()

    fun installPrinter(printer: LogPrinter) {
        Logger.printer = printer
    }

    fun i(tag: String, msg: String) {
        printer.i(tag, msg)
    }

    fun d(tag: String, msg: String) {
        printer.d(tag, msg)
    }

    fun w(tag: String, msg: String) {
        printer.w(tag, msg)
    }

    fun w(tag: String, msg: String, throwable: Throwable) {
        printer.w(tag, msg, throwable)
    }

    fun e(tag: String, msg: String) {
        printer.e(tag, msg)
    }

    fun stacktrace(tag: String, msg: String, throwable: Throwable) {
        printer.e(tag, msg, throwable)
    }

    private fun getDefaultPrinter() = object : LogPrinter {

        private val time: Date get() = Date(System.currentTimeMillis())

        override fun i(tag: String, msg: String) {
            println("$time ~ [INFO | $tag]: $msg\n")
        }

        override fun d(tag: String, msg: String) {
            println("$time ~ [DEBUG | $tag]: $msg\n")
        }

        override fun w(tag: String, msg: String) {
            println("$time ~ [WARN | $tag]: $msg\n")
        }

        override fun w(tag: String, msg: String, throwable: Throwable) {
            println("$time ~ [WARN | $tag]: $msg\n $throwable")
        }

        override fun e(tag: String, msg: String) {
            println("$time ~ [ERROR | $tag]: $msg\n")
        }

        override fun e(tag: String, msg: String, throwable: Throwable) {
            println("$time ~ [ERROR | $tag]: $msg\n $throwable")
        }
    }
}

fun installLogPrinter(printer: LogPrinter) {
    Logger.printer = printer
}