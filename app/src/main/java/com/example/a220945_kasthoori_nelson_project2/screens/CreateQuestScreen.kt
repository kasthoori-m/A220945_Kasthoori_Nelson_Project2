package com.example.a220945_kasthoori_nelson_project2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.a220945_kasthoori_nelson_project2.EduQuestViewModel

@Composable
fun CreateQuestScreen(viewModel: EduQuestViewModel, navController: NavController) {
    var titleInput by remember { mutableStateOf("") }
    var xpInput by remember { mutableStateOf("50") }
    var errorMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Forging a New Quest",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = titleInput,
            onValueChange = { titleInput = it; errorMessage = "" },
            label = { Text("What do you need to study?") },
            placeholder = { Text("e.g., Read Chapter 5") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // FIX: Added KeyboardOptions(keyboardType = KeyboardType.Number) so the numeric
        // keyboard opens automatically on mobile instead of the full keyboard.
        OutlinedTextField(
            value = xpInput,
            onValueChange = { xpInput = it; errorMessage = "" },
            label = { Text("XP Reward") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val xpValue = xpInput.toIntOrNull()
                    if (titleInput.isBlank() || xpValue == null || xpValue <= 0) {
                        errorMessage = "Please enter a valid task and XP amount."
                    } else {
                        viewModel.addCustomQuest(titleInput, xpValue)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Quest")
            }
        }
    }
}
