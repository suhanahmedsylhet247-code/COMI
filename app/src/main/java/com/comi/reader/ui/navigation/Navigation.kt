package com.comi.reader.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Library : Screen("library")
    data object History : Screen("history")
    data object Browse : Screen("browse")
    data object Settings : Screen("settings")
    data object Reader : Screen("reader/{comicId}") {
        fun createRoute(comicId: Long) = "reader/$comicId"
    }
    data object ComicDetail : Screen("comic/{comicId}") {
        fun createRoute(comicId: Long) = "comic/$comicId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Library, "Library", Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark),
    BottomNavItem(Screen.History, "History", Icons.Filled.History, Icons.Outlined.History),
    BottomNavItem(Screen.Browse, "Browse", Icons.Filled.Explore, Icons.Outlined.Explore),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)
