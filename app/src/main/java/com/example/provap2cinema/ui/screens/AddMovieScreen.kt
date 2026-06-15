package com.example.provap2cinema.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.provap2cinema.model.Movie
import com.example.provap2cinema.viewmodel.MovieState
import com.example.provap2cinema.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovieScreen(
    viewModel: MovieViewModel,
    movieId: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var posterUrl by remember { mutableStateOf("") }
    
    // imageUri guarda o preview local enquanto a conversão acontece
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val isEditMode = movieId != null

    // Carregar dados iniciais ao editar (reage a mudanças no estado do ViewModel)
    LaunchedEffect(movieId, viewModel.movieState) {
        if (isEditMode) {
            val state = viewModel.movieState
            if (state is MovieState.Success) {
                state.movies.find { it.id == movieId }?.let { movie ->
                    // Preenche apenas se os campos estiverem vazios para não sobrescrever edições manuais
                    if (title.isEmpty()) title = movie.title
                    if (description.isEmpty()) description = movie.description
                    if (duration.isEmpty()) duration = movie.duration
                    if (genre.isEmpty()) genre = movie.genre
                    if (rating.isEmpty()) rating = movie.rating
                    if (posterUrl.isEmpty()) posterUrl = movie.posterUrl
                }
            }
        }
    }

    // Seletor de galeria que já inicia o processamento Base64
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it // Preview imediato
            isUploadingImage = true
            viewModel.processImageToBase64(
                context = context,
                uri = it,
                onResult = { base64String ->
                    posterUrl = base64String // Preenche o campo da URL
                    isUploadingImage = false
                    Toast.makeText(context, "Imagem convertida em URL!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    isUploadingImage = false
                    Toast.makeText(context, "Erro ao converter: $error", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (isEditMode) "EDITAR FILME" else "ADICIONAR FILME", 
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Poster do Filme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                // Se o Base64 estiver pronto, usa ele. Se não, usa a URI da galeria.
                val displaySource = if (posterUrl.isNotEmpty()) posterUrl else imageUri
                
                if (displaySource != null) {
                    AsyncImage(
                        model = displaySource,
                        contentDescription = "Poster selecionado",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    if (isUploadingImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "Alterar poster",
                                modifier = Modifier.padding(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Tocar para selecionar da galeria",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // O campo da URL será preenchido automaticamente com o texto Base64 gerado
            OutlinedTextField(
                value = posterUrl,
                onValueChange = { posterUrl = it },
                label = { Text("URL do Poster") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                placeholder = { Text("A URL (Base64) aparecerá aqui...") },
                supportingText = { Text("A imagem é convertida em texto automaticamente") }
            )

            Text(
                "Informações Gerais",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Sinopse") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duração") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Ex: 120 min") }
                )
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Gênero") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = rating,
                onValueChange = { rating = it },
                label = { Text("Classificação") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Ex: 14 anos") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = {
                        isSaving = true
                        val movie = Movie(
                            id = movieId ?: "",
                            title = title,
                            description = description,
                            duration = duration,
                            genre = genre,
                            rating = rating,
                            posterUrl = posterUrl
                        )
                        
                        if (isEditMode) {
                            viewModel.updateMovie(movie) {
                                isSaving = false
                                onNavigateBack()
                            }
                        } else {
                            viewModel.addMovie(movie) {
                                isSaving = false
                                onNavigateBack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = title.isNotBlank() && description.isNotBlank() && posterUrl.isNotBlank() && !isUploadingImage,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        if (isEditMode) "SALVAR ALTERAÇÕES" else "CADASTRAR FILME", 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = 1.2.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
