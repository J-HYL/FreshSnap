package com.marujho.freshsnap.ui.main

import android.content.Context
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.marujho.freshsnap.R
import com.marujho.freshsnap.data.repository.UserPreferences
import com.marujho.freshsnap.worker.ExpirationWorker

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context : Context,
    private val repository: ProductRepository,
    private val userPreferences: UserPreferences,
    private val shoppingRepository: com.marujho.freshsnap.data.repository.ShoppingRepository
) : ViewModel() {
    private val _allProducts = MutableStateFlow<List<ProductUiModel>>(emptyList())
    private val _filteredProducts = MutableStateFlow<List<ProductUiModel>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val products: StateFlow<List<ProductUiModel>> = _filteredProducts.asStateFlow()

    var selectedTab by mutableStateOf(0)
        private set

    private val _redDays = MutableStateFlow(2)
    val redDays: StateFlow<Int> = _redDays

    private val _yellowDays = MutableStateFlow(5)
    val yellowDays: StateFlow<Int> = _yellowDays

    init {
        loadProducts()
        scheduleExpirationWorker()
        // Sincronizar los días de alerta con UserPreferences
        viewModelScope.launch {
            userPreferences.expiryRedDays.collect { _redDays.value = it }
        }
        viewModelScope.launch {
            userPreferences.expiryYellowDays.collect { _yellowDays.value = it }
        }
    }
    private fun scheduleExpirationWorker() {
        val expirationWorkRequest = PeriodicWorkRequestBuilder<ExpirationWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "CheckExpirationDaily",
            ExistingPeriodicWorkPolicy.KEEP,
            expirationWorkRequest
        )
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            val result = repository.getAllProducts()
            if (result.isSuccess) {
                val rawList = result.getOrThrow().map { it.toUiModel() }
                _allProducts.value = rawList
                filterProducts()
            }
        }
    }

    fun onTabSelected(index: Int) {
        selectedTab = index
        filterProducts()
    }

    private fun filterProducts() {
        val today = System.currentTimeMillis()
        val fullList = _allProducts.value
        val query = _searchQuery.value.trim().lowercase()

        val tabFilteredList = when (selectedTab) {
            0 -> { // no consumidos y no caducados
                fullList.filter { !it.isConsumed && it.expirationTimestamp >= today }
                    .sortedBy { it.expiryDays }
            }
            1 -> { // no consumidos + fecha > hoy
                fullList.filter { !it.isConsumed && it.expirationTimestamp < today }
                    .sortedByDescending { it.expirationTimestamp }
            }
            2 -> { // consumidos
                fullList.filter { it.isConsumed }
                    .sortedByDescending { it.scannedTimestamp }
            }
            else -> emptyList()
        }

        _filteredProducts.value = if (query.isBlank()) {
            tabFilteredList
        } else {
            tabFilteredList.filter { product ->
                product.name.lowercase().contains(query) || product.ean.contains(query)
            }
        }
    }

    // funciones del swipe
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            loadProducts()
        }
    }

    fun consumeProduct(productId: String) {
        viewModelScope.launch {
            repository.consumeProduct(productId)
            loadProducts()
        }
    }

    private fun ProductUiModel.isExpired(today: Long): Boolean {
        return this.expirationTimestamp < today
    }

    private fun UserProduct.toUiModel(): ProductUiModel {
        val today = System.currentTimeMillis()
        val expDate = this.expirationDate ?: today
        val diffInMillis = expDate - today
        val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

        val expiryTextFormatted = when {
            daysRemaining < 0 -> context.getString(R.string.expiry_expired)
            daysRemaining == 0 -> context.getString(R.string.expiry_today)
            daysRemaining == 1 -> context.getString(R.string.expiry_one_day)
            daysRemaining < 30 -> context.getString(R.string.expiry_days, daysRemaining)
            daysRemaining < 365 -> context.getString(R.string.expiry_months, daysRemaining / 30)
            else -> context.getString(R.string.expiry_years, daysRemaining / 365)
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expDateString = dateFormat.format(Date(expDate))
        val scanDateString = dateFormat.format(Date(this.scanDate))

        return ProductUiModel(
            id = this.id,
            name = this.name.ifBlank { "Sin nombre" },
            brand = this.brand.ifBlank { "Sin marca" },
            imageUrl = this.imageUrl,
            expiryDays = daysRemaining,
            expiryText = expiryTextFormatted,
            expiryDate = expDateString,
            expirationTimestamp = expDate,
            scannedDate = scanDateString,
            scannedTimestamp = this.scanDate,
            quantity = this.quantity ?: "-",
            ean = this.ean,
            nutriScore = this.nutriScore ?: "?",
            greenScore = this.greenScore ?: "?",
            isConsumed = this.isConsumed
        )
    }

    fun addToShoppingList(productName: String) {
        viewModelScope.launch {
            shoppingRepository.addShoppingItem(productName)
        }
    }
}