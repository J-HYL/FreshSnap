package com.marujho.freshsnap.ui.detail

import android.content.ClipData
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.R
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.marujho.freshsnap.data.remote.dto.ProductDto

@Composable
fun detailScreen(
    viewModel: DetailViewModel = hiltViewModel()
) {

    val state = viewModel.uiState

    when(state) {
        is DetailUiState.Loading -> {}//Imagen cargando
        is DetailUiState.Error -> {}//Imagen Error
        is DetailUiState.Success -> {
            val product = state.product
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    detailImage(product.imageUrl ?: "")
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
    }




}

@Composable
fun detailImage(url: String) { //Conectar para tener la url
    Card(
        modifier = Modifier
            .padding (16.dp)
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
fun detectNullText(label : String, text : String?,padd : Int,style : TextStyle = MaterialTheme.typography.bodyMedium){
    if (!text.isNullOrBlank()){
        Text(
            text = "$label: $text",
            style = style,
            modifier = Modifier.padding(bottom = padd.dp)
        )

    }
}
@Composable
fun detailGeneralInformation(product : ProductDto) { //Connectar con viewmodel para conseguir los datos
    val paddingMod : Int = 4
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ){
            Text(
                text = "INFORMACION GENERAL",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            detectNullText("Nombre",product.productName,paddingMod)
            detectNullText("Fecha Escaner","",paddingMod) //Añadir fecha actual
            detectNullText("Cantidad",product.quantity,paddingMod)
            detectNullText("Denominacion general",product.categories,paddingMod)
            detectNullText("Envase",product.packaging,paddingMod)
            detectNullText("Tienda",product.quantity,paddingMod) //Falta añadir la variable de tiendas
            detectNullText("Paises de venta",product.countries,paddingMod)
        }
    }
}

@Composable
fun detailHealth(product : ProductDto){
    val paddingMod : Int = 4
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ){
            Text(
                text = "SALUD",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )

            Text(
                text = "NutriScore, Cambiar a imagen",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            Text(
                text = "Grasas",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            Text(
                text = "Grasas saturadas",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            Text(
                text = "Azucares",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            Text(
                text = "Sal",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
        }
    }
}
@Composable
fun detailNutriments(product : ProductDto){
    val paddingMod : Int = 4
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ){
            Text(
                text = "VALOR NUTRICIONAL",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            Text(
                text = "Nombre",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            Text(
                text = "Fecha Escaner",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            Text(
                text = "Cantidad",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            Text(
                text = "Denominacion general",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            Text(
                text = "Envase",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            Text(
                text = "Tienda",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )
            Text(
                text = "Paises de venta",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = paddingMod.dp)
            )

        }
    }

}
@Preview
@Composable
fun detailScreenPreview() {
    detailScreen()
}