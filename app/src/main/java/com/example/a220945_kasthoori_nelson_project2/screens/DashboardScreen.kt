package com.example.a220945_kasthoori_nelson_project2.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.a220945_kasthoori_nelson_project2.EduQuestViewModel

@Composable
fun DashboardScreen(viewModel: EduQuestViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val displayedCourses = allCourses.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery, ignoreCase = true)
    }
    val coreCourses = displayedCourses.filter { it.isCore }
    val citraCourses = displayedCourses.filter { !it.isCore }

    // --- LOGOUT DIALOG ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Switch User", fontWeight = FontWeight.Bold) },
            text = {
                Text("Your progress is saved. Another user can log in with their own matric number, and you can return anytime by entering your matric number again.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logoutUser()
                    navController.navigate("ProfileSetup") {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Log Out", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // --- RESET DIALOG ---
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Data", fontWeight = FontWeight.Bold) },
            text = {
                Text("⚠️ This will permanently delete your profile, XP, streak, check-in history, and all progress. This cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    viewModel.resetCurrentUser()
                    navController.navigate("ProfileSetup") {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Reset Everything", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search courses...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray
            )
        )

        // Profile Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Welcome back,", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    Text(
                        uiState.name.ifEmpty { "Student" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("🏆 ${uiState.currentTitle}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFCD34D))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${uiState.matricNumber} | ${uiState.program}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }

                // Two icon buttons — clearly visible on dark navy card
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Logout — bright yellow, door-exit icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFBBF24), shape = RoundedCornerShape(10.dp))
                            .clickable { showLogoutDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚪", fontSize = 20.sp)
                    }
                    // Reset — bright red, trash icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFEF4444), shape = RoundedCornerShape(10.dp))
                            .clickable { showResetDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🗑️", fontSize = 20.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = { navController.navigate("ActiveQuests") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("View My Custom Quests", fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate("CampusCheckIn") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("📍 Campus Check-In (+50 XP)", fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate("StudyMap") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("🗺️ Study Map & Leaderboard", fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gamification Stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(id = android.R.drawable.ic_menu_today), contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Streak", fontSize = 10.sp, color = Color.Gray)
                        Text("${uiState.currentStreak} Days", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(id = android.R.drawable.star_on), contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Total XP", fontSize = 10.sp, color = Color.Gray)
                        Text("${uiState.totalXP} XP", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Core Courses
        if (coreCourses.isNotEmpty()) {
            Text("Current Semester Courses", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            coreCourses.forEach { course ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable {
                            viewModel.selectQuest(course.id)
                            navController.navigate("QuestDetails")
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(course.title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFE2E8F0), shape = RoundedCornerShape(4.dp))) {
                            Box(modifier = Modifier.fillMaxWidth(course.progress).fillMaxHeight().background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${(course.progress * 100).toInt()}% Mastered", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Citra Courses
        if (citraCourses.isNotEmpty()) {
            Text("Explore Citra Quests", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            citraCourses.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    rowItems.forEach { course ->
                        AnimatedSubjectCard(
                            title = course.id,
                            subtitle = course.title,
                            details = course.description,
                            bg = MaterialTheme.colorScheme.primary,
                            txt = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.selectQuest(course.id)
                            navController.navigate("QuestDetails")
                        }
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AnimatedSubjectCard(
    title: String,
    subtitle: String,
    details: String,
    bg: Color,
    txt: Color,
    modifier: Modifier,
    onClickAction: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.clickable { expanded = !expanded }.animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = txt, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = txt, fontSize = 12.sp, lineHeight = 14.sp)

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = details, color = txt, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClickAction,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = bg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}