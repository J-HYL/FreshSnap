package com.marujho.freshsnap.ui.shopping

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marujho.freshsnap.data.model.ShoppingCategory
import com.marujho.freshsnap.data.model.ShoppingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    bottomBarPadding: Dp = 0.dp,
    viewModel: ShoppingViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isShoppingMode by viewModel.isShoppingMode.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (msg != null && msg.startsWith("cleared:")) {
            val count = msg.removePrefix("cleared:").toIntOrNull() ?: 0
            snackbarHostState.showSnackbar("$count items eliminados")
            viewModel.clearSnackbar()
        }
    }

    if (isShoppingMode) {
        ShoppingModeScreen(
            items = items.filter { !it.isChecked },
            stats = stats,
            onItemTap = { viewModel.toggleCheck(it) },
            onExit = { viewModel.setShoppingMode(false) },
            bottomBarPadding = bottomBarPadding
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = bottomBarPadding)
            ) {
                if (stats.pending > 0) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.setShoppingMode(true) },
                        icon = { Icon(Icons.Default.PlayArrow, null) },
                        text = { Text("Empezar compra") },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                }
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header con titulo y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lista de la compra",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row {
                    if (stats.total > 0) {
                        IconButton(onClick = { shareList(context, items) }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Compartir",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (stats.checked > 0) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(
                                Icons.Default.CleaningServices,
                                contentDescription = "Limpiar comprados",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Stats card
            AnimatedVisibility(visible = stats.total > 0) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    StatsCard(stats = stats)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty()) {
                EmptyState()
            } else {
                ShoppingItemsList(
                    items = items,
                    onToggle = { viewModel.toggleCheck(it) },
                    onDelete = { viewModel.deleteItem(it.id) },
                    bottomBarPadding = bottomBarPadding
                )
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, qty ->
                viewModel.addItem(name, qty)
                showAddDialog = false
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            icon = { Icon(Icons.Default.CleaningServices, null) },
            title = { Text("Limpiar comprados") },
            text = { Text("Se eliminarán ${stats.checked} items marcados como comprados. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearCheckedItems()
                    showClearConfirm = false
                }) { Text("Limpiar") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

// region Stats Card

@Composable
private fun StatsCard(stats: ShoppingStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatNumber(value = stats.total, label = "Total")
                StatNumber(
                    value = stats.pending,
                    label = "Pendientes",
                    color = MaterialTheme.colorScheme.primary
                )
                StatNumber(
                    value = stats.checked,
                    label = "Comprados",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { stats.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(stats.progress * 100).toInt()}% completado",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatNumber(
    value: Int,
    label: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// endregion

// region Items list with categories

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShoppingItemsList(
    items: List<ShoppingItem>,
    onToggle: (ShoppingItem) -> Unit,
    onDelete: (ShoppingItem) -> Unit,
    bottomBarPadding: Dp
) {
    val pending = items.filter { !it.isChecked }
    val checked = items.filter { it.isChecked }

    // Agrupa pendientes por categoria respetando el orden definido
    val pendingByCategory = SHOPPING_CATEGORY_ORDER.mapNotNull { cat ->
        val list = pending.filter { ShoppingCategory.fromName(it.category) == cat }
        if (list.isEmpty()) null else cat to list
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = bottomBarPadding + 160.dp)
    ) {
        pendingByCategory.forEach { (category, list) ->
            stickyHeader(key = "header-${category.name}") {
                CategoryHeader(category = category, count = list.size)
            }
            items(list, key = { it.id }) { item ->
                ShoppingItemRow(
                    item = item,
                    onToggle = { onToggle(item) },
                    onDelete = { onDelete(item) }
                )
            }
        }

        if (checked.isNotEmpty()) {
            stickyHeader(key = "header-checked") {
                CheckedHeader(count = checked.size)
            }
            items(checked, key = { it.id }) { item ->
                ShoppingItemRow(
                    item = item,
                    onToggle = { onToggle(item) },
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: ShoppingCategory, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.emoji, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = category.color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "·  $count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CheckedHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Comprados ·  $count",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

// endregion

// region Item row

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onToggle()
                    false // No dismiss, solo toggle
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                else -> false
            }
        },
        positionalThreshold = { it * 0.5f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val (bgColor, icon, alignment) = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Triple(
                    MaterialTheme.colorScheme.tertiary,
                    Icons.Default.Check,
                    Alignment.CenterStart
                )
                SwipeToDismissBoxValue.EndToStart -> Triple(
                    MaterialTheme.colorScheme.error,
                    Icons.Default.Delete,
                    Alignment.CenterEnd
                )
                else -> Triple(Color.Transparent, Icons.Default.Check, Alignment.Center)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                if (bgColor != Color.Transparent) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
            }
        },
        content = {
            ShoppingItemCard(item = item, onToggle = onToggle, onDelete = onDelete)
        }
    )
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val category = ShoppingCategory.fromName(item.category)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra de color de categoria
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (item.source == ShoppingItem.SOURCE_RECIPE) 64.dp else 56.dp)
                    .background(category.color)
            )
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() }
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (item.isChecked)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (item.quantity.isNotBlank()) {
                        Text(
                            text = item.quantity,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                if (item.source == ShoppingItem.SOURCE_RECIPE) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "De receta",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// endregion

// region Add dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.ShoppingCart, null) },
        title = { Text("Añadir producto") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Producto") },
                    placeholder = { Text("Ej. Leche entera") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad (opcional)") },
                    placeholder = { Text("Ej. 1L, 200g, 2 unidades") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, quantity) },
                enabled = name.isNotBlank()
            ) { Text("Añadir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// endregion

// region Empty state

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tu lista está vacía",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pulsa + para añadir productos o usa las recetas para añadir ingredientes que faltan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// endregion

// region Shopping mode (full-screen)

@Composable
private fun ShoppingModeScreen(
    items: List<ShoppingItem>,
    stats: ShoppingStats,
    onItemTap: (ShoppingItem) -> Unit,
    onExit: () -> Unit,
    bottomBarPadding: Dp
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onExit) {
                        Icon(Icons.Default.Close, contentDescription = "Salir")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🛒 Modo compra",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${stats.checked}/${stats.total} comprados",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🎉", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "¡Compra completada!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Has comprado todos los items de tu lista.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onExit) { Text("Salir") }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = bottomBarPadding + 16.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    BigShoppingItem(item = item, onTap = { onItemTap(item) })
                }
            }
        }
    }
}

@Composable
private fun BigShoppingItem(item: ShoppingItem, onTap: () -> Unit) {
    val category = ShoppingCategory.fromName(item.category)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        onClick = onTap
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = category.emoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.quantity.isNotBlank()) {
                    Text(
                        text = item.quantity,
                        style = MaterialTheme.typography.titleMedium,
                        color = category.color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Marcar comprado",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

// endregion

// region Share

private fun shareList(context: android.content.Context, items: List<ShoppingItem>) {
    val pending = items.filter { !it.isChecked }
    val checked = items.filter { it.isChecked }

    val text = buildString {
        appendLine("🛒 Lista de la compra (FreshSnap)")
        appendLine()

        // Agrupar pendientes por categoria
        SHOPPING_CATEGORY_ORDER.forEach { category ->
            val list = pending.filter { ShoppingCategory.fromName(it.category) == category }
            if (list.isNotEmpty()) {
                appendLine("${category.emoji} ${category.displayName}")
                list.forEach { item ->
                    val qty = if (item.quantity.isNotBlank()) " (${item.quantity})" else ""
                    appendLine("  □ ${item.name}$qty")
                }
                appendLine()
            }
        }

        if (checked.isNotEmpty()) {
            appendLine("✓ Ya comprado")
            checked.forEach { item ->
                val qty = if (item.quantity.isNotBlank()) " (${item.quantity})" else ""
                appendLine("  ✓ ${item.name}$qty")
            }
        }
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Compartir lista"))
}

// endregion
