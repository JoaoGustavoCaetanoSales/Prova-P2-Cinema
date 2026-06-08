package com.example.provap2cinema.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.provap2cinema.model.Session
import com.example.provap2cinema.repository.SaleRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    session: Session,
    saleRepository: SaleRepository = remember { SaleRepository() },
    onNavigateBack: () -> Unit,
    onConfirmSeats: (List<String>) -> Unit
) {
    val rows = listOf("A", "B", "C", "D", "E", "F", "G")
    val seatsPerRow = 8
    val allSeats = remember {
        rows.flatMap { row -> (1..seatsPerRow).map { "$row$it" } }
    }
    
    var occupiedSeats by remember { mutableStateOf(setOf<String>()) } 
    var isLoading by remember { mutableStateOf(true) }
    val selectedSeats = remember { mutableStateListOf<String>() }

    LaunchedEffect(session.id) {
        isLoading = true
        saleRepository.getTicketsBySession(session.id).onSuccess { tickets ->
            occupiedSeats = tickets.map { it.seat }.toSet()
        }
        isLoading = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ESCOLHA SEUS ASSENTOS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                        Text(session.movieTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Assentos: ${if (selectedSeats.isEmpty()) "Nenhum" else selectedSeats.joinToString(", ")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Total: R$ ${"%.2f".format(selectedSeats.size * session.price)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { onConfirmSeats(selectedSeats.toList()) },
                            shape = RoundedCornerShape(12.dp),
                            enabled = selectedSeats.isNotEmpty() && !isLoading,
                            modifier = Modifier.height(56.dp).padding(start = 16.dp)
                        ) {
                            Text("CONFIRMAR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(0.9f).height(6.dp).clip(RoundedCornerShape(50))
                            .background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary, Color.Transparent)))
                    )
                    Text("TELA", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(top = 16.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(seatsPerRow),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(allSeats) { seat ->
                        val isOccupied = occupiedSeats.contains(seat)
                        val isSelected = selectedSeats.contains(seat)
                        
                        val containerColor = when {
                            isOccupied -> MaterialTheme.colorScheme.surfaceVariant
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surface
                        }
                        
                        val contentColor = when {
                            isOccupied -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(containerColor)
                                .then(if (!isSelected && !isOccupied) Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp)) else Modifier)
                                .clickable(enabled = !isOccupied) {
                                    if (isSelected) selectedSeats.remove(seat)
                                    else selectedSeats.add(seat)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = seat, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = contentColor)
                        }
                    }
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        LegendItem("Livre", MaterialTheme.colorScheme.surface, hasBorder = true)
                        LegendItem("Ocupado", MaterialTheme.colorScheme.surfaceVariant)
                        LegendItem("Selecionado", MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, hasBorder: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(color).then(if (hasBorder) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)) else Modifier))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
