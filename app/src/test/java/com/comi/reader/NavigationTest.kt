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
        assertEquals("detail/{comicId}", Screen.ComicDetail.route)
    }

    @Test
    fun `reader route creates correct path`() {
        assertEquals("reader/42", Screen.Reader.createRoute(42))
        assertEquals("reader/1", Screen.Reader.createRoute(1))
    }

    @Test
    fun `comic detail route creates correct path`() {
        assertEquals("detail/42", Screen.ComicDetail.createRoute(42))
    }

    @Test
    fun `new screen routes are correct`() {
        assertEquals("search", Screen.Search.route)
        assertEquals("categories", Screen.Categories.route)
        assertEquals("downloads", Screen.Downloads.route)
        assertEquals("statistics", Screen.Statistics.route)
        assertEquals("backup", Screen.Backup.route)
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
    fun `bottom nav items have correct routes`() {
        assertEquals(Screen.Library.route, bottomNavItems[0].route)
        assertEquals(Screen.History.route, bottomNavItems[1].route)
        assertEquals(Screen.Browse.route, bottomNavItems[2].route)
        assertEquals(Screen.Settings.route, bottomNavItems[3].route)
    }
}
