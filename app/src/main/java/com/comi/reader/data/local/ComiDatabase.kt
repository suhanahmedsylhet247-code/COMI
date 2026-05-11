package com.comi.reader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.comi.reader.data.local.converter.InstantConverter
import com.comi.reader.data.local.dao.ComicDao
import com.comi.reader.data.local.entity.BookmarkEntity
import com.comi.reader.data.local.entity.ChapterEntity
import com.comi.reader.data.local.entity.ComicEntity
import com.comi.reader.data.local.entity.DownloadTaskEntity
import com.comi.reader.data.local.entity.ReadingProgressEntity

@Database(
    entities = [
        ComicEntity::class,
        ChapterEntity::class,
        ReadingProgressEntity::class,
        BookmarkEntity::class,
        DownloadTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(InstantConverter::class)
abstract class ComiDatabase : RoomDatabase() {
    abstract fun comicDao(): ComicDao
}
