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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marujho.freshsnap.R


@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAllergyScreen(
    viewModel: SettingsAllergyViewModel = viewModel()
) {
    val context = LocalContext.current
    val savedAllergies by viewModel.userAllergies.collectAsStateWithLifecycle()

    var selectedAllergies by remember { mutableStateOf(setOf<String>()) }

    // Sincronizar cuando cargan los datos
    LaunchedEffect(savedAllergies) {
        selectedAllergies = savedAllergies
    }

    val possibleAllergies = listOf(
        stringResource(R.string.allergy_gluten) to "en:gluten",
        stringResource(R.string.allergy_wheat) to "en:wheat",
        stringResource(R.string.allergy_rye) to "en:rye",
        stringResource(R.string.allergy_barley) to "en:barley",
        stringResource(R.string.allergy_oats) to "en:oats",
        stringResource(R.string.allergy_crustaceans) to "en:crustaceans",
        stringResource(R.string.allergy_eggs) to "en:eggs",
        stringResource(R.string.allergy_fish) to "en:fish",
        stringResource(R.string.allergy_peanuts) to "en:peanuts",
        stringResource(R.string.allergy_soybeans) to "en:soybeans",
        stringResource(R.string.allergy_milk) to "en:milk",
        stringResource(R.string.allergy_nuts) to "en:nuts",
        stringResource(R.string.allergy_celery) to "en:celery",
        stringResource(R.string.allergy_mustard) to "en:mustard",
        stringResource(R.string.allergy_sesame) to "en:sesame-seeds",
        stringResource(R.string.allergy_sulphites) to "en:sulphur-dioxide-and-sulphites",
        stringResource(R.string.allergy_lupin) to "en:lupin",
        stringResource(R.string.allergy_molluscs) to "en:molluscs"
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
                        Toast.makeText(context, context.getString(R.string.data_saved), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(stringResource(R.string.save_changes))
                }
            }
        }
        }
    }

