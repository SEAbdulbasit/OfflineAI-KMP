package org.abma.offlinelai_kmp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.abma.offlinelai_kmp.ui.screens.ChatScreen
import org.abma.offlinelai_kmp.ui.screens.SettingsScreen
import org.abma.offlinelai_kmp.ui.theme.GemmaTheme
import org.abma.offlinelai_kmp.ui.viewmodel.ChatAction
import org.abma.offlinelai_kmp.ui.viewmodel.ChatViewModel

/**
 * Navigation route constants for type-safe navigation.
 */
private object Routes {
    const val CHAT = "chat"
    const val SETTINGS = "settings"
}

@Composable
fun App() {
    val systemDarkTheme = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemDarkTheme) }

    GemmaTheme(
        darkTheme = isDarkTheme,
        onToggleTheme = { isDarkTheme = !isDarkTheme }
    ) {
        val navController = rememberNavController()
        val chatViewModel: ChatViewModel = viewModel { ChatViewModel() }
        val uiState by chatViewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = Routes.CHAT
        ) {
            composable(Routes.CHAT) {
                ChatScreen(
                    onNavigateToSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    viewModel = chatViewModel
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onLoadModel = { path, config ->
                        chatViewModel.onAction(ChatAction.LoadModel(path, config))
                    },
                    onRemoveModel = { path ->
                        chatViewModel.onAction(ChatAction.RemoveModel(path))
                    },
                    loadedModels = uiState.loadedModels,
                    currentModelPath = uiState.currentModelPath
                )
            }
        }
    }
}