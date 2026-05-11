package com.comi.reader

import com.comi.reader.ui.search.SearchUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchUiStateTest {

    @Test
    fun `SearchUiState defaults`() {
        val state = SearchUiState()
        assertEquals("", state.query)
        assertTrue(state.results.isEmpty())
        assertTrue(state.searchHistory.isEmpty())
        assertTrue(state.genres.isEmpty())
        assertFalse(state.isSearchActive)
        assertNull(state.statusFilter)
        assertFalse(state.isLoading)
    }

    @Test
    fun `SearchUiState copy with query`() {
        val state = SearchUiState().copy(query = "naruto", isSearchActive = true)
        assertEquals("naruto", state.query)
        assertTrue(state.isSearchActive)
    }
}
