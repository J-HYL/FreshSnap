package com.marujho.freshsnap.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.marujho.freshsnap.data.model.ShoppingItem
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShoppingRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private fun getShoppingCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("shopping_list")
    }

    suspend fun addShoppingItem(name: String): Result<String> {
        return try {
            val collection = getShoppingCollection() ?: throw Exception("Usuario no logueado")
            val docRef = collection.document()
            val item = ShoppingItem(id = docRef.id, name = name)
            docRef.set(item).await()
            Result.success("Añadido a la lista")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getShoppingItems(): Result<List<ShoppingItem>> {
        return try {
            val collection = getShoppingCollection() ?: throw Exception("Usuario no logueado")
            val snapshot = collection.get().await()
            val items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ShoppingItem::class.java)?.copy(id = doc.id)
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleItemCheck(itemId: String, isChecked: Boolean): Result<String> {
        return try {
            val collection = getShoppingCollection() ?: throw Exception("Usuario no logueado")
            collection.document(itemId).update("isChecked", isChecked).await()
            Result.success("Actualizado")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(itemId: String): Result<String> {
        return try {
            val collection = getShoppingCollection() ?: throw Exception("Usuario no logueado")
            collection.document(itemId).delete().await()
            Result.success("Eliminado")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}