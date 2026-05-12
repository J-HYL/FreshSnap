package com.marujho.freshsnap.data.domain

import com.marujho.freshsnap.data.model.IngredientClassification
import com.marujho.freshsnap.data.model.UserProduct
import java.security.MessageDigest
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class IngredientMasticator @Inject constructor() {

    /**
     * Clasifica los productos del inventario por su estado de frescura.
     * Reutiliza la misma logica de MainViewModel (daysRemaining vs umbrales).
     * Solo incluye productos no consumidos. Los productos ya caducados se
     * tratan como ROJOS (urgentes) para que tambien se sugieran recetas.
     */
    fun classify(
        products: List<UserProduct>,
        redDays: Int,
        yellowDays: Int
    ): IngredientClassification {
        // Normalizar a inicio del dia para que un producto que caduca HOY
        // siga contando como valido (de lo contrario quedaria fuera por hora).
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val red = mutableListOf<String>()
        val yellow = mutableListOf<String>()
        val green = mutableListOf<String>()

        products
            .filter { !it.isConsumed }
            .forEach { product ->
                val expDate = product.expirationDate ?: today
                val daysRemaining = TimeUnit.MILLISECONDS.toDays(expDate - today).toInt()
                val ingredientName = extractIngredientName(product)

                if (ingredientName.isBlank()) return@forEach

                when {
                    daysRemaining <= redDays -> red.add(ingredientName)
                    daysRemaining <= yellowDays -> yellow.add(ingredientName)
                    else -> green.add(ingredientName)
                }
            }

        return IngredientClassification(
            redIngredients = red.distinct(),
            yellowIngredients = yellow.distinct(),
            greenIngredients = green.distinct()
        )
    }

    /**
     * Extrae un nombre de ingrediente limpio de un UserProduct.
     * Elimina cantidades, marcas y texto generico.
     * Fallback: usa la primera categoria de OFF si el nombre es muy corto.
     */
    fun extractIngredientName(product: UserProduct): String {
        var name = product.name
            .lowercase()
            .replace(Regex("\\d+\\s*(g|kg|ml|l|cl|oz|un|uds)\\b"), "")
            .replace(Regex("\\b(marca|brand|pack|lote|bio|eco|light|zero)\\b.*"), "")
            .trim()

        if (name.length < 3 && !product.categories.isNullOrBlank()) {
            name = product.categories
                .split(",")
                .firstOrNull()
                ?.trim()
                ?.lowercase()
                ?: name
        }

        return name.replaceFirstChar { it.uppercase() }
    }

    /**
     * Genera un string ultracompacto para enviar a Groq.
     * YELLOW se fusiona con GREEN (Groq solo necesita "urgente" vs "disponible").
     * Formato: "R:huevos,leche|G:harina,sal"
     */
    fun toCompactString(classification: IngredientClassification): String {
        val r = classification.redIngredients.joinToString(",")
        val available = (classification.yellowIngredients + classification.greenIngredients)
            .distinct()
            .joinToString(",")
        return "R:$r|G:$available"
    }

    /**
     * Genera un hash MD5 del inventario clasificado (ordenado) para cache.
     * El mismo inventario siempre produce el mismo hash.
     */
    fun computeInventoryHash(classification: IngredientClassification): String {
        val sorted = toCompactString(
            classification.copy(
                redIngredients = classification.redIngredients.sorted(),
                yellowIngredients = classification.yellowIngredients.sorted(),
                greenIngredients = classification.greenIngredients.sorted()
            )
        )
        val md5 = MessageDigest.getInstance("MD5")
        return md5.digest(sorted.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
