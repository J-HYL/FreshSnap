package com.marujho.freshsnap.ui.settings.Account

import androidx.compose.material3.*
import androidx.compose.runtime.Composable


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAccountScreen(
    viewModel: SettingsAccountViewModel = viewModel()
) {

    val name by viewModel.userName.collectAsState()
    val age by viewModel.userAge.collectAsState()
    val gender by viewModel.userGender.collectAsState()
    val language by viewModel.userLanguage.collectAsState()

    var editedName by remember { mutableStateOf(name) }
    var editedAge by remember { mutableStateOf(age.toString()) }
    var editedGender by remember { mutableStateOf(gender) }
    var editedLanguage by remember { mutableStateOf(language) }

    val genders = listOf("Masculino", "Femenino", "Otro")
    val languages = listOf("Español", "English")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Cuenta") },
            )
        }
    ){ padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = editedAge,
                onValueChange = { editedAge = it.filter { c -> c.isDigit() } },
                label = { Text("Edad") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("Sexo", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))

            genders.forEach {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = editedGender == it,
                        onClick = { editedGender = it }
                    )
                    Text(it)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Idioma", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))

            languages.forEach {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = editedLanguage == it,
                        onClick = { editedLanguage = it }
                    )
                    Text(it)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveUserData(
                        editedName,
                        editedAge.toIntOrNull() ?: 0,
                        editedGender,
                        editedLanguage
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
        }
    }
}
