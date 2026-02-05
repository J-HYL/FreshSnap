package com.marujho.freshsnap.ui.main

import android.net.http.SslCertificate.saveState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.marujho.freshsnap.ui.theme.Green
import com.marujho.freshsnap.ui.theme.Grey
import com.marujho.freshsnap.R

data class ProductUiModel(
    val id: String,
    val name: String,
    val brand: String,
    val imageUrl: String?,
    val expiryDays: Int,
    val expiryDate: String,
    val scannedDate: String,
    val quantity: String,
    val ean: String,
    val nutriScore: String = "A"
)

@Composable
fun MainScreen(
    navController: NavController,
    bottomBarPadding: Dp = 0.dp,
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val products by viewModel.products.collectAsState() // datos de prueba

    val backgroundColor = Color(0xFFF5F5F5)

    Scaffold(
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("scanner_screen") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                containerColor = Green,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = bottomBarPadding)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(16.dp))

            SearchBar()

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = bottomBarPadding + 80.dp)
            ) {
                items(products) { product ->
                    ProductCardItem(product, onNavigateToDetail)
                }
            }
        }
    }
}

@Composable
fun SearchBar() {
    TextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Search") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_camera), contentDescription = null, tint = Color.Gray) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun ProductCardItem(
    product: ProductUiModel,
    onNavigateToDetail: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")

    val statusColor = when {
        product.expiryDays <= 2 -> Color(0xFFFF5252)
        product.expiryDays <= 5 -> Color(0xFFFFC107)
        else -> Color(0xFF69F0AE)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                expanded = !expanded
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(statusColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Imagen del producto (Placeholder)
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.CenterVertically),
                    tint = Color.Gray
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = product.brand,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${product.expiryDays} - days",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            modifier = Modifier.rotate(rotationState)
                        )
                    }
                    Text(
                        text = product.expiryDate,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            TextDetail("Cantidad:", product.quantity)
                            TextDetail("Marca:", product.brand)
                            TextDetail("Escaneado:", product.scannedDate)
                            TextDetail("EAN:", product.ean)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            // aqui poner imagen de nutriscore
                            Text("Green Score: B", fontWeight = FontWeight.Bold) // mirar como devulve green score
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Nutri-Score: ${product.nutriScore}", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onNavigateToDetail(product.ean) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ver más", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TextDetail(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = "$label ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = value, fontSize = 13.sp, color = Color.DarkGray)
    }
}