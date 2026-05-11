package com.comi.reader.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.comi.reader.domain.model.ReadingMode
import com.comi.reader.ui.components.BrowseScreen
import com.comi.reader.ui.history.HistoryScreen
import com.comi.reader.ui.library.LibraryScreen
import com.comi.reader.ui.navigation.Screen
import com.comi.reader.ui.navigation.bottomNavItems
import com.comi.reader.ui.reader.ReaderScreen
import com.comi.reader.ui.settings.SettingsScreen
import com.comi.reader.ui.theme.ComiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by rememberSaveable { mutableStateOf(false) }
            var defaultReadingMode by rememberSaveable { mutableStateOf(ReadingMode.LEFT_TO_RIGHT) }

            ComiTheme(darkTheme = isDarkMode) {
                ComiApp(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = it },
                    defaultReadingMode = defaultReadingMode,
                    onSetDefaultReadingMode = { defaultReadingMode = it }
                )
            }
        }
    }
}

@Composable
fun ComiApp(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    defaultReadingMode: ReadingMode,
    onSetDefaultReadingMode: (ReadingMode) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Library.route,
        Screen.History.route,
        Screen.Browse.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Library.route) {
                LibraryScreen(
                    onComicClick = { comicId ->
                        navController.navigate(Screen.Reader.createRoute(comicId))
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onComicClick = { comicId ->
                        navController.navigate(Screen.Reader.createRoute(comicId))
                    }
                )
            }

            composable(Screen.Browse.route) {
                BrowseScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    defaultReadingMode = defaultReadingMode,
                    onSetDefaultReadingMode = onSetDefaultReadingMode
                )
            }

            composable(
                route = Screen.Reader.route,
                arguments = listOf(navArgument("comicId") { type = NavType.LongType })
            ) {
                ReaderScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
