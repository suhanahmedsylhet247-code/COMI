package com.comi.reader.extension.loader

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.comi.reader.extension.model.Extension
import com.comi.reader.extension.model.LoadResult
import dalvik.system.PathClassLoader
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory
import java.io.File

object ExtensionLoader {

    private const val EXTENSION_FEATURE = "tachiyomi.extension"
    private const val METADATA_SOURCE_CLASS = "tachiyomi.extension.class"
    private const val METADATA_SOURCE_FACTORY = "tachiyomi.extension.factory"
    private const val METADATA_NSFW = "tachiyomi.extension.nsfw"
    private const val PRIVATE_EXTENSION_EXTENSION = "ext"

    @Suppress("DEPRECATION")
    private val PACKAGE_FLAGS = PackageManager.GET_CONFIGURATIONS or
        PackageManager.GET_META_DATA or
        PackageManager.GET_SIGNATURES or
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else 0)

    fun getPrivateExtensionDir(context: Context) = File(context.filesDir, "exts")

    fun installPrivateExtensionFile(context: Context, file: File): Boolean {
        val pkgInfo = context.packageManager.getPackageArchiveInfo(file.absolutePath, PACKAGE_FLAGS)
            ?: return false
        if (!isPackageAnExtension(pkgInfo)) return false

        val targetDir = getPrivateExtensionDir(context)
        targetDir.mkdirs()
        val target = File(targetDir, "${pkgInfo.packageName}.$PRIVATE_EXTENSION_EXTENSION")
        return try {
            file.copyTo(target, overwrite = true)
            target.setReadOnly()
            true
        } catch (e: Exception) {
            android.util.Log.e("ExtensionLoader", "Failed to install extension", e)
            false
        }
    }

    fun loadExtensions(context: Context): List<LoadResult> {
        val results = mutableListOf<LoadResult>()

        // Load private extensions from our directory
        val privateDir = getPrivateExtensionDir(context)
        if (privateDir.exists()) {
            privateDir.listFiles()?.filter { it.extension == PRIVATE_EXTENSION_EXTENSION }?.forEach { file ->
                val result = loadPrivateExtension(context, file)
                results.add(result)
            }
        }

        // Also load extensions installed as regular apps (shared extensions)
        val pm = context.packageManager
        @Suppress("DEPRECATION")
        val installedPkgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong()))
        } else {
            pm.getInstalledPackages(PACKAGE_FLAGS)
        }

        installedPkgs.filter { isPackageAnExtension(it) }.forEach { pkgInfo ->
            val result = loadSharedExtension(context, pkgInfo)
            results.add(result)
        }

        return results
    }

    private fun loadPrivateExtension(context: Context, file: File): LoadResult {
        return try {
            val pkgInfo = context.packageManager.getPackageArchiveInfo(
                file.absolutePath, PACKAGE_FLAGS
            ) ?: return LoadResult.Error

            val appInfo = pkgInfo.applicationInfo ?: return LoadResult.Error
            appInfo.sourceDir = file.absolutePath
            appInfo.publicSourceDir = file.absolutePath

            loadExtensionFromPkgInfo(context, pkgInfo, isShared = false)
        } catch (e: Exception) {
            android.util.Log.e("ExtensionLoader", "Error loading private extension: ${file.name}", e)
            LoadResult.Error
        }
    }

    private fun loadSharedExtension(context: Context, pkgInfo: PackageInfo): LoadResult {
        return try {
            loadExtensionFromPkgInfo(context, pkgInfo, isShared = true)
        } catch (e: Exception) {
            android.util.Log.e("ExtensionLoader", "Error loading shared extension: ${pkgInfo.packageName}", e)
            LoadResult.Error
        }
    }

    @Suppress("DEPRECATION")
    private fun loadExtensionFromPkgInfo(
        context: Context,
        pkgInfo: PackageInfo,
        isShared: Boolean,
    ): LoadResult {
        val appInfo = pkgInfo.applicationInfo ?: return LoadResult.Error
        val metadata = appInfo.metaData ?: return LoadResult.Error

        val extName = metadata.getString(METADATA_SOURCE_CLASS)
        val extFactory = metadata.getString(METADATA_SOURCE_FACTORY)
        val isNsfw = metadata.getInt(METADATA_NSFW, 0) == 1

        val classLoader = PathClassLoader(
            appInfo.sourceDir,
            null,
            context.classLoader,
        )

        val sources = mutableListOf<Source>()

        try {
            if (extFactory != null) {
                val factoryClass = Class.forName(extFactory, false, classLoader)
                val factory = factoryClass.getDeclaredConstructor().newInstance() as SourceFactory
                sources.addAll(factory.createSources())
            } else if (extName != null) {
                val classNames = extName.split(";")
                for (className in classNames) {
                    val trimmed = className.trim()
                    if (trimmed.isEmpty()) continue
                    val sourceClass = Class.forName(trimmed, false, classLoader)
                    val source = sourceClass.getDeclaredConstructor().newInstance() as Source
                    sources.add(source)
                }
            } else {
                return LoadResult.Error
            }
        } catch (e: Exception) {
            android.util.Log.e("ExtensionLoader", "Error instantiating source classes for ${pkgInfo.packageName}", e)
            return LoadResult.Error
        }

        val lang = sources.filterIsInstance<CatalogueSource>().firstOrNull()?.lang ?: "all"

        val extension = Extension.Installed(
            name = pkgInfo.applicationInfo?.loadLabel(context.packageManager)?.toString()
                ?: pkgInfo.packageName,
            pkgName = pkgInfo.packageName,
            versionName = pkgInfo.versionName ?: "",
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.versionCode.toLong()
            },
            lang = lang,
            isNsfw = isNsfw,
            pkgFactory = extFactory,
            sources = sources,
            icon = try { appInfo.loadIcon(context.packageManager) } catch (_: Exception) { null },
            isShared = isShared,
        )

        return LoadResult.Success(extension)
    }

    private fun isPackageAnExtension(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.reqFeatures?.any { it.name == EXTENSION_FEATURE } == true
    }

    fun uninstallPrivateExtension(context: Context, pkgName: String): Boolean {
        val dir = getPrivateExtensionDir(context)
        val file = File(dir, "$pkgName.$PRIVATE_EXTENSION_EXTENSION")
        return file.delete()
    }
}
