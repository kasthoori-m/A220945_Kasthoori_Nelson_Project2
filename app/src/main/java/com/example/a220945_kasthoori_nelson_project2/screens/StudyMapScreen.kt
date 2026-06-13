package com.example.a220945_kasthoori_nelson_project2.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.a220945_kasthoori_nelson_project2.EduQuestViewModel
import com.example.a220945_kasthoori_nelson_project2.data.LocationHelper
import kotlinx.coroutines.launch

@Composable
fun StudyMapScreen(viewModel: EduQuestViewModel, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val studySpots by viewModel.studySpots.collectAsState()
    val isLoadingSpots by viewModel.studySpotsLoading.collectAsState()
    val spotsError by viewModel.studySpotsError.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val isLoadingLeaderboard by viewModel.leaderboardLoading.collectAsState()
    val leaderboardError by viewModel.leaderboardError.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val locationHelper = remember { LocationHelper(context) }
    var locationStatusMsg by remember { mutableStateOf("") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Fetch leaderboard when screen opens
    LaunchedEffect(Unit) {
        viewModel.fetchLeaderboard()
    }

    // Permission launcher for fetching study spots
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                locationStatusMsg = "Detecting location..."
                val location = locationHelper.getCurrentLocation()
                if (location != null) {
                    locationStatusMsg = ""
                    viewModel.fetchStudySpots(location.latitude, location.longitude)
                } else {
                    locationStatusMsg = "Could not detect location. Enable GPS and try again."
                }
            }
        } else {
            locationStatusMsg = "Location permission required to find nearby study spots."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, shape = CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Study Map", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---------------------------------------------------------------
        // SECTION 1: NEARBY STUDY SPOTS (REST API — Overpass/OpenStreetMap)
        // ---------------------------------------------------------------
        Text("📚 Nearby Study Spots", style = MaterialTheme.typography.titleMedium, color = primaryColor)
        Text(
            "Libraries, cafes, and universities near you via OpenStreetMap",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val fineGranted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val coarseGranted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (fineGranted || coarseGranted) {
                    coroutineScope.launch {
                        locationStatusMsg = "Detecting location..."
                        val location = locationHelper.getCurrentLocation()
                        if (location != null) {
                            locationStatusMsg = ""
                            viewModel.fetchStudySpots(location.latitude, location.longitude)
                        } else {
                            locationStatusMsg = "Could not detect location. Enable GPS and try again."
                        }
                    }
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("🔍 Find Study Spots Near Me", fontWeight = FontWeight.ExtraBold)
        }

        // Status / error messages
        if (locationStatusMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(locationStatusMsg, color = Color.Gray, fontSize = 12.sp)
        }
        if (spotsError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(spotsError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Loading spinner for API call
        if (isLoadingSpots) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = secondaryColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fetching from OpenStreetMap...", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        // Study spot cards from API response
        if (studySpots.isNotEmpty()) {
            studySpots.forEach { spot ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon based on amenity type
                        val icon = when (spot.amenity) {
                            "library" -> "📚"
                            "cafe" -> "☕"
                            "university", "college" -> "🏛️"
                            "park" -> "🌿"
                            else -> "📍"
                        }
                        Text(icon, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                spot.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                spot.displayType,
                                fontSize = 11.sp,
                                color = secondaryColor,
                                fontWeight = FontWeight.Bold
                            )
                            if (spot.lat != null && spot.lon != null) {
                                Text(
                                    "%.4f, %.4f".format(spot.lat, spot.lon),
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        } else if (!isLoadingSpots && spotsError.isEmpty() && locationStatusMsg.isEmpty()) {
            Text(
                "Tap the button above to discover study spots near your current location.",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ---------------------------------------------------------------
        // SECTION 2: CAMPUS LEADERBOARD (Firebase Firestore)
        // ---------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("🏆 Campus Leaderboard", style = MaterialTheme.typography.titleMedium, color = primaryColor)
                Text("Top students by XP — synced from Firebase", fontSize = 12.sp, color = Color.Gray)
            }
            TextButton(
                onClick = { viewModel.fetchLeaderboard() },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "Refresh",
                    color = secondaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoadingLeaderboard) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = primaryColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading from Firebase...", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        if (leaderboardError.isNotEmpty() && !isLoadingLeaderboard) {
            Text(leaderboardError, color = Color.Gray, fontSize = 13.sp)
        }

        if (leaderboard.isNotEmpty()) {
            leaderboard.forEachIndexed { index, entry ->
                val rankEmoji = when (index) {
                    0 -> "🥇"
                    1 -> "🥈"
                    2 -> "🥉"
                    else -> "  ${index + 1}."
                }
                // Highlight current user's own entry
                val isCurrentUser = entry.matricNumber == uiState.matricNumber
                val cardBg = if (isCurrentUser) primaryColor.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 4.dp else 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(rankEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    entry.name + if (isCurrentUser) " (You)" else "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isCurrentUser) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(entry.currentTitle, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Text(
                            "${entry.totalXP} XP",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }

        // Push current student to leaderboard manually
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                viewModel.pushToLeaderboard()
                viewModel.fetchLeaderboard()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("📤 Update My Leaderboard Entry", fontWeight = FontWeight.Bold, color = primaryColor)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}