package com.marujho.freshsnap.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.model.ShoppingItem
import com.marujho.freshsnap.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            val result = repository.getShoppingItems()
            if (result.isSuccess) {
                _items.value = result.getOrThrow().sortedBy { it.isChecked }
            }
        }
    }

    fun addItem(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addShoppingItem(name.trim())
            loadItems()
        }
    }

    fun toggleCheck(item: ShoppingItem) {
        viewModelScope.launch {
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
}