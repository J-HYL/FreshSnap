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
import androidx.compose.ui.res.stringResource
import com.marujho.freshsnap.R
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList

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

    var editedName by remember { mutableStateOf("") }
    var tempName by remember { mutableStateOf("") }
    var isEditingName by remember { mutableStateOf(false) }

    var editedAge by remember { mutableStateOf("") }
    var tempAge by remember { mutableStateOf("") }
    var isEditingAge by remember { mutableStateOf(false) }

    var editedGender by remember { mutableStateOf("") }
    var editedLanguage by remember { mutableStateOf("") }

    LaunchedEffect(name) { editedName = name; tempName = name }
    LaunchedEffect(age) { editedAge = age.toString(); tempAge = age.toString() }
    LaunchedEffect(gender) { editedGender = gender }
    LaunchedEffect(language) { editedLanguage = language }

    val gendersList = listOf(
        stringResource(R.string.gender_male),
        stringResource(R.string.gender_female),
        stringResource(R.string.gender_other)
    )
    val languagesList = listOf(
        stringResource(R.string.language_spanish),
        stringResource(R.string.language_english)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_account)) }
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
                    text = stringResource(R.string.name_value, editedName),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_name_title)
                )
            }

            if (isEditingName) {
                AlertDialog(
                    onDismissRequest = { isEditingName = false },
                    title = { Text(stringResource(R.string.edit_name_title)) },
                    text = {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text(stringResource(R.string.general_name)) },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            editedName = tempName
                            isEditingName = false
                        }) {
                            Text(stringResource(R.string.save))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isEditingName = false }) {
                            Text(stringResource(R.string.cancel))
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
                    text = stringResource(R.string.age_value, editedAge),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_age_title)
                )
            }

            if (isEditingAge) {
                AlertDialog(
                    onDismissRequest = { isEditingAge = false },
                    title = { Text(stringResource(R.string.edit_age_title)) },
                    text = {
                        OutlinedTextField(
                            value = tempAge,
                            onValueChange = { tempAge = it.filter { c -> c.isDigit() } },
                            label = { Text(stringResource(R.string.general_age)) },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            editedAge = tempAge
                            isEditingAge = false
                        }) {
                            Text(stringResource(R.string.save))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isEditingAge = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Sexo
            Text(stringResource(R.string.gender_title), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
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
            Text(stringResource(R.string.language_title), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))

            val languagesList = listOf(
                stringResource(R.string.language_system) to "Sistema",
                stringResource(R.string.language_spanish) to "es",
                stringResource(R.string.language_english) to "en"
            )

            languagesList.forEach { (label, langCode) ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = editedLanguage == langCode || (editedLanguage.isEmpty() && langCode == "Sistema"),
                        onClick = {
                            editedLanguage = langCode

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val localeManager = context.getSystemService(LocaleManager::class.java)

                                if (langCode == "Sistema") {
                                    localeManager.applicationLocales = LocaleList.getEmptyLocaleList()
                                } else {
                                    localeManager.applicationLocales = LocaleList.forLanguageTags(langCode)
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "El cambio de idioma manual requiere Android 13+. Cambia el idioma desde los ajustes de tu teléfono.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                    Text(label)
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

                    Toast.makeText(
                        context,
                        context.getString(R.string.data_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_changes))
            }
        }
    }
}