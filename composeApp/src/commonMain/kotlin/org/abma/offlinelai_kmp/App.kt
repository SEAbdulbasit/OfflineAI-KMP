package org.abma.offlinelai_kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

        NavHost(
            navController = navController,
            startDestination = "chat"
        ) {
            composable("chat") {
                ChatScreen(
                    onNavigateToSettings = { navController.navigate("settings") },
                    viewModel = chatViewModel
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLoadModel = { path, config ->
                        chatViewModel.loadModel(path, config)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}