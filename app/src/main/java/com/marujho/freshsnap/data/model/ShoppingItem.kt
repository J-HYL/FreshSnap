package com.marujho.freshsnap.data.model

import com.google.firebase.firestore.PropertyName

data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    val quantity: String = "",
    val source: String = SOURCE_MANUAL,
    val category: String = ShoppingCategory.OTHER.name,
    val addedDate: Long = System.currentTimeMillis(),
    @get:PropertyName("isChecked")
    @set:PropertyName("isChecked")
    var isChecked: Boolean = false
) {
    companion object {
        const val SOURCE_MANUAL = "manual"
        const val SOURCE_RECIPE = "recipe"
    }
}
