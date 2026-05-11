package com.comi.reader.extension.model

data class MangaDetails(
    val url: String,
    val title: String,
    val coverUrl: String? = null,
    val author: String? = null,
    val artist: String? = null,
    val description: String? = null,
    val genres: List<String> = emptyList(),
    val status: MangaStatus = MangaStatus.UNKNOWN
)

enum class MangaStatus {
    ONGOING, COMPLETED, HIATUS, CANCELLED, UNKNOWN
}

data class MangaChapter(
    val url: String,
    val title: String,
    val number: Float = 0f,
    val dateUpload: Long = 0,
    val scanlator: String? = null
)

data class MangaPage(
    val index: Int,
    val imageUrl: String,
    val headers: Map<String, String> = emptyMap()
)

data class Extension(
    val id: String,
    val name: String,
    val versionName: String,
    val versionCode: Int,
    val language: String,
    val isInstalled: Boolean = false,
    val hasUpdate: Boolean = false,
    val iconUrl: String? = null,
    val sources: List<SourceInfo> = emptyList()
)

data class SourceInfo(
    val id: String,
    val name: String,
    val language: String,
    val baseUrl: String
)
