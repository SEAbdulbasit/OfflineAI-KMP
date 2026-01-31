package org.abma.offlinelai_kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.abma.offlinelai_kmp.ui.screens.ChatScreen
import org.abma.offlinelai_kmp.ui.screens.SettingsScreen
import org.abma.offlinelai_kmp.ui.theme.GemmaTheme
import org.abma.offlinelai_kmp.ui.viewmodel.ChatViewModel

@Composable
fun App() {
    GemmaTheme {
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
                            chatViewModel.loadModel(path, config)
//                            navController.popBackStack()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onRemoveModel = { path ->
                        try {
                            chatViewModel.removeLoadedModel(path)
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