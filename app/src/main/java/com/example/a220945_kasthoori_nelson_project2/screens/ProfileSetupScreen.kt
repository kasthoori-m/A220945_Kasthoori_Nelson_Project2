package com.example.a220945_kasthoori_nelson_project2.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.a220945_kasthoori_nelson_project2.EduQuestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(viewModel: EduQuestViewModel, navController: NavController) {
    var nameInput by remember { mutableStateOf("") }
    var matricInput by remember { mutableStateOf("") }

    val programs = listOf("Software Engineering", "Computer Science", "Information Technology")
    var expanded by remember { mutableStateOf(false) }
    var selectedProgram by remember { mutableStateOf("Choose your program") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Welcome to EduQuest",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Set Up Your Profile",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it; errorMessage = "" },
                    label = { Text("Student Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = matricInput,
                    onValueChange = { matricInput = it; errorMessage = "" },
                    label = { Text("Matric Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedProgram,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Program") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        programs.forEach { option ->
                            val isSelected = selectedProgram == option
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedProgram = option
                                    expanded = false
                                    errorMessage = ""
                                },
                                modifier = Modifier.background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                                )
                            )
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (nameInput.isBlank() || matricInput.isBlank() || selectedProgram == "Choose your program") {
                            errorMessage = "Please fill in all fields to continue."
                        } else {
                            errorMessage = ""
                            viewModel.updateProfile(nameInput, matricInput, selectedProgram)
                            navController.navigate("Dashboard")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Enter Dashboard")
                }
            }
        }
    }
}
