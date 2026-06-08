package com.example.provap2cinema.repository

import com.example.provap2cinema.model.Session
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SessionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val sessionsCollection = db.collection("sessoes")

    suspend fun addSession(session: Session): Result<Unit> {
        return try {
            sessionsCollection.add(session).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSessions(): Result<List<Session>> {
        return try {
            val snapshot = sessionsCollection.get().await()
            val sessions = snapshot.toObjects(Session::class.java)
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSession(session: Session): Result<Unit> {
        return try {
            sessionsCollection.document(session.id).set(session).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSession(sessionId: String): Result<Unit> {
        return try {
            sessionsCollection.document(sessionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
