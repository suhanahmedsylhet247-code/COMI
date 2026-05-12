package com.comi.reader.extension.model

import android.graphics.drawable.Drawable
import eu.kanade.tachiyomi.source.Source

sealed class Extension {
    abstract val name: String
    abstract val pkgName: String
    abstract val versionName: String
    abstract val versionCode: Long
    abstract val lang: String?
    abstract val isNsfw: Boolean

    data class Installed(
        override val name: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val lang: String,
        override val isNsfw: Boolean,
        val pkgFactory: String?,
        val sources: List<Source>,
        val icon: Drawable?,
        val hasUpdate: Boolean = false,
        val isObsolete: Boolean = false,
        val isShared: Boolean,
        val repoUrl: String? = null,
    ) : Extension()

    data class Available(
        override val name: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        val libVersion: Double,
        override val lang: String,
        override val isNsfw: Boolean,
        val sources: List<AvailableSource>,
        val apkName: String,
        val iconUrl: String,
        val repoUrl: String,
    ) : Extension()

    data class Untrusted(
        override val name: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val lang: String,
        override val isNsfw: Boolean,
        val signatureHash: String,
    ) : Extension()
}

data class AvailableSource(
    val id: Long,
    val lang: String,
    val name: String,
    val baseUrl: String,
)

sealed interface LoadResult {
    data class Success(val extension: Extension.Installed) : LoadResult
    data class Untrusted(val extension: Extension.Untrusted) : LoadResult
    data object Error : LoadResult
}

enum class InstallStep {
    Idle,
    Pending,
    Downloading,
    Installing,
    Installed,
    Error,
}
