package com.example.provap2cinema.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.provap2cinema.model.Sale
import com.example.provap2cinema.model.Ticket
import com.example.provap2cinema.repository.SaleRepository
import kotlinx.coroutines.launch

sealed class ReportsState {
    object Loading : ReportsState()
    data class Success(
        val totalRevenue: Double,
        val ticketsCount: Int,
        val productsRevenue: Double,
        val ticketsRevenue: Double,
        val recentSales: List<Sale>,
        val recentTickets: List<Ticket>
    ) : ReportsState()
    data class Error(val message: String) : ReportsState()
}

class ReportsViewModel(private val repository: SaleRepository = SaleRepository()) : ViewModel() {
    var state by mutableStateOf<ReportsState>(ReportsState.Loading)
        private set

    init {
        fetchReports()
    }

    fun fetchReports() {
        viewModelScope.launch {
            state = ReportsState.Loading
            val salesResult = repository.getSales()
            val ticketsResult = repository.getAllTickets()

            if (salesResult.isSuccess && ticketsResult.isSuccess) {
                val sales = salesResult.getOrDefault(emptyList())
                val tickets = ticketsResult.getOrDefault(emptyList())
                
                val productsRevenue = sales.sumOf { it.totalAmount }
                val ticketsRevenue = tickets.sumOf { it.price }
                
                state = ReportsState.Success(
                    totalRevenue = productsRevenue + ticketsRevenue,
                    ticketsCount = tickets.size,
                    productsRevenue = productsRevenue,
                    ticketsRevenue = ticketsRevenue,
                    recentSales = sales.takeLast(10).reversed(),
                    recentTickets = tickets.takeLast(10).reversed()
                )
            } else {
                state = ReportsState.Error("Erro ao carregar relatórios")
            }
        }
    }
}
