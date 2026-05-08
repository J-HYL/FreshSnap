package com.marujho.freshsnap.ui.shopping

import androidx.compose.ui.graphics.Color
import com.marujho.freshsnap.data.model.ShoppingCategory

/**
 * Mapper UI para las categorias: color, emoji y label legible.
 * Vive en la capa UI para no contaminar data/model con dependencias de Compose.
 */
val ShoppingCategory.color: Color
    get() = when (this) {
        ShoppingCategory.DAIRY -> Color(0xFF64B5F6)        // azul
        ShoppingCategory.VEGETABLES -> Color(0xFF81C784)   // verde
        ShoppingCategory.FRUITS -> Color(0xFFFFB74D)       // naranja claro
        ShoppingCategory.MEAT -> Color(0xFFE57373)         // rojo
        ShoppingCategory.BAKERY -> Color(0xFFA1887F)       // marron
        ShoppingCategory.PANTRY -> Color(0xFFBA68C8)       // morado
        ShoppingCategory.DRINKS -> Color(0xFF4DD0E1)       // cian
        ShoppingCategory.FROZEN -> Color(0xFF7986CB)       // indigo
        ShoppingCategory.CLEANING -> Color(0xFF90A4AE)     // gris azulado
        ShoppingCategory.OTHER -> Color(0xFF9E9E9E)        // gris
    }

val ShoppingCategory.emoji: String
    get() = when (this) {
        ShoppingCategory.DAIRY -> "🥛"
        ShoppingCategory.VEGETABLES -> "🥬"
        ShoppingCategory.FRUITS -> "🍎"
        ShoppingCategory.MEAT -> "🥩"
        ShoppingCategory.BAKERY -> "🥖"
        ShoppingCategory.PANTRY -> "🥫"
        ShoppingCategory.DRINKS -> "🥤"
        ShoppingCategory.FROZEN -> "🧊"
        ShoppingCategory.CLEANING -> "🧴"
        ShoppingCategory.OTHER -> "📦"
    }

val ShoppingCategory.displayName: String
    get() = when (this) {
        ShoppingCategory.DAIRY -> "Lácteos"
        ShoppingCategory.VEGETABLES -> "Verduras"
        ShoppingCategory.FRUITS -> "Frutas"
        ShoppingCategory.MEAT -> "Carne y pescado"
        ShoppingCategory.BAKERY -> "Panadería"
        ShoppingCategory.PANTRY -> "Despensa"
        ShoppingCategory.DRINKS -> "Bebidas"
        ShoppingCategory.FROZEN -> "Congelados"
        ShoppingCategory.CLEANING -> "Limpieza"
        ShoppingCategory.OTHER -> "Otros"
    }

/** Orden fijo para mostrar las categorias en la lista (lacteos primero, otros al final). */
val SHOPPING_CATEGORY_ORDER: List<ShoppingCategory> = listOf(
    ShoppingCategory.FRUITS,
    ShoppingCategory.VEGETABLES,
    ShoppingCategory.MEAT,
    ShoppingCategory.DAIRY,
    ShoppingCategory.BAKERY,
    ShoppingCategory.PANTRY,
    ShoppingCategory.FROZEN,
    ShoppingCategory.DRINKS,
    ShoppingCategory.CLEANING,
    ShoppingCategory.OTHER
)
