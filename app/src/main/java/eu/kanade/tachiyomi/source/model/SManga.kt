@file:Suppress("PropertyName")

package eu.kanade.tachiyomi.source.model

import java.io.Serializable

interface SManga : Serializable {
    var url: String
    var title: String
    var artist: String?
    var author: String?
    var description: String?
    var genre: String?
    var status: Int
    var thumbnail_url: String?
    var update_strategy: UpdateStrategy
    var initialized: Boolean

    fun copyFrom(other: SManga) {
        other.url.let { url = it }
        other.title.let { title = it }
        other.artist?.let { artist = it }
        other.author?.let { author = it }
        other.description?.let { description = it }
        other.genre?.let { genre = it }
        if (other.status != UNKNOWN) status = other.status
        other.thumbnail_url?.let { thumbnail_url = it }
    }

    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        const val PUBLISHING_FINISHED = 4
        const val CANCELLED = 5
        const val ON_HIATUS = 6

        fun create(): SManga = SMangaImpl()
    }
}

class SMangaImpl : SManga {
    override lateinit var url: String
    override lateinit var title: String
    override var artist: String? = null
    override var author: String? = null
    override var description: String? = null
    override var genre: String? = null
    override var status: Int = SManga.UNKNOWN
    override var thumbnail_url: String? = null
    override var update_strategy: UpdateStrategy = UpdateStrategy.ALWAYS_UPDATE
    override var initialized: Boolean = false
}

enum class UpdateStrategy {
    ALWAYS_UPDATE,
    ONLY_FETCH_ONCE,
}
