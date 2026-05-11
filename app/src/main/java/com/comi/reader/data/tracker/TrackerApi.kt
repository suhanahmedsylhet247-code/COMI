package com.comi.reader.data.tracker

import com.comi.reader.domain.model.TrackingStatus

interface TrackerApi {
    val id: String
    val name: String
    val logoUrl: String

    suspend fun authenticate(token: String): Boolean
    suspend fun search(query: String): List<TrackerSearchResult>
    suspend fun getStatus(remoteId: String): TrackerMangaStatus?
    suspend fun updateStatus(remoteId: String, status: TrackingStatus)
    suspend fun updateChapterProgress(remoteId: String, chapterNumber: Float)
    suspend fun updateScore(remoteId: String, score: Float)
}

data class TrackerSearchResult(
    val remoteId: String,
    val title: String,
    val coverUrl: String? = null,
    val synopsis: String? = null,
    val totalChapters: Int = 0,
    val status: String? = null,
    val score: Float = 0f
)

data class TrackerMangaStatus(
    val remoteId: String,
    val status: TrackingStatus,
    val chaptersRead: Float,
    val totalChapters: Int,
    val score: Float,
    val startDate: String? = null,
    val finishDate: String? = null
)

class MyAnimeListTracker : TrackerApi {
    override val id = "myanimelist"
    override val name = "MyAnimeList"
    override val logoUrl = "https://myanimelist.net/img/common/pwa/launcher-icon-0-75x.png"

    override suspend fun authenticate(token: String): Boolean {
        // MAL OAuth2 authentication
        // In production: POST to https://myanimelist.net/v1/oauth2/token
        return token.isNotBlank()
    }

    override suspend fun search(query: String): List<TrackerSearchResult> {
        // In production: GET https://api.myanimelist.net/v2/manga?q={query}
        return emptyList()
    }

    override suspend fun getStatus(remoteId: String): TrackerMangaStatus? {
        // In production: GET manga status from MAL API
        return null
    }

    override suspend fun updateStatus(remoteId: String, status: TrackingStatus) {
        // In production: PUT to MAL API
    }

    override suspend fun updateChapterProgress(remoteId: String, chapterNumber: Float) {
        // In production: PUT to MAL API with num_chapters_read
    }

    override suspend fun updateScore(remoteId: String, score: Float) {
        // In production: PUT to MAL API with score
    }
}

class AniListTracker : TrackerApi {
    override val id = "anilist"
    override val name = "AniList"
    override val logoUrl = "https://anilist.co/img/icons/android-chrome-512x512.png"

    override suspend fun authenticate(token: String): Boolean {
        // AniList OAuth2 authentication
        // In production: POST to https://anilist.co/api/v2/oauth/token
        return token.isNotBlank()
    }

    override suspend fun search(query: String): List<TrackerSearchResult> {
        // In production: GraphQL query to AniList API
        return emptyList()
    }

    override suspend fun getStatus(remoteId: String): TrackerMangaStatus? = null

    override suspend fun updateStatus(remoteId: String, status: TrackingStatus) {}
    override suspend fun updateChapterProgress(remoteId: String, chapterNumber: Float) {}
    override suspend fun updateScore(remoteId: String, score: Float) {}
}

class KitsuTracker : TrackerApi {
    override val id = "kitsu"
    override val name = "Kitsu"
    override val logoUrl = "https://kitsu.app/kitsu-256-ed442f7567271af715884ca3080b7a22.png"

    override suspend fun authenticate(token: String): Boolean {
        // Kitsu OAuth2 authentication
        // In production: POST to https://kitsu.app/api/oauth/token
        return token.isNotBlank()
    }

    override suspend fun search(query: String): List<TrackerSearchResult> {
        // In production: GET https://kitsu.app/api/edge/manga?filter[text]={query}
        return emptyList()
    }

    override suspend fun getStatus(remoteId: String): TrackerMangaStatus? = null
    override suspend fun updateStatus(remoteId: String, status: TrackingStatus) {}
    override suspend fun updateChapterProgress(remoteId: String, chapterNumber: Float) {}
    override suspend fun updateScore(remoteId: String, score: Float) {}
}
