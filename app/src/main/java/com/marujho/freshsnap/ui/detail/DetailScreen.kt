package com.marujho.freshsnap.ui.detail

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.marujho.freshsnap.R
import com.marujho.freshsnap.data.remote.dto.NutrientLevel
import com.marujho.freshsnap.data.remote.dto.ProductDto
import com.marujho.freshsnap.ui.theme.Green
import com.marujho.freshsnap.ui.theme.SoftRed
import com.marujho.freshsnap.ui.theme.Yellow
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun detailScreen(
    viewModel: DetailViewModel,
    onNavigateMain: () -> Unit,
    onNavigationToScanDate: () -> Unit = {}
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    val allergyMatches by viewModel.allergyMatches.collectAsState()

    fun onConfirmPressed() {
        viewModel.saveProduct(
            onSuccess = {
                Toast.makeText(context, context.getString(R.string.product_saved), Toast.LENGTH_SHORT).show()
                onNavigateMain()
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        )
    }

    when (state) {
        is DetailUiState.Loading -> {} // Cargando
        is DetailUiState.Error -> {} // Error
        is DetailUiState.Success -> {
            val product = state.product

            var showSelectDialog by remember { mutableStateOf(false) }
            var showDatePick by remember { mutableStateOf(false) }
            var datePickerState = rememberDatePickerState()

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    detailBottomBar(
                        onCancel = { onNavigateMain() },
                        onConfirm = { onConfirmPressed() },
                        onDate = { showSelectDialog = true }
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { detailImage(product.imageUrl ?: "", hasWarning = allergyMatches.isNotEmpty()) }
                    if (viewModel.expirationDate != null) {
                        item { Text(stringResource(R.string.expiry_date_value, viewModel.expirationDate ?: "")) }
                    }
                    item { detailGeneralInformation(product = product) }
                    item { detailHealth(product = product) }
                    item { detailNutriments(product = product) }
                    item { detailAllergies(product.allergensTags, allergyMatches) }
                }
            }

            if (showSelectDialog) {
                AlertDialog(
                    onDismissRequest = { showSelectDialog = false },
                    title = { Text(stringResource(R.string.add_expiry_date_title)) },
                    text = { Text(stringResource(R.string.add_expiry_date_text)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showSelectDialog = false
                                onNavigationToScanDate()
                            }
                        ) { Text(stringResource(R.string.scan_button)) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showSelectDialog = false
                            showDatePick = true
                        }) { Text(stringResource(R.string.manual_button)) }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    textContentColor = MaterialTheme.colorScheme.onSurface
                )
            }

            if (showDatePick) {
                DatePickerDialog(
                    onDismissRequest = { showDatePick = false },
                    confirmButton = {
                        Button(onClick = {
                            showDatePick = false
                            viewModel.setExpirationDateFromMillis(datePickerState.selectedDateMillis ?: return@Button)
                        }) { Text(stringResource(R.string.save)) }
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    )
                ) { DatePicker(state = datePickerState) }
            }
        }
    }
}


@Composable
fun detailImage(url: String, hasWarning: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            AsyncImage(
                model = url,
                contentDescription = stringResource(R.string.detail_image_desc),
                placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                error = painterResource(android.R.drawable.ic_dialog_alert),
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )

            if (hasWarning) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alerta de alergia",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.TopEnd)
                        .size(36.dp)
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun detectNullText(
    label: String,
    text: String?,
    padd: Int,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    if (!text.isNullOrBlank()) {
        Text(
            text = "$label: $text",
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = padd.dp)
        )
    }
}

@Composable
fun detailGeneralInformation(product: ProductDto) {
    val paddingMod = 4
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.general_info_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            detectNullText(stringResource(R.string.general_name), product.productName, paddingMod)
            detectNullText(stringResource(R.string.general_scan_date), "", paddingMod)
            detectNullText(stringResource(R.string.general_quantity), product.quantity, paddingMod)
            detectNullText(stringResource(R.string.general_denomination), product.categories, paddingMod)
            detectNullText(stringResource(R.string.general_packaging), product.packaging, paddingMod)
            detectNullText(stringResource(R.string.general_store), product.brands, paddingMod)
            detectNullText(stringResource(R.string.general_countries), product.countries, paddingMod)
        }
    }
}

@Composable
fun detailHealth(product: ProductDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.health_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            val nutriScoreResource = getNutriScoreResource(product.nutriScore)
            if (nutriScoreResource != null) {
                Image(
                    painter = painterResource(id = nutriScoreResource),
                    contentDescription = stringResource(R.string.nutriscore_desc),
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Fit
                )
            }
            detailNutrimentsLevels("Grasas", product.nutrimentsLevels?.fat)
            detailNutrimentsLevels("Grasas saturadas", product.nutrimentsLevels?.saturatedFat)
            detailNutrimentsLevels("Azucares", product.nutrimentsLevels?.sugars)
            detailNutrimentsLevels("Sal", product.nutrimentsLevels?.salt)
        }
    }
}

fun getNutriScoreResource(nutriScore: String?): Int? {
    return when (nutriScore?.lowercase()) {
        "a" -> R.drawable.ic_nutriscore_a
        "b" -> R.drawable.ic_nutriscore_b
        "c" -> R.drawable.ic_nutriscore_c
        "d" -> R.drawable.ic_nutriscore_d
        "e" -> R.drawable.ic_nutriscore_e
        else -> null
    }
}

@Composable
fun detailNutrimentsLevels(label: String, level: NutrientLevel?) {
    if (level != null) {
        val (color, textPrefix) = when (level) {
            NutrientLevel.LOW -> Pair(Green, stringResource(R.string.low_in))
            NutrientLevel.MODERATE -> Pair(Yellow, stringResource(R.string.moderate_in))
            NutrientLevel.HIGH -> Pair(SoftRed, stringResource(R.string.high_in))
        }
        Row(modifier = Modifier.padding(vertical = 2.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, shape = CircleShape)
                    .align(androidx.compose.ui.Alignment.CenterVertically)
            )
            Text(
                text = "  $textPrefix $label",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun detailNutriments(product: ProductDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.nutritional_value_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            detailRowNutriments(stringResource(R.string.nutriment_energy), product.nutriments?.energyKcal100g, "Kcal")
            detailRowNutriments(stringResource(R.string.nutriment_fats), product.nutriments?.fat100g, "g")
            detailRowNutriments(stringResource(R.string.nutriment_sat_fats), product.nutriments?.saturatedFat100g, "g")
            detailRowNutriments(stringResource(R.string.nutriment_carbs), product.nutriments?.carbohydrates100g, "g")
            detailRowNutriments(stringResource(R.string.nutriment_sugars), product.nutriments?.sugars100g, "g")
            detailRowNutriments(stringResource(R.string.nutriment_proteins), product.nutriments?.proteins100g, "g")
            detailRowNutriments(stringResource(R.string.nutriment_salt), product.nutriments?.salt100g, "g")
            detailRowNutriments(stringResource(R.string.nutriment_sodium), product.nutriments?.sodium100g, "g")
            detailRowNutriments(stringResource(R.string.nutriment_fiber), product.nutriments?.fiber100g, "g")
        }
    }
}

@Composable
fun detailRowNutriments(label: String, data: Double?, unit: String) {
    if (data != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$data $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun detailBottomBar(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDate: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) { Text(stringResource(R.string.cancel)) }

        OutlinedButton(
            onClick = onDate,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) { Text(stringResource(R.string.expiry_date_label)) }

        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) { Text(stringResource(R.string.save)) }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun detailAllergies(allergensTags: List<String>?, allergyMatches: List<String>) {
    if (!allergensTags.isNullOrEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.allergies_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (allergyMatches.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peligro de alergia",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    allergensTags.forEach { tag ->
                        val isMatch = allergyMatches.contains(tag)
                        val cleanTag = tag.substringAfter("en:").replaceFirstChar { it.uppercase() }

                        val containerColor = if (isMatch) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                        val contentColor = if (isMatch) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .background(containerColor, shape = RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cleanTag,
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor,
                                fontWeight = if (isMatch) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}