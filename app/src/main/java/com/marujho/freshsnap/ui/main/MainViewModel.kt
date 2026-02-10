package com.marujho.freshsnap.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.model.UserProduct
import com.marujho.freshsnap.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {
    private val _products = MutableStateFlow<List<ProductUiModel>>(emptyList())
    val products: StateFlow<List<ProductUiModel>> = _products.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            val result = repository.getAllProducts()

            if (result.isSuccess) {
                val userProducts = result.getOrDefault(emptyList())

                val uiList = userProducts.map { it.toUiModel() }

                val sortedList = uiList.sortedBy { it.expiryDays }

                _products.value = sortedList
            } else {
                _products.value = emptyList()
            }
        }
    }

    private fun UserProduct.toUiModel(): ProductUiModel {
        val today = System.currentTimeMillis()
        val expDate = this.expirationDate ?: today // si es null ponemos que es hoy
        val diffInMillis = expDate - today
        val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

        // formatesr fecha
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expDateString = dateFormat.format(Date(expDate))
        val scanDateString = dateFormat.format(Date(this.scanDate))

        return ProductUiModel(
            id = this.id,
            name = this.name.ifBlank { "Sin nombre" },
            brand = this.brand.ifBlank { "Sin marca" },
            imageUrl = this.imageUrl,
            expiryDays = daysRemaining,
            expiryDate = expDateString,
            scannedDate = scanDateString,
            quantity = this.quantity ?: "-",
            ean = this.ean,
            nutriScore = this.nutriScore ?: "?",
            greenScore = this.greenScore ?: "?"
        )
    }
}