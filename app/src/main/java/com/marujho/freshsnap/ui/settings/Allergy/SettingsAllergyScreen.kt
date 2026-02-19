package com.marujho.freshsnap.ui.settings.Allergy

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAllergyScreen(
    viewModel: SettingsAllergyViewModel = viewModel()
) {

    val savedAllergies by viewModel.userAllergies.collectAsState()

    var selectedAllergies by remember { mutableStateOf(setOf<String>()) }

    // Sincronizar cuando cargan los datos
    LaunchedEffect(savedAllergies) {
        selectedAllergies = savedAllergies
    }

    val possibleAllergies = listOf(
        "Leche" to "en:milk",
        "Gluten" to "en:gluten",
        "Huevos" to "en:eggs",
        "Frutos secos" to "en:nuts",
        "Soja" to "en:soybeans"
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Alergias") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            possibleAllergies.forEach { (label, tag) ->

                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = selectedAllergies.contains(tag),
                        onCheckedChange = { isChecked ->

                            selectedAllergies =
                                if (isChecked) {
                                    selectedAllergies + tag
                                } else {
                                    selectedAllergies - tag
                                }
                        }
                    )

                    Text(label)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveAllergies(selectedAllergies)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
        }
    }
}
