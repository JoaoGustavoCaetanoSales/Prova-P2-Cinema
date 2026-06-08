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

sealed class SaleState {
    object Idle : SaleState()
    object Loading : SaleState()
    object Success : SaleState()
    data class Error(val message: String) : SaleState()
}

class SaleViewModel(private val repository: SaleRepository = SaleRepository()) : ViewModel() {

    var saleState by mutableStateOf<SaleState>(SaleState.Idle)
        private set

    fun finalizePurchase(tickets: List<Ticket>, sale: Sale, onSuccess: () -> Unit) {
        viewModelScope.launch {
            saleState = SaleState.Loading
            repository.finalizePurchase(tickets, sale)
                .onSuccess {
                    saleState = SaleState.Success
                    onSuccess()
                }
                .onFailure { error ->
                    saleState = SaleState.Error(error.message ?: "Erro ao finalizar compra")
                }
        }
    }

    fun resetState() {
        saleState = SaleState.Idle
    }
}
