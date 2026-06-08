package com.example.provap2cinema.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.provap2cinema.model.Movie
import com.example.provap2cinema.model.Session
import com.example.provap2cinema.viewmodel.MovieState
import com.example.provap2cinema.viewmodel.MovieViewModel
import com.example.provap2cinema.viewmodel.SessionState
import com.example.provap2cinema.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionScreen(
    movieViewModel: MovieViewModel,
    sessionViewModel: SessionViewModel,
    sessionId: String? = null,
    onNavigateBack: () -> Unit
) {
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val movieState = movieViewModel.movieState
    val isEditMode = sessionId != null

    LaunchedEffect(sessionId) {
        if (isEditMode) {
            val state = sessionViewModel.sessionState
            if (state is SessionState.Success) {
                state.sessions.find { it.id == sessionId }?.let { session ->
                    date = session.date
                    time = session.time
                    room = session.room
                    price = session.price.toString()
                    
                    // Try to find the movie in the movie list
                    val mState = movieViewModel.movieState
                    if (mState is MovieState.Success) {
                        selectedMovie = mState.movies.find { it.id == session.movieId }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (isEditMode) "EDITAR SESSÃO" else "NOVA SESSÃO", 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 1.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Text(
                "Configurar Sessão",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )

            // Movie Selector
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedMovie?.title ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Selecione o Filme") },
                    leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Escolha um filme da lista") }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (movieState is MovieState.Success) {
                        movieState.movies.forEach { movie ->
                            DropdownMenuItem(
                                text = { Text(movie.title) },
                                onClick = {
                                    selectedMovie = movie
                                    expanded = false
                                }
                            )
                        }
                    } else {
                        DropdownMenuItem(
                            text = { Text("Carregando filmes...") },
                            onClick = { },
                            enabled = false
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("DD/MM") }
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Horário") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("HH:mm") }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Sala") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Ex: 01") }
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Preço (R$)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("0.00") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    selectedMovie?.let { movie ->
                        val session = Session(
                            id = sessionId ?: "",
                            movieId = movie.id,
                            movieTitle = movie.title,
                            date = date,
                            time = time,
                            room = room,
                            price = price.toDoubleOrNull() ?: 0.0
                        )
                        if (isEditMode) {
                            sessionViewModel.updateSession(session) {
                                onNavigateBack()
                            }
                        } else {
                            sessionViewModel.addSession(session) {
                                onNavigateBack()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedMovie != null && date.isNotBlank() && time.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (isEditMode) "SALVAR ALTERAÇÕES" else "CRIAR SESSÃO", 
                    fontWeight = FontWeight.Bold, 
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}
