package com.example.provap2cinema.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.provap2cinema.model.Product
import com.example.provap2cinema.repository.ProductRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

sealed class ProductState {
    object Idle : ProductState()
    object Loading : ProductState()
    data class Success(val products: List<Product>) : ProductState()
    data class Error(val message: String) : ProductState()
}

class ProductViewModel(private val repository: ProductRepository = ProductRepository()) : ViewModel() {

    var productState by mutableStateOf<ProductState>(ProductState.Idle)
        private set

    private var listenerRegistration: ListenerRegistration? = null

    init {
        startListening()
    }

    private fun startListening() {
        productState = ProductState.Loading
        listenerRegistration = repository.listenToProducts { products ->
            productState = ProductState.Success(products)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            productState = ProductState.Loading
            repository.getProducts()
                .onSuccess { products ->
                    productState = ProductState.Success(products)
                }
                .onFailure { error ->
                    productState = ProductState.Error(error.message ?: "Erro ao carregar produtos")
                }
        }
    }

    fun addProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.addProduct(product)
                .onSuccess {
                    onSuccess()
                }
        }
    }

    fun updateStock(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateStock(productId, newQuantity)
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun updateProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProduct(product)
                .onSuccess {
                    onSuccess()
                }
        }
    }
}
