package com.marujho.freshsnap.ui.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.marujho.freshsnap.R
import com.marujho.freshsnap.data.model.CachedRecipe
import com.marujho.freshsnap.data.model.RecipeIngredient
import com.marujho.freshsnap.data.model.RecipeSource
import com.marujho.freshsnap.ui.theme.Green
import com.marujho.freshsnap.ui.theme.SoftRed

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeScreen(
    bottomBarPadding: Dp = 0.dp,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage!!)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.recipe_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Selecciona qué ingredientes quieres usar hoy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is RecipeUiState.LoadingInventory -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is RecipeUiState.Ready -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = bottomBarPadding + 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // Selector de ingredientes
                        item {
                            if (state.ingredients.isEmpty()) {
                                EmptyStateContent(
                                    icon = Icons.Default.Kitchen,
                                    message = "Tu despensa está vacía. Añade productos para generar recetas.",
                                    iconTint = MaterialTheme.colorScheme.outline
                                )
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.ingredients.forEach { ingredient ->
                                        FilterChip(
                                            selected = ingredient.isSelected,
                                            onClick = { viewModel.toggleIngredient(ingredient.name) },
                                            label = { Text(ingredient.name) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // otón de Generar
                        if (state.ingredients.isNotEmpty()) {
                            item {
                                Button(
                                    onClick = { viewModel.generateRecipe() },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    enabled = !state.isGenerating && state.ingredients.any { it.isSelected },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (state.isGenerating) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Pensando receta...")
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Generar Receta con IA")
                                    }
                                }
                            }
                        }

                        // Errores o Mensajes de la IA (Ej. Combinaciones absurdas)
                        if (state.errorMessage != null) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = state.errorMessage,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }

                        // Mostrar Receta Generada
                        if (state.recipe != null) {
                            item {
                                RecipeCard(
                                    recipe = state.recipe,
                                    onAddMissingToList = { ingredients ->
                                        viewModel.addMissingToShoppingList(ingredients)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    iconTint: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = iconTint)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeCard(recipe: CachedRecipe, onAddMissingToList: (List<RecipeIngredient>) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val ownedIngredients = recipe.toRecipeIngredients(recipe.ingredientsOwned)
    val missingIngredients = recipe.toRecipeIngredients(recipe.ingredientsMissing)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = { expanded = !expanded }
    ) {
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = recipe.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${ownedIngredients.size} tienes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (missingIngredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = SoftRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${missingIngredients.size} faltan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    if (recipe.instructions.isNotBlank()) {
                        Text(text = "Instrucciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = recipe.instructions, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if (ownedIngredients.isNotEmpty()) {
                        Text(text = "En tu despensa", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Green)
                        ownedIngredients.forEach { IngredientRow(it, true) }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (missingIngredients.isNotEmpty()) {
                        Text(text = "Te falta comprar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SoftRed)
                        missingIngredients.forEach { IngredientRow(it, false) }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onAddMissingToList(missingIngredients) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Añadir faltantes a la lista")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(ingredient: RecipeIngredient, checked: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.size(24.dp), colors = CheckboxDefaults.colors(checkedColor = Green, uncheckedColor = SoftRed))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = ingredient.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (ingredient.measure.isNotBlank()) {
            Text(text = ingredient.measure, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}