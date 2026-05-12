package com.comi.reader.domain.dedup

import com.comi.reader.data.local.entity.ChapterEntity
import kotlin.math.abs

data class MergedChapter(
    val canonicalNumber: Float,
    val displayName: String,
    val sourceChapters: List<SourceChapter>,
    val isFiller: Boolean,
    val dateUpload: Long,
    val isRead: Boolean,
) {
    val bestChapter: SourceChapter
        get() = sourceChapters.maxByOrNull { it.priority } ?: sourceChapters.first()
}

data class SourceChapter(
    val chapter: ChapterEntity,
    val sourceId: String,
    val sourceName: String,
    val priority: Int,
)

object ChapterDeduplicator {

    private const val FLOAT_TOLERANCE = 0.01f

    fun mergeChapterLists(
        chaptersBySource: Map<String, List<ChapterEntity>>,
        sourceNames: Map<String, String>,
        sourcePriorities: Map<String, Int> = emptyMap(),
    ): List<MergedChapter> {
        val allSourceChapters = mutableListOf<SourceChapter>()

        chaptersBySource.forEach { (sourceId, chapters) ->
            chapters.forEach { chapter ->
                allSourceChapters.add(
                    SourceChapter(
                        chapter = chapter,
                        sourceId = sourceId,
                        sourceName = sourceNames[sourceId] ?: sourceId,
                        priority = sourcePriorities[sourceId] ?: 0,
                    )
                )
            }
        }

        // Group by canonical chapter number
        val groups = groupByCanonicalNumber(allSourceChapters)

        // For chapters without numbers (named chapters), match by chronological order
        val numberedGroups = groups.filter { it.key >= 0f }
        val unmatched = groups[UNNAMED_GROUP_KEY] ?: emptyList()

        val mergedFromNumbers = numberedGroups.map { (number, sourceChapters) ->
            val isFiller = isFillerChapter(number)
            MergedChapter(
                canonicalNumber = number,
                displayName = formatChapterNumber(number, sourceChapters),
                sourceChapters = sourceChapters,
                isFiller = isFiller,
                dateUpload = sourceChapters.maxOf { it.chapter.dateUpload },
                isRead = sourceChapters.any { it.chapter.isRead },
            )
        }

        // Try to match unnamed chapters to existing numbered ones by date
        val mergedFromNamed = matchNamedChaptersByDate(unmatched, mergedFromNumbers)

        val allMerged = (mergedFromNumbers + mergedFromNamed)
            .sortedBy { it.canonicalNumber }

        return allMerged
    }

    private fun groupByCanonicalNumber(
        chapters: List<SourceChapter>,
    ): Map<Float, List<SourceChapter>> {
        val groups = mutableMapOf<Float, MutableList<SourceChapter>>()

        for (sc in chapters) {
            val number = sc.chapter.number
            if (number < 0f) {
                // Unnamed chapter (number not set)
                groups.getOrPut(UNNAMED_GROUP_KEY) { mutableListOf() }.add(sc)
                continue
            }

            // Find existing group with matching number (within tolerance)
            val matchingKey = groups.keys.find { abs(it - number) < FLOAT_TOLERANCE }
            if (matchingKey != null) {
                groups[matchingKey]!!.add(sc)
            } else {
                groups[number] = mutableListOf(sc)
            }
        }

        return groups
    }

    fun isFillerChapter(number: Float): Boolean {
        // Chapters with decimal parts like 10.5, 10.1, etc. are potential fillers
        val decimal = number - number.toInt()
        return decimal > FLOAT_TOLERANCE && abs(decimal - 0.5f) < FLOAT_TOLERANCE
    }

    private fun matchNamedChaptersByDate(
        unnamed: List<SourceChapter>,
        existing: List<MergedChapter>,
    ): List<MergedChapter> {
        if (unnamed.isEmpty()) return emptyList()

        val result = mutableListOf<MergedChapter>()

        // Sort unnamed by upload date
        val sortedUnnamed = unnamed.sortedBy { it.chapter.dateUpload }
        // Sort existing by canonical number
        val sortedExisting = existing.sortedBy { it.canonicalNumber }

        val matchedIndices = mutableSetOf<Int>()

        for (uc in sortedUnnamed) {
            if (uc.chapter.dateUpload <= 0) {
                // No date info — treat as unique chapter
                result.add(
                    MergedChapter(
                        canonicalNumber = uc.chapter.number,
                        displayName = uc.chapter.title,
                        sourceChapters = listOf(uc),
                        isFiller = false,
                        dateUpload = uc.chapter.dateUpload,
                        isRead = uc.chapter.isRead,
                    )
                )
                continue
            }

            // Find the closest existing chapter by date (within 24 hours)
            var bestMatch = -1
            var bestDiff = Long.MAX_VALUE
            for ((idx, existing) in sortedExisting.withIndex()) {
                if (idx in matchedIndices) continue
                val diff = abs(existing.dateUpload - uc.chapter.dateUpload)
                if (diff < bestDiff && diff < 24 * 60 * 60 * 1000) { // 24 hour window
                    bestDiff = diff
                    bestMatch = idx
                }
            }

            if (bestMatch >= 0) {
                // This named chapter matches an existing numbered chapter
                matchedIndices.add(bestMatch)
                // The existing MergedChapter already has this covered
            } else {
                // Truly unique chapter
                result.add(
                    MergedChapter(
                        canonicalNumber = uc.chapter.number,
                        displayName = uc.chapter.title,
                        sourceChapters = listOf(uc),
                        isFiller = false,
                        dateUpload = uc.chapter.dateUpload,
                        isRead = uc.chapter.isRead,
                    )
                )
            }
        }

        return result
    }

    private fun formatChapterNumber(number: Float, sources: List<SourceChapter>): String {
        val intPart = number.toInt()
        val decimal = number - intPart
        return if (abs(decimal) < FLOAT_TOLERANCE) {
            "Chapter $intPart"
        } else {
            "Chapter $number"
        }
    }

    fun deduplicateNewChapters(
        newChaptersBySource: Map<String, List<ChapterEntity>>,
    ): List<ChapterEntity> {
        val seen = mutableSetOf<Float>()
        val result = mutableListOf<ChapterEntity>()

        // Process sources by priority (first source wins)
        for ((_, chapters) in newChaptersBySource) {
            for (chapter in chapters) {
                val number = chapter.number
                if (number >= 0f) {
                    val matchingKey = seen.find { abs(it - number) < FLOAT_TOLERANCE }
                    if (matchingKey == null) {
                        seen.add(number)
                        result.add(chapter)
                    }
                } else {
                    // Named-only chapter — always include
                    result.add(chapter)
                }
            }
        }

        return result
    }

    private const val UNNAMED_GROUP_KEY = -999f
}
