package com.example.provap2cinema.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.provap2cinema.model.Sale
import com.example.provap2cinema.model.Ticket
import com.example.provap2cinema.repository.SaleRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

sealed class PurchaseHistoryState {
    object Loading : PurchaseHistoryState()
    data class Success(val tickets: List<Ticket>, val sales: List<Sale>) : PurchaseHistoryState()
    data class Error(val message: String) : PurchaseHistoryState()
}

class PurchaseHistoryViewModel(private val repository: SaleRepository = SaleRepository()) : ViewModel() {
    var state by mutableStateOf<PurchaseHistoryState>(PurchaseHistoryState.Loading)
        private set

    init {
        loadHistory()
    }

    fun loadHistory() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            state = PurchaseHistoryState.Error("Usuário não autenticado")
            return
        }

        viewModelScope.launch {
            state = PurchaseHistoryState.Loading
            val ticketsResult = repository.getTicketsByUser(userId)
            val salesResult = repository.getSales() // Idealmente filtraria por usuário no repositório

            if (ticketsResult.isSuccess && salesResult.isSuccess) {
                val userSales = salesResult.getOrNull()?.filter { it.userId == userId } ?: emptyList()
                state = PurchaseHistoryState.Success(
                    tickets = ticketsResult.getOrDefault(emptyList()),
                    sales = userSales
                )
            } else {
                state = PurchaseHistoryState.Error("Erro ao carregar histórico")
            }
        }
    }
}
