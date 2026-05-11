package com.comi.reader

import com.comi.reader.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryModelTest {

    @Test
    fun `Category creation`() {
        val category = Category(id = 1, name = "Manga", order = 0)
        assertEquals(1L, category.id)
        assertEquals("Manga", category.name)
        assertEquals(0, category.order)
    }

    @Test
    fun `Category copy with new name`() {
        val category = Category(id = 1, name = "Action", order = 0)
        val updated = category.copy(name = "Adventure")
        assertEquals("Adventure", updated.name)
        assertEquals(1L, updated.id)
    }
}
