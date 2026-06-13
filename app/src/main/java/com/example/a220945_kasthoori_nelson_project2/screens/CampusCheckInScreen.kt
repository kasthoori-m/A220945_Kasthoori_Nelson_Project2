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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.a220945_kasthoori_nelson_project2.EduQuestViewModel
import com.example.a220945_kasthoori_nelson_project2.data.LocationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CampusCheckInScreen(viewModel: EduQuestViewModel, navController: NavController) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val alreadyCheckedIn by viewModel.alreadyCheckedInToday.collectAsState()

    // collectAsStateWithLifecycle is preferred for database Flows —
    // it pauses collection when the app goes to background, saving battery
    val checkInHistory by viewModel.checkInHistory.collectAsStateWithLifecycle(initialValue = emptyList())

    val locationHelper = remember { LocationHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    var locationStatus by remember { mutableStateOf("Tap the button to detect your location") }
    var isLoading by remember { mutableStateOf(false) }
    var detectedOnCampus by remember { mutableStateOf<Boolean?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Check if student already checked in today when screen opens
    LaunchedEffect(Unit) {
        viewModel.checkIfAlreadyCheckedIn()
    }

    // PERMISSION LAUNCHER — handles the system permission dialog result
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isLoading = true
            locationStatus = "Detecting your location..."
            coroutineScope.launch {
                fetchLocationAndCheckIn(
                    locationHelper = locationHelper,
                    viewModel = viewModel,
                    onStatusUpdate = { locationStatus = it },
                    onCampusResult = { detectedOnCampus = it },
                    onDone = { isLoading = false }
                )
            }
        } else {
            locationStatus = "Location permission denied. Please enable it in Settings to use this feature."
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
            Text("Campus Check-In", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- MAIN CHECK-IN CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = primaryColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📍", fontSize = 48.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Study on Campus Today?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Check in from UKM campus to earn your daily +50 XP bonus!",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Status card — colour changes based on GPS result
                val statusBg = when (detectedOnCampus) {
                    true  -> Color(0xFF16A34A)              // Green — on campus
                    false -> Color(0xFFDC2626)              // Red — off campus
                    null  -> Color.White.copy(alpha = 0.15f) // Neutral — not checked yet
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = statusBg)
                ) {
                    Text(
                        text = locationStatus,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Main action button
                Button(
                    onClick = {
                        val fineGranted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val coarseGranted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (fineGranted || coarseGranted) {
                            isLoading = true
                            locationStatus = "Detecting your location..."
                            coroutineScope.launch {
                                fetchLocationAndCheckIn(
                                    locationHelper = locationHelper,
                                    viewModel = viewModel,
                                    onStatusUpdate = { locationStatus = it },
                                    onCampusResult = { detectedOnCampus = it },
                                    onDone = { isLoading = false }
                                )
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
                    enabled = !alreadyCheckedIn && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = secondaryColor,
                        disabledContainerColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (alreadyCheckedIn) "✅ Already Checked In Today" else "📡 Detect My Location",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                if (!alreadyCheckedIn) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "+50 XP on successful campus check-in",
                        fontSize = 11.sp,
                        color = Color(0xFFFCD34D),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- STUDENT STATS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total XP", fontSize = 11.sp, color = Color.Gray)
                    Text("${uiState.totalXP}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor)
                }
                HorizontalDivider(modifier = Modifier.height(40.dp).width(1.dp), color = Color.LightGray)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Streak", fontSize = 11.sp, color = Color.Gray)
                    Text("${uiState.currentStreak} days", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor)
                }
                HorizontalDivider(modifier = Modifier.height(40.dp).width(1.dp), color = Color.LightGray)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Check-ins", fontSize = 11.sp, color = Color.Gray)
                    Text("${checkInHistory.size}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CHECK-IN HISTORY from Room ---
        Text("Check-In History", style = MaterialTheme.typography.titleMedium, color = primaryColor)
        Spacer(modifier = Modifier.height(12.dp))

        if (checkInHistory.isEmpty()) {
            Text("No check-ins yet. Visit campus to start earning XP!", color = Color.Gray, fontSize = 13.sp)
        } else {
            checkInHistory.take(10).forEach { record ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(record.locationName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                    .format(Date(record.timestamp)),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            "+${record.xpEarned} XP",
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------------------
// PRIVATE HELPER — GPS fetch + campus check + ViewModel save
// Extracted from the Composable to keep UI code clean
// ---------------------------------------------------------------------------
private suspend fun fetchLocationAndCheckIn(
    locationHelper: LocationHelper,
    viewModel: EduQuestViewModel,
    onStatusUpdate: (String) -> Unit,
    onCampusResult: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    val location = locationHelper.getCurrentLocation()

    if (location == null) {
        onStatusUpdate("Could not detect location. Make sure GPS is enabled and try again.")
        onCampusResult(false)
        onDone()
        return
    }

    val lat = location.latitude
    val lon = location.longitude
    val onCampus = locationHelper.isNearCampus(lat, lon)
    val locationName = locationHelper.getLocationName(lat, lon)

    onCampusResult(onCampus)

    if (onCampus) {
        viewModel.saveCheckIn(latitude = lat, longitude = lon, locationName = locationName, xpEarned = 50)
        onStatusUpdate("✅ You're on campus! +50 XP awarded. Keep studying!")
    } else {
        onStatusUpdate("📍 You're currently off campus (%.4f, %.4f). Visit UKM to earn XP!".format(lat, lon))
    }

    onDone()
}