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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
}
