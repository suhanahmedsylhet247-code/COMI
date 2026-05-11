package com.comi.reader.data.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.comi.reader.domain.model.ComicFormat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComicParser @Inject constructor(
    private val context: Context
) {

    fun detectFormat(file: File): ComicFormat {
        val ext = file.extension.lowercase()
        return when (ext) {
            "cbz", "zip" -> ComicFormat.CBZ
            "cbr", "rar" -> ComicFormat.CBR
            "pdf" -> ComicFormat.PDF
            else -> ComicFormat.UNKNOWN
        }
    }

    fun getPageCount(file: File): Int {
        return when (detectFormat(file)) {
            ComicFormat.CBZ -> getCbzPageCount(file)
            ComicFormat.PDF -> getPdfPageCount(file)
            ComicFormat.CBR -> getCbrPageCount(file)
            ComicFormat.UNKNOWN -> 0
        }
    }

    fun extractPage(file: File, pageIndex: Int): Bitmap? {
        return when (detectFormat(file)) {
            ComicFormat.CBZ -> extractCbzPage(file, pageIndex)
            ComicFormat.PDF -> extractPdfPage(file, pageIndex)
            ComicFormat.CBR -> extractCbrPage(file, pageIndex)
            ComicFormat.UNKNOWN -> null
        }
    }

    fun extractCoverThumbnail(file: File, maxWidth: Int = 300): Bitmap? {
        val page = extractPage(file, 0) ?: return null
        val ratio = maxWidth.toFloat() / page.width
        val scaledHeight = (page.height * ratio).toInt()
        return Bitmap.createScaledBitmap(page, maxWidth, scaledHeight, true)
    }

    fun extractMetadata(file: File): Map<String, String> {
        return when (detectFormat(file)) {
            ComicFormat.CBZ -> extractCbzMetadata(file)
            ComicFormat.PDF -> extractPdfMetadata(file)
            else -> emptyMap()
        }
    }

    // CBZ (ZIP) parsing
    private fun getCbzPageCount(file: File): Int {
        return getImageEntries(file).size
    }

    private fun extractCbzPage(file: File, pageIndex: Int): Bitmap? {
        val entries = getImageEntries(file)
        if (pageIndex < 0 || pageIndex >= entries.size) return null
        val entryName = entries[pageIndex]

        ZipFile(file).use { zip ->
            val entry = zip.getEntry(entryName) ?: return null
            zip.getInputStream(entry).use { stream ->
                return BitmapFactory.decodeStream(stream)
            }
        }
    }

    private fun getImageEntries(file: File): List<String> {
        val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
        ZipFile(file).use { zip ->
            return zip.entries()
                .asSequence()
                .filter { !it.isDirectory }
                .filter { entry ->
                    val ext = entry.name.substringAfterLast('.', "").lowercase()
                    ext in imageExtensions
                }
                .map { it.name }
                .sorted()
                .toList()
        }
    }

    private fun extractCbzMetadata(file: File): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        try {
            ZipFile(file).use { zip ->
                val comicInfo = zip.getEntry("ComicInfo.xml")
                if (comicInfo != null) {
                    zip.getInputStream(comicInfo).use { stream ->
                        val xml = stream.bufferedReader().readText()
                        extractXmlField(xml, "Title")?.let { metadata["title"] = it }
                        extractXmlField(xml, "Writer")?.let { metadata["author"] = it }
                        extractXmlField(xml, "Series")?.let { metadata["series"] = it }
                        extractXmlField(xml, "Volume")?.let { metadata["volume"] = it }
                        extractXmlField(xml, "Summary")?.let { metadata["description"] = it }
                        extractXmlField(xml, "Publisher")?.let { metadata["publisher"] = it }
                        extractXmlField(xml, "Year")?.let { metadata["year"] = it }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore metadata extraction errors
        }
        return metadata
    }

    private fun extractXmlField(xml: String, tag: String): String? {
        val regex = Regex("<$tag>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
        return regex.find(xml)?.groupValues?.get(1)?.trim()
    }

    // PDF parsing
    private fun getPdfPageCount(file: File): Int {
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)
        val count = renderer.pageCount
        renderer.close()
        fd.close()
        return count
    }

    private fun extractPdfPage(file: File, pageIndex: Int): Bitmap? {
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)
        if (pageIndex < 0 || pageIndex >= renderer.pageCount) {
            renderer.close()
            fd.close()
            return null
        }

        val page = renderer.openPage(pageIndex)
        val scale = 2.0f
        val width = (page.width * scale).toInt()
        val height = (page.height * scale).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        fd.close()
        return bitmap
    }

    private fun extractPdfMetadata(file: File): Map<String, String> {
        // PDF metadata extraction is limited on Android without external libs
        return emptyMap()
    }

    // CBR (RAR) parsing - basic support
    // Note: Full RAR support requires native library. This provides basic structure.
    private fun getCbrPageCount(file: File): Int {
        return try {
            getCbrImageEntries(file).size
        } catch (_: Exception) {
            0
        }
    }

    private fun extractCbrPage(file: File, pageIndex: Int): Bitmap? {
        // CBR requires native RAR extraction library
        // For now, we return null and show a message to convert to CBZ
        return null
    }

    private fun getCbrImageEntries(file: File): List<String> {
        // Placeholder - RAR parsing requires JNI or external process
        return emptyList()
    }

    fun saveBitmapToFile(bitmap: Bitmap, outputFile: File): Boolean {
        return try {
            outputFile.parentFile?.mkdirs()
            outputFile.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getPageAsBytes(file: File, pageIndex: Int): ByteArray? {
        val bitmap = extractPage(file, pageIndex) ?: return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }
}
