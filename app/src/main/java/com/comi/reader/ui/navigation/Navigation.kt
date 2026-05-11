package com.comi.reader.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Library : Screen("library")
    data object History : Screen("history")
    data object Browse : Screen("browse")
    data object Settings : Screen("settings")
    data object Reader : Screen("reader/{comicId}") {
        fun createRoute(comicId: Long) = "reader/$comicId"
    }
    data object ComicDetail : Screen("detail/{comicId}") {
        fun createRoute(comicId: Long) = "detail/$comicId"
    }
    data object Search : Screen("search")
    data object Categories : Screen("categories")
    data object Downloads : Screen("downloads")
    data object Statistics : Screen("statistics")
    data object Backup : Screen("backup")
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Library", Icons.Filled.LibraryBooks, Screen.Library.route),
    BottomNavItem("History", Icons.Filled.History, Screen.History.route),
    BottomNavItem("Browse", Icons.Filled.Explore, Screen.Browse.route),
    BottomNavItem("Settings", Icons.Filled.Settings, Screen.Settings.route)
)
