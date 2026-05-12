package eu.kanade.tachiyomi.source.model

import android.net.Uri
import java.io.Serializable

open class Page(
    val index: Int,
    var url: String = "",
    var imageUrl: String? = null,
    @Transient var uri: Uri? = null,
    @Transient var status: State = State.QUEUE,
    @Transient var progress: Int = 0,
) : Serializable {

    enum class State {
        QUEUE,
        LOAD_PAGE,
        DOWNLOAD_IMAGE,
        READY,
        ERROR,
    }

    override fun hashCode(): Int = index.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Page) return false
        return index == other.index
    }
}
