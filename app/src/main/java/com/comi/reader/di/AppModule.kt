package com.comi.reader.di

import android.content.Context
import androidx.room.Room
import com.comi.reader.data.local.ComiDatabase
import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.parser.ComicParser
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
            "comi.db"
        ).build()
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
}
