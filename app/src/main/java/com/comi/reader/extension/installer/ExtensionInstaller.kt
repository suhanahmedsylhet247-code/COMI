package com.comi.reader.extension.installer

import android.content.Context
import com.comi.reader.extension.loader.ExtensionLoader
import com.comi.reader.extension.model.Extension
import com.comi.reader.extension.model.InstallStep
import com.comi.reader.extension.repo.ExtensionRepoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionInstaller @Inject constructor(
    private val context: Context,
    private val client: OkHttpClient,
    private val repoManager: ExtensionRepoManager,
) {
    private val _installSteps = MutableStateFlow<Map<String, InstallStep>>(emptyMap())
    val installSteps: StateFlow<Map<String, InstallStep>> = _installSteps.asStateFlow()

    suspend fun installExtension(extension: Extension.Available): Boolean {
        updateStep(extension.pkgName, InstallStep.Downloading)
        return try {
            val apkUrl = repoManager.getApkUrl(extension)
            val tempFile = downloadApk(apkUrl, extension.pkgName)
                ?: run {
                    updateStep(extension.pkgName, InstallStep.Error)
                    return false
                }

            updateStep(extension.pkgName, InstallStep.Installing)

            val success = ExtensionLoader.installPrivateExtensionFile(context, tempFile)
            tempFile.delete()

            if (success) {
                updateStep(extension.pkgName, InstallStep.Installed)
            } else {
                updateStep(extension.pkgName, InstallStep.Error)
            }
            success
        } catch (e: Exception) {
            android.util.Log.e("ExtensionInstaller", "Install failed for ${extension.pkgName}", e)
            updateStep(extension.pkgName, InstallStep.Error)
            false
        }
    }

    suspend fun uninstallExtension(pkgName: String): Boolean {
        return ExtensionLoader.uninstallPrivateExtension(context, pkgName)
    }

    private suspend fun downloadApk(url: String, pkgName: String): File? =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext null

                val downloadDir = File(context.cacheDir, "extension_downloads")
                downloadDir.mkdirs()
                val file = File(downloadDir, "$pkgName.apk")
                file.outputStream().use { output ->
                    response.body?.byteStream()?.use { input ->
                        input.copyTo(output)
                    }
                }
                file
            } catch (e: Exception) {
                android.util.Log.e("ExtensionInstaller", "Download failed: $url", e)
                null
            }
        }

    private fun updateStep(pkgName: String, step: InstallStep) {
        _installSteps.value = _installSteps.value + (pkgName to step)
    }

    fun clearInstallStep(pkgName: String) {
        _installSteps.value = _installSteps.value - pkgName
    }
}
