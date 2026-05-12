package com.comi.reader.di

import android.content.Context
import androidx.room.Room
import com.comi.reader.data.local.ComiDatabase
import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.parser.ComicParser
import com.comi.reader.data.preferences.AppPreferences
import com.comi.reader.data.tracker.AniListTracker
import com.comi.reader.data.tracker.KitsuTracker
import com.comi.reader.data.tracker.MyAnimeListTracker
import com.comi.reader.data.tracker.TrackerApi
import com.comi.reader.data.repository.SourceDownloader
import com.comi.reader.extension.installer.ExtensionInstaller
import com.comi.reader.extension.manager.ExtensionManager
import com.comi.reader.extension.repo.ExtensionRepoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eu.kanade.tachiyomi.network.NetworkHelper
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ComiDatabase {
        return Room.databaseBuilder(
            context,
            ComiDatabase::class.java,
            "comi_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideComicDao(database: ComiDatabase): ComicDao {
        return database.comicDao()
    }

    @Provides
    @Singleton
    fun provideComicParser(@ApplicationContext context: Context): ComicParser {
        return ComicParser(context)
    }

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }

    @Provides
    @Singleton
    fun provideTrackers(): List<TrackerApi> {
        return listOf(
            MyAnimeListTracker(),
            AniListTracker(),
            KitsuTracker()
        )
    }

    @Provides
    @Singleton
    fun provideNetworkHelper(@ApplicationContext context: Context): NetworkHelper {
        return NetworkHelper(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(networkHelper: NetworkHelper): OkHttpClient {
        return networkHelper.client
    }

    @Provides
    @Singleton
    fun provideExtensionRepoManager(client: OkHttpClient): ExtensionRepoManager {
        return ExtensionRepoManager(client)
    }

    @Provides
    @Singleton
    fun provideExtensionInstaller(
        @ApplicationContext context: Context,
        client: OkHttpClient,
        repoManager: ExtensionRepoManager,
    ): ExtensionInstaller {
        return ExtensionInstaller(context, client, repoManager)
    }

    @Provides
    @Singleton
    fun provideExtensionManager(
        @ApplicationContext context: Context,
        repoManager: ExtensionRepoManager,
        installer: ExtensionInstaller,
    ): ExtensionManager {
        return ExtensionManager(context, repoManager, installer)
    }

    @Provides
    @Singleton
    fun provideSourceDownloader(
        @ApplicationContext context: Context,
        client: OkHttpClient,
        extensionManager: ExtensionManager,
        comicDao: ComicDao,
    ): SourceDownloader {
        return SourceDownloader(context, client, extensionManager, comicDao)
    }
}
