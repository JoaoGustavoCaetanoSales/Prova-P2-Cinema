package com.example.provap2cinema.repository

import android.net.Uri
import com.example.provap2cinema.model.Movie
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MovieRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val moviesCollection = db.collection("filmes")
    private val storageRef = storage.reference.child("posters")

    suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val fileName = UUID.randomUUID().toString()
            val fileRef = storageRef.child(fileName)
            fileRef.putFile(imageUri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMovie(movie: Movie): Result<Unit> {
        return try {
            moviesCollection.add(movie).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovies(): Result<List<Movie>> {
        return try {
            val snapshot = moviesCollection.get().await()
            val movies = snapshot.toObjects(Movie::class.java)
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMovie(movie: Movie): Result<Unit> {
        return try {
            movie.id.takeIf { it.isNotEmpty() }?.let { id ->
                moviesCollection.document(id).set(movie).await()
                Result.success(Unit)
            } ?: Result.failure(Exception("ID do filme vazio"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMovie(movieId: String): Result<Unit> {
        return try {
            moviesCollection.document(movieId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
