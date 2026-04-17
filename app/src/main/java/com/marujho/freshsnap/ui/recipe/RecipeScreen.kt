package com.marujho.freshsnap.ui.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    bottomBarPadding: Dp = 0.dp,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val addedText = stringResource(R.string.recipe_added_to_list)

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(addedText)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recipe_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (uiState is RecipeUiState.Success || uiState is RecipeUiState.Error) {
                    IconButton(onClick = { viewModel.loadRecipes(forceRefresh = true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.recipe_regenerate),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is RecipeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.recipe_loading),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is RecipeUiState.EmptyNoRed -> {
                    EmptyStateContent(
                        icon = Icons.Default.CheckCircle,
                        message = stringResource(R.string.recipe_empty_no_red),
                        iconTint = Green
                    )
                }

                is RecipeUiState.EmptyNoProducts -> {
                    EmptyStateContent(
                        icon = Icons.Default.RestaurantMenu,
                        message = stringResource(R.string.recipe_empty_no_products),
                        iconTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }

                is RecipeUiState.Error -> {
                    EmptyStateContent(
                        icon = Icons.Default.ErrorOutline,
                        message = stringResource(R.string.recipe_error),
                        iconTint = SoftRed
                    )
                }

                is RecipeUiState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = bottomBarPadding + 16.dp)
                    ) {
                        items(state.recipes, key = { it.id.ifBlank { it.title } }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
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

@Composable
private fun EmptyStateContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    iconTint: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecipeCard(
    recipe: CachedRecipe,
    onAddMissingToList: (List<RecipeIngredient>) -> Unit
) {
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
            // Header con imagen
            if (recipe.imageUrl != null) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Titulo y source chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SourceChip(source = recipe.source)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Resumen de ingredientes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Green,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${ownedIngredients.size} tienes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (missingIngredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = SoftRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${missingIngredients.size} faltan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Contenido expandible
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Instrucciones
                    if (recipe.instructions.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.recipe_instructions),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recipe.instructions,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Ingredientes que tienes
                    if (ownedIngredients.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.recipe_ingredients_owned),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Green
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ownedIngredients.forEach { ingredient ->
                            IngredientRow(
                                ingredient = ingredient,
                                checked = true
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Ingredientes que faltan
                    if (missingIngredients.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.recipe_ingredients_missing),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SoftRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        missingIngredients.forEach { ingredient ->
                            IngredientRow(
                                ingredient = ingredient,
                                checked = false
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Boton para agregar a lista de compras
                        Button(
                            onClick = { onAddMissingToList(missingIngredients) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.recipe_add_missing_to_list))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SourceChip(source: String) {
    val (text, color) = when (source) {
        RecipeSource.MEALDB.name -> stringResource(R.string.recipe_source_mealdb) to MaterialTheme.colorScheme.primary
        RecipeSource.GROQ_FILTERED.name -> stringResource(R.string.recipe_source_groq_filter) to MaterialTheme.colorScheme.tertiary
        RecipeSource.GROQ_GENERATED.name -> stringResource(R.string.recipe_source_groq_generated) to MaterialTheme.colorScheme.secondary
        else -> source to MaterialTheme.colorScheme.outline
    }

    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.12f),
            labelColor = color
        ),
        border = null
    )
}

@Composable
private fun IngredientRow(
    ingredient: RecipeIngredient,
    checked: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = Green,
                uncheckedColor = SoftRed
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = ingredient.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (ingredient.measure.isNotBlank()) {
            Text(
                text = ingredient.measure,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
