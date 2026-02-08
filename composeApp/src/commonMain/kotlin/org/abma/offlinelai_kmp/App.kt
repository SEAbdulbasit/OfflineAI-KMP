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
            startDestination = "chat"
        ) {
            composable("chat") {
                ChatScreen(
                    onNavigateToSettings = {
                        try {
                            navController.navigate("settings")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    viewModel = chatViewModel
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = {
                        try {
                            navController.popBackStack()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onLoadModel = { path, config ->
                        try {
                            chatViewModel.onAction(ChatAction.LoadModel(path, config))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onRemoveModel = { path ->
                        try {
                            chatViewModel.onAction(ChatAction.RemoveModel(path))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    loadedModels = uiState.loadedModels,
                    currentModelPath = uiState.currentModelPath
                )
            }
        }
    }
}