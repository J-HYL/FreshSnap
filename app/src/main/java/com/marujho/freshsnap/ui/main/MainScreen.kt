package com.marujho.freshsnap.ui.main

import android.R.id.tabs
import android.net.http.SslCertificate.saveState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.stringResource
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
import com.marujho.freshsnap.ui.theme.SoftRed
import com.marujho.freshsnap.ui.theme.Yellow

data class ProductUiModel(
    val id: String,
    val name: String,
    val brand: String,
    val imageUrl: String?,
    val expiryDays: Int,
    val expiryDate: String,
    val expirationTimestamp: Long,
    val scannedDate: String,
    val scannedTimestamp: Long,
    val quantity: String,
    val ean: String,
    val nutriScore: String = "A",
    val greenScore: String = "?",
    val isConsumed: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    bottomBarPadding: Dp = 0.dp,
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val products by viewModel.products.collectAsState() // datos de prueba
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab = viewModel.selectedTab
    val tabs = listOf(
        stringResource(R.string.tab_pantry),
        stringResource(R.string.tab_expired),
        stringResource(R.string.tab_consumed)
    )
    val redDays by viewModel.redDays.collectAsState()
    val yellowDays by viewModel.yellowDays.collectAsState()

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
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_button_desc))
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

            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    CategoryButton(
                        text = title,
                        isSelected = selectedTab == index,
                        onClick = { viewModel.onTabSelected(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = bottomBarPadding + 80.dp)
            ) {
                items(
                    items = products,
                    key = { product -> "${product.id}_$selectedTab" }
                ) { product ->

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            when (dismissValue) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    viewModel.consumeProduct(product.id)
                                    true
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    viewModel.deleteProduct(product.id)
                                    true
                                }
                                else -> false
                            }
                        },
                        positionalThreshold = { totalDistance -> totalDistance * 0.12f }
                    )

                    LaunchedEffect(selectedTab) {
                        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = selectedTab == 0,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            val color = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                                SwipeToDismissBoxValue.EndToStart -> Color(0xFFE57373)
                                else -> Color.Transparent
                            }

                            val icon = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                else -> null
                            }

                            val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                                Alignment.CenterStart else Alignment.CenterEnd

                            if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = alignment
                                ) {
                                    if (icon != null) {
                                        Icon(icon, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        },
                        content = {
                            ProductCardItem(
                                product = product,
                                selectedTab = selectedTab,
                                redDays = redDays,
                                yellowDays = yellowDays,
                                onNavigateToDetail = onNavigateToDetail
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
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
    selectedTab: Int,
    redDays: Int,
    yellowDays: Int,
    onNavigateToDetail: (String) -> Unit

) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    val statusColor = when {
        selectedTab == 2 -> Color(0xFF42A5F5) // azul consumidos
        product.expiryDays <= redDays -> SoftRed
        product.expiryDays <= yellowDays -> Yellow
        else -> Green
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
                        contentDescription = stringResource(R.string.product_image_desc),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_freshsnap_logo_splash),
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
                        if (selectedTab == 0) {
                            Text(
                                text = stringResource(R.string.days_remaining, product.expiryDays + 1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else if (selectedTab == 1) {
                            Text(
                                text = stringResource(R.string.status_expired),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else if (selectedTab == 2) {
                            Text(
                                text = stringResource(R.string.status_consumed),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.expand_desc),
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
                            TextDetail(stringResource(R.string.quantity_label), product.quantity)
                            TextDetail(stringResource(R.string.brand_label), product.brand)
                            TextDetail(stringResource(R.string.scanned_label), product.scannedDate)
                            TextDetail(stringResource(R.string.ean_label), product.ean)
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
                        onClick = { onNavigateToDetail("${product.ean}?productId=${product.id}") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.see_more_button), color = Color.White)
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