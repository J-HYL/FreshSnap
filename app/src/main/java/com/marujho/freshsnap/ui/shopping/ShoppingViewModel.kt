package com.marujho.freshsnap.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.domain.ShoppingCategoryClassifier
import com.marujho.freshsnap.data.model.ShoppingItem
import com.marujho.freshsnap.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingStats(
    val total: Int,
    val checked: Int,
    val pending: Int,
    val progress: Float // 0f..1f
)

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val repository: ShoppingRepository,
    private val classifier: ShoppingCategoryClassifier
) : ViewModel() {

    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items.asStateFlow()

    private val _isShoppingMode = MutableStateFlow(false)
    val isShoppingMode: StateFlow<Boolean> = _isShoppingMode.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val stats: StateFlow<ShoppingStats> = items
        .map { list ->
            val total = list.size
            val checked = list.count { it.isChecked }
            ShoppingStats(
                total = total,
                checked = checked,
                pending = total - checked,
                progress = if (total == 0) 0f else checked.toFloat() / total.toFloat()
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ShoppingStats(0, 0, 0, 0f)
        )

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            val result = repository.getShoppingItems()
            if (result.isSuccess) {
                val raw = result.getOrThrow()
                // Reclasificar items cuya categoria guardada no coincida con la actual
                // (corrige items mal clasificados de versiones anteriores).
                val reclassified = raw.map { item ->
                    val expected = classifier.classify(item.name).name
                    if (item.category != expected) {
                        // Update silencioso en background
                        launch { repository.updateCategory(item.id, expected) }
                        item.copy(category = expected)
                    } else {
                        item
                    }
                }
                _items.value = reclassified.sortedBy { it.isChecked }
            }
        }
    }

    fun addItem(name: String, quantity: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addShoppingItem(
                name = name.trim(),
                quantity = quantity.trim(),
                source = ShoppingItem.SOURCE_MANUAL
            )
            loadItems()
        }
    }

    fun toggleCheck(item: ShoppingItem) {
        viewModelScope.launch {
            // Update optimista
            _items.value = _items.value.map {
                if (it.id == item.id) it.copy(isChecked = !item.isChecked) else it
            }.sortedBy { it.isChecked }

            repository.toggleItemCheck(item.id, !item.isChecked)
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
            loadItems()
        }
    }

    fun clearCheckedItems() {
        viewModelScope.launch {
            val result = repository.clearCheckedItems()
            if (result.isSuccess) {
                val count = result.getOrThrow()
                _snackbarMessage.value = "cleared:$count"
                loadItems()
            }
        }
    }

    fun setShoppingMode(active: Boolean) {
        _isShoppingMode.value = active
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
