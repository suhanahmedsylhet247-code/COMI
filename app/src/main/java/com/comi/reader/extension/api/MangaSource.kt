package com.comi.reader.extension.api

/**
 * Legacy source interface (deprecated — use Tachiyomi-compatible Source/CatalogueSource instead).
 * Extensions from Keiyoushi/Yuzino repos implement eu.kanade.tachiyomi.source.CatalogueSource.
 * This file is kept only for backward compatibility.
 */
@Deprecated("Use eu.kanade.tachiyomi.source.CatalogueSource instead")
interface ComiSource {
    val id: String
    val name: String
    val language: String
    val baseUrl: String
}
