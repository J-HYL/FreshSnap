package com.marujho.freshsnap.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.google.firebase.auth.FirebaseAuth


@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {

    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val expiryDays by viewModel.expiryAlertDays.collectAsState()
    var showAlertOptions by remember { mutableStateOf(false) }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {

            UserHeader()

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(16.dp))

            SettingsItem(
                title = "Dark Theme / Light Theme",
                onClick = { },
                trailing = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = {
                            viewModel.toggleDarkMode(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            )

            SettingsItem(
                title = "Mi Cuenta",
                subtitle = "Nombre, edad, sexo, idioma.",
                onClick = { navController.navigate("settings_account") }
            )

            SettingsItem(
                title = "Alergias",
                subtitle = "Leche, gluten, huevos.",
                onClick = { navController.navigate("settings_allergy") }
            )

            SettingsItem(
                title = "Alertas caducidad",
                subtitle = when (expiryDays) {
                    3 -> "3 días antes"
                    5 -> "5 días antes"
                    7 -> "1 semana antes"
                    else -> ""
                },
                onClick = {
                    showAlertOptions = !showAlertOptions
                }
            )

            if (showAlertOptions) {

                val options = listOf(
                    3 to "3 días antes",
                    5 to "5 días antes",
                    7 to "1 semana antes"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp)
                ) {

                    options.forEach { (days, label) ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setExpiryDays(days)
                                    showAlertOptions = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected = expiryDays == days,
                                onClick = {
                                    viewModel.setExpiryDays(days)
                                    showAlertOptions = false
                                }
                            )

                            Spacer(Modifier.width(8.dp))

                            Text(label)
                        }
                    }
                }

            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.logout {

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cerrar sesión")
            }

        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(
            navController = rememberNavController()
        )
    }
}
