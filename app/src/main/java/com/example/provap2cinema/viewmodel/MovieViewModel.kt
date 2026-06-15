package com.example.provap2cinema.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.provap2cinema.model.Movie
import com.example.provap2cinema.repository.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

sealed class MovieState {
    object Idle : MovieState()
    object Loading : MovieState()
    data class Success(val movies: List<Movie>) : MovieState()
    data class Error(val message: String) : MovieState()
}

class MovieViewModel(private val repository: MovieRepository = MovieRepository()) : ViewModel() {

    var movieState by mutableStateOf<MovieState>(MovieState.Idle)
        private set

    init {
        fetchMovies()
    }

    fun fetchMovies() {
        viewModelScope.launch {
            movieState = MovieState.Loading
            repository.getMovies()
                .onSuccess { movies ->
                    movieState = MovieState.Success(movies)
                }
                .onFailure { error ->
                    movieState = MovieState.Error(error.message ?: "Erro ao carregar filmes")
                }
        }
    }

    fun uploadMovieImage(imageUri: Uri, onResult: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.uploadImage(imageUri)
                .onSuccess { downloadUrl ->
                    onResult(downloadUrl)
                }
                .onFailure { error ->
                    onError(error.message ?: "Erro ao fazer upload da imagem")
                }
        }
    }

    fun processImageToBase64(context: Context, uri: Uri, onResult: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val base64String = withContext(Dispatchers.IO) {
                    val contentResolver = context.contentResolver
                    
                    // 1. Decodificar apenas as dimensões para evitar OutOfMemory
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

                    // 2. Calcular o redimensionamento ideal (limite de 600px para Base64 não ficar gigante)
                    options.inSampleSize = calculateInSampleSize(options, 600, 600)
                    options.inJustDecodeBounds = false
                    
                    // 3. Decodificar o bitmap real com o tamanho reduzido
                    val bitmap = contentResolver.openInputStream(uri)?.use { 
                        BitmapFactory.decodeStream(it, null, options) 
                    } ?: throw Exception("Não foi possível ler a imagem")

                    // 4. Comprimir para JPEG e converter para Base64 (NO_WRAP para não quebrar a string)
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val byteArray = outputStream.toByteArray()
                    Base64.encodeToString(byteArray, Base64.NO_WRAP)
                }
                onResult("data:image/jpeg;base64,$base64String")
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Erro ao processar imagem", e)
                onError(e.message ?: "Erro desconhecido")
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun addMovie(movie: Movie, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.addMovie(movie)
                .onSuccess {
                    fetchMovies()
                    onSuccess()
                }
        }
    }

    fun updateMovie(movie: Movie, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateMovie(movie)
                .onSuccess {
                    fetchMovies()
                    onSuccess()
                }
        }
    }

    fun deleteMovie(movieId: String) {
        viewModelScope.launch {
            repository.deleteMovie(movieId)
                .onSuccess { fetchMovies() }
        }
    }
}
