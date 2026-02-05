package com.marujho.freshsnap.ui.detail

import android.content.ClipData
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.marujho.freshsnap.data.remote.dto.NutrientLevel
import com.marujho.freshsnap.data.remote.dto.ProductDto
import com.marujho.freshsnap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun detailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onNavigateMain: () -> Unit,
    onNavigationToScanDate: () -> Unit ={}
) {

    val state = viewModel.uiState

    when (state) {
        is DetailUiState.Loading -> {}//Imagen cargando
        is DetailUiState.Error -> {}//Imagen Error
        is DetailUiState.Success -> {
            val product = state.product

            var showSelectDialog by remember { mutableStateOf(false) }
            var showDatePick by remember { mutableStateOf(false) }
            var datePickerState = rememberDatePickerState()


            Scaffold(
                bottomBar = {
                    detailBottomBar(
                        onCancel = { onNavigateMain() },
                        onConfirm = { showSelectDialog = true}
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray)
                        .padding(innerPadding),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        detailImage(product.imageUrl ?: "")
                    }
                    if (viewModel.expirationDate != null){
                        item {
                            Text("Caducidad ${viewModel.expirationDate}")
                        }
                    }
                    item {
                        detailGeneralInformation(product = product)
                    }
                    item {
                        detailHealth(product = product)
                    }
                    item {
                        detailNutriments(product = product)
                    }
                }
            }
            if (showSelectDialog) {
                AlertDialog( //Dialog para añadir fecha de caducidad
                    onDismissRequest = {showSelectDialog = false},
                    title = { Text("Añadir fecha de caducidad") },
                    text = { Text("¿Como quieres añadirla?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSelectDialog = false
                                val currentBarCode = product.code ?: ""
                                onNavigationToScanDate()
                            }
                        ) { Text(text = "Escanear")}
                    },
                    dismissButton = { //Boton para añadir manualmente

                        Button(onClick = {
                            showSelectDialog = false
                            showDatePick = true
                        }) { Text(text = "Manual")}
                    }
                )
            }
            if (showDatePick){ //Dialog para el calendario
                DatePickerDialog(
                    onDismissRequest = {showDatePick = false},
                    confirmButton = {
                        Button( onClick = {
                            showDatePick = false
                            viewModel.setExpirationDatefromCal(datePickerState.selectedDateMillis)
                        }) { Text("Guardar") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }


}

@Composable
fun detailImage(url: String) { //Conectar para tener la url
    Card(
        modifier = Modifier
            .padding(16.dp)
    ) {
        AsyncImage(
            model = url,
            contentDescription = "Imagen del producto",
            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
            error = painterResource(android.R.drawable.ic_dialog_alert),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
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
            modifier = Modifier.padding(bottom = padd.dp)
        )

    }
}

@Composable
fun detailGeneralInformation(product: ProductDto) { //Connectar con viewmodel para conseguir los datos
    val paddingMod: Int = 4
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "INFORMACION GENERAL",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            detectNullText("Nombre", product.productName, paddingMod)
            detectNullText("Fecha Escaner", "", paddingMod) //Añadir fecha actual
            detectNullText("Cantidad", product.quantity, paddingMod)
            detectNullText("Denominacion general", product.categories, paddingMod)
            detectNullText("Envase", product.packaging, paddingMod)
            detectNullText(
                "Tienda",
                product.quantity,
                paddingMod
            ) //Falta añadir la variable de tiendas
            detectNullText("Paises de venta", product.countries, paddingMod)
        }
    }
}

@Composable
fun detailHealth(product: ProductDto) {
    val paddingMod: Int = 4
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "SALUD",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )

            if (product.nutriScore != null){
                Image(
                    painter = painterResource(id = getNutriScoreResource(product.nutriScore)!!),
                    contentDescription = "NutriScore",
                    modifier = Modifier.size(100.dp),
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

fun getNutriScoreResource(nutriScore: String?): Int?{
    return when(nutriScore?.lowercase()){
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
        var color: Color
        var text: String
        when (level) {
            NutrientLevel.LOW -> {
                color = Color.Green
                text = "Bajo en $label"
            }

            NutrientLevel.MODERATE -> {
                color = Color.Yellow
                text = "Moderado en $label"
            }

            NutrientLevel.HIGH -> {
                color = Color.Red
                text = "Alto en $label"
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
} //Cambiar Colores para que se vean mejor

@Composable
fun detailNutriments(product: ProductDto) {
    val paddingMod: Int = 4
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "VALOR NUTRICIONAL",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            detailRowNutriments("Energia Kcal", product.nutriments?.energyKcal100g)
            detailRowNutriments("Energia Kj", product.nutriments?.energyKj100g)
            detailRowNutriments("Grasas", product.nutriments?.fat100g)
            detailRowNutriments("Grasas saturadas", product.nutriments?.saturatedFat100g)
            detailRowNutriments("Carbohidratos", product.nutriments?.carbohydrates100g)
            detailRowNutriments("Azúcares", product.nutriments?.sugars100g)
            detailRowNutriments("Proteínas", product.nutriments?.proteins100g)
            detailRowNutriments("Sal", product.nutriments?.salt100g)
            detailRowNutriments("Fibra", product.nutriments?.fiber100g)
            detailRowNutriments("Sodio", product.nutriments?.sodium100g)
        }
    }

}

@Composable
fun detailRowNutriments(label: String, data: Double?) {
    if (data != null) {
        Row() {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = data.toString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun detailBottomBar(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "Cancelar")
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "Confirmar")
        }
    }
}

/*@Preview
@Composable
fun detailScreenPreview() {
    detailScreen(onNavigateMain = {})
}*/