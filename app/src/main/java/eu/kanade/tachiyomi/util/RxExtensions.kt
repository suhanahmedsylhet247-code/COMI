package eu.kanade.tachiyomi.util

import rx.Observable

suspend fun <T> Observable<T>.awaitSingle(): T {
    return toBlocking().first()
}
