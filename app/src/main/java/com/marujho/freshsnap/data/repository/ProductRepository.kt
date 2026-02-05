package com.marujho.freshsnap.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.marujho.freshsnap.data.model.UserProduct
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private fun getUserProductsCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("pantry")
    }

    suspend fun saveProduct(product: UserProduct): Result<String> {
        return try {
            val collection = getUserProductsCollection()
                ?: throw Exception("Error no se encuentra el usuario")

            // documento vacio
            val documentRef = collection.document()

            // id nuevo
            val productToSave = product.copy(id = documentRef.id)

            // guardar en firebase
            documentRef.set(productToSave).await()

            Result.success("Producto guardado")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}