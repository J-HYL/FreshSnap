package com.marujho.freshsnap.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.marujho.freshsnap.R

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {

    val isDarkMode by viewModel.isDarkMode.collectAsState()

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
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(16.dp))

            SettingsItem(
                title = stringResource(R.string.settings_theme),
                onClick = {},
                trailing = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = {
                            viewModel.toggleDarkMode(it)
                        }
                    )
                }
            )

            SettingsItem(
                title = stringResource(R.string.settings_account),
                subtitle = stringResource(R.string.settings_account_sub),
                onClick = { navController.navigate("settings_account") }
            )

            SettingsItem(
                title = stringResource(R.string.settings_allergies),
                subtitle = stringResource(R.string.settings_allergies_sub),
                onClick = { navController.navigate("settings_allergy") }
            )

            SettingsItem(
                title = stringResource(R.string.settings_alerts),
                subtitle = stringResource(R.string.settings_alerts_sub),
                onClick = { navController.navigate("settings_alert") }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.logout { }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.logout_button))
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