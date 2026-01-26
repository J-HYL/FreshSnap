package com.marujho.freshsnap.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.marujho.freshsnap.data.model.FirestoreUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    // Login
    suspend fun login(email: String, pass: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success("Login exitoso")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign up
    suspend fun signUp(email: String, pass: String, name: String): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Error obteniendo UID")

            val newUser = FirestoreUser(id = uid, email = email, name = name)
            db.collection("users").document(uid).set(newUser).await()

            Result.success("Usuario creado")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun getUserName(): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("No user logged in")
            val document = db.collection("users").document(uid).get().await()
            val name = document.getString("name") ?: "Usuario"
            Result.success(name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}