package com.marujho.freshsnap.data.model

import com.google.firebase.firestore.PropertyName
data class UserProduct(
    // id y fechas
    val id: String = "",
    val ean: String = "",
    val scanDate: Long = System.currentTimeMillis(),
    val expirationDate: Long? = null,

    // info general
    val name: String = "",
    val brand: String = "",
    val imageUrl: String? = null,
    val quantity: String? = null,
    val categories: String? = null,
    val packaging: String? = null,
    val countries: String? = null,

    // scores
    val nutriScore: String? = null,
    val novaGroup: Int? = null,
    val greenScore: String? = null,

    // informacion nutricional (100g)
    val energyKcal: Double? = null,
    val energyKj: Double? = null,
    val fat: Double? = null,
    val saturatedFat: Double? = null,
    val carbohydrates: Double? = null,
    val sugars: Double? = null,
    val proteins: Double? = null,
    val salt: Double? = null,
    val fiber: Double? = null,
    val sodium: Double? = null,

    // datos nutricionales
    val fatLevel: String? = null,
    val saturatedFatLevel: String? = null,
    val sugarLevel: String? = null,
    val saltLevel: String? = null,

    val allergensTags: List<String>? = null,

    @get:PropertyName("isConsumed")
    @set:PropertyName("isConsumed")
    var isConsumed: Boolean = false
    // forzar nombre (si no la bd lo detecta como Consumed)
)