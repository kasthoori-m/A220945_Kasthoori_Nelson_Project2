package com.example.a220945_kasthoori_nelson_project2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.a220945_kasthoori_nelson_project2.screens.ActiveQuestsScreen
import com.example.a220945_kasthoori_nelson_project2.screens.CampusCheckInScreen
import com.example.a220945_kasthoori_nelson_project2.screens.CreateQuestScreen
import com.example.a220945_kasthoori_nelson_project2.screens.DashboardScreen
import com.example.a220945_kasthoori_nelson_project2.screens.ProfileSetupScreen
import com.example.a220945_kasthoori_nelson_project2.screens.QuestDetailsScreen
import com.example.a220945_kasthoori_nelson_project2.screens.StudyMapScreen
import com.example.a220945_kasthoori_nelson_project2.ui.theme.EduQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EduQuestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EduQuestApp()
                }
            }
        }
    }
}

@Composable
fun EduQuestApp(viewModel: EduQuestViewModel = viewModel()) {
    val navController = rememberNavController()
    val startDestination by viewModel.startDestination.collectAsState()

    // NavHost always starts at "Loading" screen.
    // Once the ViewModel finishes checking Room (init block), it updates
    // startDestination and we navigate accordingly — only once.
    NavHost(navController = navController, startDestination = "Loading") {

        // LOADING SCREEN — shown for a split second while Room is queried
        composable("Loading") {
            LoadingScreen()
            // React to ViewModel's routing decision
            LaunchedEffect(startDestination) {
                when (startDestination) {
                    is StartDestination.Dashboard -> {
                        navController.navigate("Dashboard") {
                            popUpTo("Loading") { inclusive = true } // Remove Loading from back stack
                        }
                    }
                    is StartDestination.ProfileSetup -> {
                        navController.navigate("ProfileSetup") {
                            popUpTo("Loading") { inclusive = true }
                        }
                    }
                    is StartDestination.Loading -> { /* Still waiting — do nothing */ }
                }
            }
        }

        // PROJECT 1 SCREENS
        composable("ProfileSetup") {
            ProfileSetupScreen(viewModel = viewModel, navController = navController)
        }
        composable("Dashboard") {
            DashboardScreen(viewModel = viewModel, navController = navController)
        }
        composable("QuestDetails") {
            QuestDetailsScreen(viewModel = viewModel, navController = navController)
        }
        composable("ActiveQuests") {
            ActiveQuestsScreen(viewModel = viewModel, navController = navController)
        }
        composable("CreateQuest") {
            CreateQuestScreen(viewModel = viewModel, navController = navController)
        }

        // PROJECT 2 SCREENS
        composable("CampusCheckIn") {
            CampusCheckInScreen(viewModel = viewModel, navController = navController)
        }
        composable("StudyMap") {
            StudyMapScreen(viewModel = viewModel, navController = navController)
        }
    }
}

// Simple centered loading spinner shown during the Room check on startup
@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}