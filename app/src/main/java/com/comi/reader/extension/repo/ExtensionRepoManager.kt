package com.comi.reader.extension.repo

import com.comi.reader.extension.model.AvailableSource
import com.comi.reader.extension.model.Extension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ExtensionRepoIndex(
    val name: String,
    val pkg: String,
    val apk: String,
    val lang: String,
    val code: Int,
    val version: String,
    val nsfw: Int = 0,
    val sources: List<ExtensionRepoSource> = emptyList(),
)

@Serializable
data class ExtensionRepoSource(
    val name: String,
    val lang: String,
    val id: Long,
    val baseUrl: String,
)

data class ExtensionRepo(
    val name: String,
    val url: String,
    val baseUrl: String,
)

@Singleton
class ExtensionRepoManager @Inject constructor(
    private val client: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _repos = MutableStateFlow<List<ExtensionRepo>>(emptyList())
    val repos: StateFlow<List<ExtensionRepo>> = _repos.asStateFlow()

    private val _availableExtensions = MutableStateFlow<List<Extension.Available>>(emptyList())
    val availableExtensions: StateFlow<List<Extension.Available>> = _availableExtensions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun addRepo(url: String) {
        val normalizedUrl = normalizeRepoUrl(url)
        val baseUrl = normalizedUrl.substringBeforeLast("/")
        val repo = ExtensionRepo(
            name = extractRepoName(normalizedUrl),
            url = normalizedUrl,
            baseUrl = baseUrl,
        )
        if (_repos.value.none { it.url == repo.url }) {
            _repos.value = _repos.value + repo
        }
    }

    fun removeRepo(repoUrl: String) {
        _repos.value = _repos.value.filter { it.url != repoUrl }
        _availableExtensions.value = _availableExtensions.value.filter { it.repoUrl != repoUrl }
    }

    suspend fun fetchAllRepos() {
        _isLoading.value = true
        try {
            val allExtensions = mutableListOf<Extension.Available>()
            for (repo in _repos.value) {
                try {
                    val extensions = fetchRepoIndex(repo)
                    allExtensions.addAll(extensions)
                } catch (e: Exception) {
                    android.util.Log.e("ExtensionRepoManager", "Failed to fetch repo: ${repo.url}", e)
                }
            }
            _availableExtensions.value = allExtensions
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun fetchRepoIndex(repo: ExtensionRepo): List<Extension.Available> =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(repo.url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("Failed to fetch repo index: ${response.code}")
            }
            val body = response.body?.string() ?: throw Exception("Empty response body")
            val indexEntries = json.decodeFromString<List<ExtensionRepoIndex>>(body)

            indexEntries.map { entry ->
                Extension.Available(
                    name = entry.name.removePrefix("Tachiyomi: "),
                    pkgName = entry.pkg,
                    versionName = entry.version,
                    versionCode = entry.code.toLong(),
                    libVersion = 1.5,
                    lang = entry.lang,
                    isNsfw = entry.nsfw == 1,
                    sources = entry.sources.map { src ->
                        AvailableSource(
                            id = src.id,
                            lang = src.lang,
                            name = src.name,
                            baseUrl = src.baseUrl,
                        )
                    },
                    apkName = entry.apk,
                    iconUrl = "${repo.baseUrl}/icon/${entry.pkg}.png",
                    repoUrl = repo.url,
                )
            }
        }

    fun getApkUrl(extension: Extension.Available): String {
        val baseUrl = extension.repoUrl.substringBeforeLast("/")
        return "$baseUrl/apk/${extension.apkName}"
    }

    private fun normalizeRepoUrl(url: String): String {
        var normalized = url.trim()
        if (!normalized.endsWith("index.min.json")) {
            normalized = normalized.trimEnd('/')
            normalized = "$normalized/index.min.json"
        }
        return normalized
    }

    private fun extractRepoName(url: String): String {
        val parts = url.split("/")
        val githubIdx = parts.indexOfFirst { it == "github.com" || it == "githubusercontent.com" }
        return if (githubIdx >= 0 && githubIdx + 1 < parts.size) {
            parts[githubIdx + 1]
        } else {
            url.substringAfterLast("//").substringBefore("/")
        }
    }
}
