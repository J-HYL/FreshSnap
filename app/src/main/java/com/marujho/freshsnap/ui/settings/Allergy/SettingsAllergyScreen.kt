package com.marujho.freshsnap.ui.settings.Allergy

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.platform.LocalContext


@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAllergyScreen(
    viewModel: SettingsAllergyViewModel = viewModel()
) {
    val context = LocalContext.current
    val savedAllergies by viewModel.userAllergies.collectAsState()

    var selectedAllergies by remember { mutableStateOf(setOf<String>()) }

    // Sincronizar cuando cargan los datos
    LaunchedEffect(savedAllergies) {
        selectedAllergies = savedAllergies
    }

    val possibleAllergies = listOf(
        "Gluten" to "en:gluten",
        "Trigo" to "en:wheat",
        "Centeno" to "en:rye",
        "Cebada" to "en:barley",
        "Avena" to "en:oats",

        "Crustáceos" to "en:crustaceans",
        "Huevos" to "en:eggs",
        "Pescado" to "en:fish",
        "Cacahuetes" to "en:peanuts",
        "Soja" to "en:soybeans",

        "Leche" to "en:milk",
        "Frutos secos" to "en:nuts",
        "Apio" to "en:celery",
        "Mostaza" to "en:mustard",
        "Sésamo" to "en:sesame-seeds",

        "Sulfitos" to "en:sulphur-dioxide-and-sulphites",
        "Altramuces" to "en:lupin",
        "Moluscos" to "en:molluscs"
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Alergias") })
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            items(possibleAllergies) { (label, tag) ->

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

            item {
                Spacer(Modifier.height(16.dp))
            }

            item (
                span = { GridItemSpan(2) }
            ){

                Button(
                    onClick = {
                        viewModel.saveAllergies(selectedAllergies)
                        Toast.makeText(context, "¡Datos guardados!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Guardar cambios")
                }
            }
        }
        }
    }

