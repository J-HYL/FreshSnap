package com.marujho.freshsnap.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.marujho.freshsnap.data.domain.ShoppingCategoryClassifier
import com.marujho.freshsnap.data.model.ShoppingItem
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShoppingRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val classifier: ShoppingCategoryClassifier
) {
    private fun getShoppingCollection() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("shopping_list")
    }

    /**
     * Anade un item a la lista de la compra con clasificacion automatica de categoria.
     * @param name nombre del producto (ej. "Leche entera")
     * @param quantity cantidad opcional (ej. "1L", "200g")
     * @param source origen del item: ShoppingItem.SOURCE_MANUAL o SOURCE_RECIPE
     */
    suspend fun addShoppingItem(
        name: String,
        quantity: String = "",
        source: String = ShoppingItem.SOURCE_MANUAL
    ): Result<String> {
        return try {
            val collection = getShoppingCollection() ?: throw Exception("Usuario no logueado")
            val docRef = collection.document()
            val category = classifier.classify(name)
            val item = ShoppingItem(
                id = docRef.id,
                name = name,
                quantity = quantity,
                source = source,
                category = category.name
            )
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

    /** Actualiza solo la categoria de un item (para reclasificacion). */
    suspend fun updateCategory(itemId: String, categoryName: String): Result<String> {
        return try {
            val collection = getShoppingCollection() ?: throw Exception("Usuario no logueado")
            collection.document(itemId).update("category", categoryName).await()
            Result.success("Categoria actualizada")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Borra todos los items marcados como comprados. Devuelve cuantos se eliminaron. */
    suspend fun clearCheckedItems(): Result<Int> {
        return try {
            val collection = getShoppingCollection() ?: throw Exception("Usuario no logueado")
            val snapshot = collection.whereEqualTo("isChecked", true).get().await()
            val batch = db.batch()
            snapshot.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
            Result.success(snapshot.documents.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
