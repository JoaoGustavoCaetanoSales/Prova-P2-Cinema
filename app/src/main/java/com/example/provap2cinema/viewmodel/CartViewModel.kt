package com.example.provap2cinema.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.provap2cinema.model.Product
import com.example.provap2cinema.model.Sale
import com.example.provap2cinema.model.SaleItem
import com.example.provap2cinema.model.Ticket
import com.example.provap2cinema.repository.ProductRepository
import com.example.provap2cinema.repository.SaleRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CartViewModel(
    private val productRepository: ProductRepository = ProductRepository(),
    private val saleRepository: SaleRepository = SaleRepository()
) : ViewModel() {
    val tickets = mutableStateListOf<Ticket>()
    val products = mutableStateListOf<SaleItem>()

    var isProcessing by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val totalAmount: Double
        get() = tickets.sumOf { it.price } + products.sumOf { it.price * it.quantity }

    fun addTicket(ticket: Ticket) {
        if (tickets.none { it.sessionId == ticket.sessionId && it.seat == ticket.seat }) {
            tickets.add(ticket)
        }
    }

    fun removeTicket(ticket: Ticket) {
        tickets.remove(ticket)
    }

    fun removeTicketsBySession(sessionId: String) {
        tickets.removeAll { it.sessionId == sessionId }
    }

    fun addProduct(product: Product, quantity: Int = 1) {
        val existing = products.find { it.productId == product.id }
        val currentQtyInCart = existing?.quantity ?: 0
        val totalRequested = currentQtyInCart + quantity
        
        if (totalRequested <= product.stockQuantity) {
            if (existing != null) {
                val index = products.indexOf(existing)
                products[index] = existing.copy(quantity = totalRequested)
            } else {
                products.add(SaleItem(product.id, product.name, quantity, product.price))
            }
        }
    }

    fun removeProduct(productId: String) {
        val existing = products.find { it.productId == productId }
        if (existing != null) {
            products.remove(existing)
        }
    }

    fun finishPurchase(onSuccess: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            
            val sale = Sale(
                userId = userId,
                products = products.toList(),
                totalAmount = totalAmount
            )

            saleRepository.finalizePurchase(tickets.toList(), sale)
                .onSuccess {
                    clearCart()
                    isProcessing = false
                    onSuccess()
                }
                .onFailure { error ->
                    isProcessing = false
                    errorMessage = error.message ?: "Erro ao finalizar compra"
                }
        }
    }

    fun clearCart() {
        tickets.clear()
        products.clear()
        errorMessage = null
    }
}
