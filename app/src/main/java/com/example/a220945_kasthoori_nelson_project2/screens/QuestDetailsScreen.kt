package com.example.a220945_kasthoori_nelson_project2.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.a220945_kasthoori_nelson_project2.EduQuestViewModel

@Composable
fun QuestDetailsScreen(viewModel: EduQuestViewModel, navController: NavController) {
    val selectedQuest by viewModel.selectedQuest.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val activeCourse = allCourses.find { it.id == selectedQuest }
    val primaryColor = if (activeCourse?.isCore == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    // FIX: Quiz data is now read from ViewModel, not defined here.
    // This prevents the entire data structure from being recreated on every recomposition.
    val courseQuizzes = viewModel.courseQuizzes

    var activeLessonName by remember { mutableStateOf<String?>(null) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf(-1) }
    var score by remember { mutableStateOf(0) }
    var quizFinished by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Back button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { navController.popBackStack() }
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, shape = CircleShape)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Abandon Quest", fontWeight = FontWeight.Bold, color = primaryColor)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = primaryColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = activeCourse?.title ?: "Course", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                // Citra (non-core) Course → Reading UI
                if (activeCourse?.isCore == false) {
                    val isCompleted = activeCourse.progress >= 1f
                    Text(text = activeCourse.description, color = Color.White, fontSize = 16.sp, lineHeight = 24.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            viewModel.completeReading(selectedQuest)
                            navController.popBackStack()
                        },
                        enabled = !isCompleted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (isCompleted) "Lesson Completed" else "Finish Reading (+50 XP)",
                            color = if (isCompleted) Color.White else primaryColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Core Course → Lesson Selection Menu
                else if (activeLessonName == null) {
                    Text("Select a lesson to begin:", color = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(16.dp))

                    courseQuizzes[selectedQuest]?.keys?.forEach { lessonName ->
                        val lessonId = "${selectedQuest}_$lessonName"
                        val highScore = uiState.lessonHighScores[lessonId] ?: 0

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { activeLessonName = lessonName },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lessonName, color = primaryColor, fontWeight = FontWeight.Bold)
                                Text("Best: $highScore/3", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                // Active Quiz Engine
                else {
                    val currentQuiz = courseQuizzes[selectedQuest]?.get(activeLessonName) ?: emptyList()
                    val lessonId = "${selectedQuest}_$activeLessonName"
                    val previousHighScore = uiState.lessonHighScores[lessonId] ?: 0

                    if (quizFinished) {
                        // FIX: xpEarned is still shown in the UI for user feedback,
                        // but the actual calculation authority stays in the ViewModel.
                        // This is display-only — the ViewModel recalculates on completeQuiz().
                        val xpEarned = if (score > previousHighScore) (score - previousHighScore) * 50 else 0

                        Text("Quiz Complete!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("You scored $score out of 3.", fontSize = 16.sp, color = Color.White)

                        if (xpEarned > 0) {
                            Text("You beat your high score! Earned +$xpEarned XP!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCD34D))
                        } else {
                            Text("You didn't beat your high score of $previousHighScore. Keep trying!", fontSize = 16.sp, color = Color.LightGray)
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                viewModel.completeQuiz(selectedQuest, lessonId, score)
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Claim Rewards & Return", color = primaryColor, fontWeight = FontWeight.ExtraBold)
                        }

                    } else {
                        val currentQ = currentQuiz[currentQuestionIndex]

                        Text(
                            "$activeLessonName - Question ${currentQuestionIndex + 1} of 3",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(currentQ.text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 24.sp)
                        Spacer(modifier = Modifier.height(24.dp))

                        currentQ.options.forEachIndexed { index, answerText ->
                            val isSelected = selectedAnswer == index
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { selectedAnswer = index },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    answerText,
                                    modifier = Modifier.padding(16.dp),
                                    color = if (isSelected) primaryColor else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (selectedAnswer == currentQ.correctIndex) score++
                                if (currentQuestionIndex < 2) {
                                    currentQuestionIndex++
                                    selectedAnswer = -1
                                } else {
                                    quizFinished = true
                                }
                            },
                            enabled = selectedAnswer != -1,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                if (currentQuestionIndex == 2) "Finish Quiz" else "Next Question",
                                color = primaryColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
