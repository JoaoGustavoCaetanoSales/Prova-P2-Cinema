package com.example.provap2cinema.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.provap2cinema.model.Movie
import com.example.provap2cinema.repository.MovieRepository
import kotlinx.coroutines.launch

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

    fun uploadMovieImage(imageUri: Uri, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            repository.uploadImage(imageUri)
                .onSuccess { downloadUrl ->
                    onSuccess(downloadUrl)
                }
                .onFailure {
                    // Handle error (could add a state for this)
                }
        }
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
