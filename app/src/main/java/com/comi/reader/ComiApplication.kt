package com.comi.reader

import android.app.Application
import com.comi.reader.data.notification.UpdateCheckWorker
import com.comi.reader.extension.manager.ExtensionManager
import dagger.hilt.android.HiltAndroidApp
import eu.kanade.tachiyomi.network.NetworkHelper
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import javax.inject.Inject

@HiltAndroidApp
class ComiApplication : Application() {

    @Inject lateinit var networkHelper: NetworkHelper
    @Inject lateinit var extensionManager: ExtensionManager

    override fun onCreate() {
        super.onCreate()

        // Register NetworkHelper with Injekt so extensions can access it via `by injectLazy()`
        Injekt.addSingleton(networkHelper)

        // Initialize extension manager (loads installed extensions)
        extensionManager.init()

        // Schedule periodic update checks
        UpdateCheckWorker.schedule(this)
    }
}
