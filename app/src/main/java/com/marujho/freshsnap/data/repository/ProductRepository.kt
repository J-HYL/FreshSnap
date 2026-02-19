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
            val documentRef = if(product.id.isNotEmpty()){
                collection.document(product.id)
            }
            else{
                collection.document()
            }


            // id nuevo
            val productToSave = product.copy(id = documentRef.id)

            // guardar en firebase
            documentRef.set(productToSave).await()

            Result.success("Producto guardado")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllProducts(): Result<List<UserProduct>> {
        return try {
            val collection = getUserProductsCollection() ?: throw Exception("Usuario no logueado")

            // obteber todos los documentos de la coleccion despensa (pantry) de firebase
            val snapshot = collection.get().await()

            // convertir los documentos a objetos
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserProduct::class.java)?.copy(id = doc.id)
            }

            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductByEan(barcode: String): Result<UserProduct?> {
        return try {
            val collection = getUserProductsCollection() ?: throw Exception("Usuario no logueado")

            val snapshot = collection.whereEqualTo("ean", barcode).get().await()

            if (!snapshot.isEmpty) {
                val product = snapshot.documents[0].toObject(UserProduct::class.java)
                Result.success(product)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun consumeProduct(productId: String): Result<String> {
        return try {
            val collection = getUserProductsCollection() ?: throw Exception("No user")
            collection.document(productId).update("isConsumed", true).await()
            Result.success("Producto consumido")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<String> {
        return try {
            val collection = getUserProductsCollection() ?: throw Exception("No user")
            collection.document(productId).delete().await()
            Result.success("Producto eliminado")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}