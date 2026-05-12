package eu.kanade.tachiyomi.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import rx.Producer
import rx.Subscription
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun Call.asObservableSuccess(): Observable<Response> {
    return Observable.unsafeCreate { subscriber ->
        val call = this
        val requestAtomic = AtomicBoolean(false)

        subscriber.add(object : Subscription {
            override fun isUnsubscribed(): Boolean = requestAtomic.get()
            override fun unsubscribe() {
                requestAtomic.set(true)
                call.cancel()
            }
        })

        try {
            val response = call.execute()
            if (!subscriber.isUnsubscribed) {
                if (response.isSuccessful) {
                    subscriber.onNext(response)
                    subscriber.onCompleted()
                } else {
                    subscriber.onError(HttpException(response.code))
                }
            }
        } catch (e: Exception) {
            if (!subscriber.isUnsubscribed) {
                subscriber.onError(e)
            }
        }
    }
}

suspend fun Call.awaitSuccess(): Response {
    return suspendCancellableCoroutine { cont ->
        cont.invokeOnCancellation { cancel() }
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    cont.resume(response)
                } else {
                    cont.resumeWithException(HttpException(response.code))
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWithException(e)
            }
        })
    }
}

fun OkHttpClient.newCachelessCallWithProgress(request: Request, listener: ProgressListener): Call {
    val progressClient = this.newBuilder()
        .cache(null)
        .addNetworkInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body!!, listener))
                .build()
        }
        .build()
    return progressClient.newCall(request)
}

fun interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}

class HttpException(val code: Int) : Exception("HTTP error $code")
