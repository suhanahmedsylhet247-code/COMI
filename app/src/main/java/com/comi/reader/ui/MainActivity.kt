package com.comi.reader.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.comi.reader.domain.model.ThemeMode
import com.comi.reader.ui.backup.BackupScreen
import com.comi.reader.ui.category.CategoryScreen
import com.comi.reader.ui.components.BrowseScreen
import com.comi.reader.ui.detail.ComicDetailScreen
import com.comi.reader.ui.downloads.DownloadsScreen
import com.comi.reader.ui.history.HistoryScreen
import com.comi.reader.ui.library.LibraryScreen
import com.comi.reader.ui.navigation.Screen
import com.comi.reader.ui.navigation.bottomNavItems
import com.comi.reader.ui.reader.ReaderScreen
import com.comi.reader.ui.search.SearchScreen
import com.comi.reader.ui.settings.SettingsScreen
import com.comi.reader.ui.settings.SettingsViewModel
import com.comi.reader.ui.statistics.StatisticsScreen
import com.comi.reader.ui.theme.ComiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComiApp()
        }
    }
}

@Composable
fun ComiApp() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isDark = when (settingsState.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    ComiTheme(
        darkTheme = isDark,
        amoledDark = settingsState.amoledDark,
        dynamicColor = settingsState.dynamicColors
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Library.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Library.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Library.route) {
                    LibraryScreen(
                        onComicClick = { comicId ->
                            navController.navigate(Screen.ComicDetail.createRoute(comicId))
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        }
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(onComicClick = { comicId ->
                        navController.navigate(Screen.Reader.createRoute(comicId))
                    })
                }

                composable(Screen.Browse.route) {
                    BrowseScreen()
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                        onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                        onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                        onNavigateToBackup = { navController.navigate(Screen.Backup.route) }
                    )
                }

                composable(
                    Screen.ComicDetail.route,
                    arguments = listOf(navArgument("comicId") { type = NavType.LongType })
                ) {
                    ComicDetailScreen(
                        onBack = { navController.popBackStack() },
                        onReadChapter = { comicId, _ ->
                            navController.navigate(Screen.Reader.createRoute(comicId))
                        }
                    )
                }

                composable(
                    Screen.Reader.route,
                    arguments = listOf(navArgument("comicId") { type = NavType.LongType })
                ) {
                    ReaderScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        onComicClick = { comicId ->
                            navController.navigate(Screen.ComicDetail.createRoute(comicId))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Categories.route) {
                    CategoryScreen(
                        onCategoryClick = { /* Show category comics */ }
                    )
                }

                composable(Screen.Downloads.route) {
                    DownloadsScreen()
                }

                composable(Screen.Statistics.route) {
                    StatisticsScreen()
                }

                composable(Screen.Backup.route) {
                    BackupScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
