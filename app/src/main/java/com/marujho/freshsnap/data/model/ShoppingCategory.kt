package com.marujho.freshsnap.data.model

/**
 * Categorias de productos para la lista de la compra.
 * Se almacena el name() del enum como String en Firestore.
 * Los colores y emojis se aplican en la capa UI (ver ui/shopping/CategoryUi.kt).
 */
enum class ShoppingCategory {
    DAIRY,
    VEGETABLES,
    FRUITS,
    MEAT,
    BAKERY,
    PANTRY,
    DRINKS,
    FROZEN,
    CLEANING,
    OTHER;

    companion object {
        /** Lookup tolerante: si el string no coincide, devuelve OTHER. */
        fun fromName(name: String?): ShoppingCategory =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
