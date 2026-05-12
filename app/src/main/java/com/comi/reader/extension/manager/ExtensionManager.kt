package com.comi.reader.extension.manager

import android.content.Context
import com.comi.reader.extension.installer.ExtensionInstaller
import com.comi.reader.extension.loader.ExtensionLoader
import com.comi.reader.extension.model.Extension
import com.comi.reader.extension.model.LoadResult
import com.comi.reader.extension.repo.ExtensionRepoManager
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionManager @Inject constructor(
    private val context: Context,
    val repoManager: ExtensionRepoManager,
    val installer: ExtensionInstaller,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _installedExtensions = MutableStateFlow<List<Extension.Installed>>(emptyList())
    val installedExtensions: StateFlow<List<Extension.Installed>> = _installedExtensions.asStateFlow()

    private val _sources = MutableStateFlow<List<CatalogueSource>>(emptyList())
    val sources: StateFlow<List<CatalogueSource>> = _sources.asStateFlow()

    private val _allSources = MutableStateFlow<Map<Long, Source>>(emptyMap())
    val allSources: StateFlow<Map<Long, Source>> = _allSources.asStateFlow()

    fun init() {
        loadInstalledExtensions()
    }

    fun loadInstalledExtensions() {
        val results = ExtensionLoader.loadExtensions(context)
        val installed = results.filterIsInstance<LoadResult.Success>().map { it.extension }
        _installedExtensions.value = installed

        val catalogueSources = mutableListOf<CatalogueSource>()
        val allSourceMap = mutableMapOf<Long, Source>()

        installed.forEach { ext ->
            ext.sources.forEach { source ->
                allSourceMap[source.id] = source
                if (source is CatalogueSource) {
                    catalogueSources.add(source)
                }
            }
        }

        _sources.value = catalogueSources
        _allSources.value = allSourceMap
    }

    suspend fun installExtension(extension: Extension.Available): Boolean {
        val success = installer.installExtension(extension)
        if (success) {
            loadInstalledExtensions()
        }
        return success
    }

    suspend fun uninstallExtension(extension: Extension.Installed): Boolean {
        val success = installer.uninstallExtension(extension.pkgName)
        if (success) {
            loadInstalledExtensions()
        }
        return success
    }

    suspend fun updateExtension(extension: Extension.Available): Boolean {
        return installExtension(extension)
    }

    fun getSource(sourceId: Long): Source? = _allSources.value[sourceId]

    fun getCatalogueSource(sourceId: Long): CatalogueSource? =
        _allSources.value[sourceId] as? CatalogueSource

    fun getSourcesByLang(lang: String): List<CatalogueSource> =
        _sources.value.filter { it.lang == lang || it.lang == "all" }

    fun isExtensionInstalled(pkgName: String): Boolean =
        _installedExtensions.value.any { it.pkgName == pkgName }

    fun hasUpdate(extension: Extension.Available): Boolean {
        val installed = _installedExtensions.value.find { it.pkgName == extension.pkgName }
            ?: return false
        return extension.versionCode > installed.versionCode
    }
}
