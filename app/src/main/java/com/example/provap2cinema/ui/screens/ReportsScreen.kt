package com.example.provap2cinema.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.provap2cinema.model.Sale
import com.example.provap2cinema.model.Ticket
import com.example.provap2cinema.viewmodel.ReportsState
import com.example.provap2cinema.viewmodel.ReportsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit
) {
    val state = viewModel.state

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("RELATÓRIOS DE VENDAS", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            when (state) {
                is ReportsState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ReportsState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                is ReportsState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            SummaryCards(state)
                        }

                        if (state.recentTickets.isNotEmpty()) {
                            item {
                                SectionHeader(icon = Icons.Default.LocalActivity, title = "Ingressos Recentes")
                            }
                            items(state.recentTickets) { ticket ->
                                TicketReportItem(ticket)
                            }
                        }

                        if (state.recentSales.isNotEmpty()) {
                            item {
                                SectionHeader(icon = Icons.Default.ShoppingBag, title = "Vendas Bomboniere Recentes")
                            }
                            items(state.recentSales) { sale ->
                                SaleReportItem(sale)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCards(state: ReportsState.Success) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StatCard(
            title = "Receita Total",
            value = "R$ ${"%.2f".format(state.totalRevenue)}",
            icon = Icons.Default.Payments,
            color = MaterialTheme.colorScheme.primary
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                title = "Ingressos",
                value = state.ticketsCount.toString(),
                icon = Icons.Default.ConfirmationNumber,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Bomboniere",
                value = "R$ ${"%.2f".format(state.productsRevenue)}",
                icon = Icons.Default.ShoppingBag,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun TicketReportItem(ticket: Ticket) {
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(ticket.purchaseDate))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(ticket.movieTitle, fontWeight = FontWeight.Bold)
                Text("Assento ${ticket.seat} • $dateStr", style = MaterialTheme.typography.bodySmall)
            }
            Text("R$ ${"%.2f".format(ticket.price)}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun SaleReportItem(sale: Sale) {
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(sale.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Pedido Bomboniere", fontWeight = FontWeight.Bold)
                Text("${sale.products.size} itens • $dateStr", style = MaterialTheme.typography.bodySmall)
            }
            Text("R$ ${"%.2f".format(sale.totalAmount)}", fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
        }
    }
}
