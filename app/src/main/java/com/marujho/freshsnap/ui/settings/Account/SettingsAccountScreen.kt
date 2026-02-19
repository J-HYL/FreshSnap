package com.marujho.freshsnap.ui.settings.Account

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun SettingsAccountScreen(

    viewModel: SettingsAccountViewModel = viewModel()
) {
    val context = LocalContext.current

    // Collectar los valores actuales del usuario
    val name by viewModel.userName.collectAsState()
    val age by viewModel.userAge.collectAsState()
    val gender by viewModel.userGender.collectAsState()
    val language by viewModel.userLanguage.collectAsState()

    // Estados locales para editar
    var editedName by remember { mutableStateOf("") }
    var tempName by remember { mutableStateOf("") }
    var isEditingName by remember { mutableStateOf(false) }

    var editedAge by remember { mutableStateOf("") }
    var tempAge by remember { mutableStateOf("") }
    var isEditingAge by remember { mutableStateOf(false) }

    var editedGender by remember { mutableStateOf("") }
    var editedLanguage by remember { mutableStateOf("") }

    // Inicializar valores actuales al entrar
    LaunchedEffect(name) { editedName = name; tempName = name }
    LaunchedEffect(age) { editedAge = age.toString(); tempAge = age.toString() }
    LaunchedEffect(gender) { editedGender = gender }
    LaunchedEffect(language) { editedLanguage = language }

    val genders = listOf("Masculino", "Femenino", "Otro")
    val languages = listOf("Español", "English")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Cuenta") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Nombre con icono y dialog
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isEditingName = true }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Nombre: $editedName",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar nombre"
                )
            }

            if (isEditingName) {
                AlertDialog(
                    onDismissRequest = { isEditingName = false },
                    title = { Text("Editar nombre") },
                    text = {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Nombre") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            editedName = tempName
                            isEditingName = false
                        }) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isEditingName = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Edad con icono y dialog
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isEditingAge = true }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Edad: $editedAge años",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar edad"
                )
            }

            if (isEditingAge) {
                AlertDialog(
                    onDismissRequest = { isEditingAge = false },
                    title = { Text("Editar edad") },
                    text = {
                        OutlinedTextField(
                            value = tempAge,
                            onValueChange = { tempAge = it.filter { c -> c.isDigit() } },
                            label = { Text("Edad") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            editedAge = tempAge
                            isEditingAge = false
                        }) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isEditingAge = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Sexo
            Text("Sexo", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            val gendersList = genders
            gendersList.forEach { g ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = editedGender == g,
                        onClick = { editedGender = g }
                    )
                    Text(g)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Idioma
            Text("Idioma", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            val languagesList = languages
            languagesList.forEach { l ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = editedLanguage == l,
                        onClick = { editedLanguage = l }
                    )
                    Text(l)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Guardar cambios
            Button(
                onClick = {
                    viewModel.saveUserData(
                        editedName,
                        editedAge.toIntOrNull() ?: 0,
                        editedGender,
                        editedLanguage,

                    )
                    Toast.makeText(context, "¡Datos guardados!", Toast.LENGTH_SHORT).show()

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")

            }
        }
    }
}
