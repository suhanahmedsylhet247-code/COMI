package tachiyomi.core.common.util.system

import android.util.Log

object LogPriority {
    const val VERBOSE = Log.VERBOSE
    const val DEBUG = Log.DEBUG
    const val INFO = Log.INFO
    const val WARN = Log.WARN
    const val ERROR = Log.ERROR
}

inline fun logcat(
    priority: Int = LogPriority.DEBUG,
    tag: String = "COMI",
    throwable: Throwable? = null,
    message: () -> String,
) {
    val msg = message()
    when (priority) {
        LogPriority.VERBOSE -> Log.v(tag, msg, throwable)
        LogPriority.DEBUG -> Log.d(tag, msg, throwable)
        LogPriority.INFO -> Log.i(tag, msg, throwable)
        LogPriority.WARN -> Log.w(tag, msg, throwable)
        LogPriority.ERROR -> Log.e(tag, msg, throwable)
    }
}
