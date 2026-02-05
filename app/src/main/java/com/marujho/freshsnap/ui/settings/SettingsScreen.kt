package com.marujho.freshsnap.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        SettingsItem("Mi cuenta") {
            navController.navigate("settings_account")
        }

        SettingsItem("Unidades de medida") {
            navController.navigate("settings_units")
        }

        SettingsItem("Permisos") {
            navController.navigate("settings_permissions")
        }

        SettingsItem("Alergias") {
            navController.navigate("settings_allergy")
        }

        SettingsItem("Alertas de caducidad") {
            navController.navigate("settings_alert")
        }

        SettingsItem("Copia de seguridad") {
            navController.navigate("settings_backup")
        }




    }
}

@Composable
fun SettingsItem(
    title: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        modifier = Modifier.clickable { onClick() }
    )
}
