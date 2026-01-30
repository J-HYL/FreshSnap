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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _products = MutableStateFlow<List<ProductUiModel>>(emptyList())
    val products: StateFlow<List<ProductUiModel>> = _products.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            val rawList = getSampleProducts()
            val sortedList = rawList.sortedBy { it.expiryDays }

            _products.value = sortedList
        }
    }

    // datos de prueba
    private fun getSampleProducts(): List<ProductUiModel> {
        return listOf(
            ProductUiModel(
                "1",
                "Digestive Avena Choco",
                "Gullón",
                null,
                1,
                "15/12/2025",
                "10/12/2025",
                "400g",
                "84100001"
            ),
            ProductUiModel(
                "2",
                "Tomate frito",
                "Hacendado",
                null,
                3,
                "17/12/2025",
                "10/12/2025",
                "3 packs",
                "84800002"
            ),
            ProductUiModel(
                "3",
                "Kéfir natural",
                "Hacendado",
                null,
                5,
                "19/12/2025",
                "10/12/2025",
                "500g",
                "84800003"
            ),
            ProductUiModel(
                "4",
                "Macarrones",
                "Coviran",
                null,
                5,
                "19/12/2025",
                "10/12/2025",
                "425 g",
                "8480000142139"
            ),
            ProductUiModel(
                "5",
                "Macarrones",
                "Hacendado",
                null,
                7,
                "21/12/2025",
                "10/12/2025",
                "1 kg",
                "84800005"
            ),
            ProductUiModel(
                "6",
                "Pizza Roma",
                "Hacendado",
                null,
                10,
                "24/12/2025",
                "10/12/2025",
                "350 g",
                "84800006"
            ),
            ProductUiModel(
                "3",
                "Kéfir natural",
                "Hacendado",
                null,
                5,
                "19/12/2025",
                "10/12/2025",
                "500g",
                "84800003"
            ),
        )
    }
}