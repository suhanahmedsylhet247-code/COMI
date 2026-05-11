package com.comi.reader

import com.comi.reader.ui.navigation.Screen
import com.comi.reader.ui.navigation.bottomNavItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTest {

    @Test
    fun `screen routes are correct`() {
        assertEquals("library", Screen.Library.route)
        assertEquals("history", Screen.History.route)
        assertEquals("browse", Screen.Browse.route)
        assertEquals("settings", Screen.Settings.route)
        assertEquals("reader/{comicId}", Screen.Reader.route)
        assertEquals("comic/{comicId}", Screen.ComicDetail.route)
    }

    @Test
    fun `reader route creates correct path`() {
        assertEquals("reader/42", Screen.Reader.createRoute(42))
        assertEquals("reader/1", Screen.Reader.createRoute(1))
    }

    @Test
    fun `comic detail route creates correct path`() {
        assertEquals("comic/42", Screen.ComicDetail.createRoute(42))
    }

    @Test
    fun `bottom nav has four items`() {
        assertEquals(4, bottomNavItems.size)
    }

    @Test
    fun `bottom nav item labels`() {
        val labels = bottomNavItems.map { it.label }
        assertTrue("Library" in labels)
        assertTrue("History" in labels)
        assertTrue("Browse" in labels)
        assertTrue("Settings" in labels)
    }

    @Test
    fun `bottom nav items have correct screens`() {
        assertEquals(Screen.Library, bottomNavItems[0].screen)
        assertEquals(Screen.History, bottomNavItems[1].screen)
        assertEquals(Screen.Browse, bottomNavItems[2].screen)
        assertEquals(Screen.Settings, bottomNavItems[3].screen)
    }
}
