package com.example.provap2cinema.repository

import com.example.provap2cinema.model.User
import com.example.provap2cinema.model.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun signUp(name: String, email: String, password: String, type: UserType): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Usuário nulo"))
            
            val user = User(id = firebaseUser.uid, name = name, email = email, type = type)
            
            // Salva os dados extras no Firestore
            db.collection("usuarios").document(firebaseUser.uid).set(user).await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Usuário nulo"))
            
            // Busca o tipo de usuário no Firestore
            val document = db.collection("usuarios").document(firebaseUser.uid).get().await()
            val user = document.toObject(User::class.java) ?: return Result.failure(Exception("Perfil não encontrado"))
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCurrentUser(): Result<User?> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val document = db.collection("usuarios").document(firebaseUser.uid).get().await()
                val user = document.toObject(User::class.java)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
