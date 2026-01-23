package com.marujho.freshsnap.ui.openFoodFacts


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.remote.api.OpenFoodFactsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OpenFoodViewModel @Inject constructor(
    private val api: OpenFoodFactsApi
) : ViewModel() {

    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            Log.d("OFF_TEST", "Barcode recibido: $barcode")

            try {
                val response = api.getProductByBarcode(barcode)

                Log.d("OFF_TEST", "Status: ${response.status}")
                Log.d("OFF_TEST", "Producto: ${response.product}")
            } catch (e: Exception) {
                Log.e("OFF_TEST", "Error en OpenFoodFacts", e)
            }
        }
    }
}
