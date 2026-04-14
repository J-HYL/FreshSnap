package com.marujho.freshsnap.data.model

import com.google.firebase.firestore.PropertyName

data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    val addedDate: Long = System.currentTimeMillis(),
    @get:PropertyName("isChecked")
    @set:PropertyName("isChecked")
    var isChecked: Boolean = false
)