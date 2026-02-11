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
import coil.compose.AsyncImage
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
    val nutriScore: String = "A",
    val greenScore: String = "?"
)

@Composable
fun MainScreen(
    navController: NavController,
    bottomBarPadding: Dp = 0.dp,
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val products by viewModel.products.collectAsState() // datos de prueba

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,

            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        singleLine = true
    )
}

@Composable
fun ProductCardItem(
    product: ProductUiModel,
    onNavigateToDetail: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .padding(vertical = 8.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = "Foto producto",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_freshsnap_logo),
                        error = painterResource(android.R.drawable.ic_menu_gallery)
                    )
                }

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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = product.brand,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            modifier = Modifier.rotate(rotationState),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = product.expiryDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            TextDetail("Cantidad:", product.quantity)
                            TextDetail("Marca:", product.brand)
                            TextDetail("Escaneado:", product.scannedDate)
                            TextDetail("EAN:", product.ean)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            // NutriScore Image
                            val nutriRes = getNutriScoreIcon(product.nutriScore)
                            if (nutriRes != null) {
                                Image(
                                    painter = painterResource(id = nutriRes),
                                    contentDescription = "NutriScore ${product.nutriScore}",
                                    modifier = Modifier.height(32.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    "Nutri-Score: ${product.nutriScore}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // EcoScore Image
                            val ecoRes = getEcoScoreIcon(product.greenScore)
                            if (ecoRes != null) {
                                Image(
                                    painter = painterResource(id = ecoRes),
                                    contentDescription = "EcoScore ${product.greenScore}",
                                    modifier = Modifier.height(32.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    "Eco-Score: ${product.greenScore}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onNavigateToDetail(product.ean) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
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
        Text(
            text = "$label ",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

fun getNutriScoreIcon(score: String?): Int? {
    return when (score?.lowercase()) {
        "a" -> R.drawable.ic_nutriscore_a
        "b" -> R.drawable.ic_nutriscore_b
        "c" -> R.drawable.ic_nutriscore_c
        "d" -> R.drawable.ic_nutriscore_d
        "e" -> R.drawable.ic_nutriscore_e
        else -> null
    }
}

fun getEcoScoreIcon(score: String?): Int? {
    return when (score?.lowercase()) {
        "a" -> R.drawable.ic_ecoscore_a
        "b" -> R.drawable.ic_ecoscore_b
        "c" -> R.drawable.ic_ecoscore_c
        "d" -> R.drawable.ic_ecoscore_d
        "e" -> R.drawable.ic_ecoscore_e
        "f" -> R.drawable.ic_ecoscore_f
        else -> null
    }
}