package com.example.provap2cinema.model

import com.google.firebase.firestore.DocumentId

enum class UserType {
    CLIENT, EMPLOYEE
}

data class User(
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    val type: UserType = UserType.CLIENT
)

data class Movie(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val duration: String = "",
    val genre: String = "",
    val rating: String = "",
    val posterUrl: String = ""
)

data class Session(
    @DocumentId val id: String = "",
    val movieId: String = "",
    val movieTitle: String = "",
    val date: String = "",
    val time: String = "",
    val room: String = "",
    val price: Double = 0.0,
    val occupiedSeats: List<String> = emptyList() // Adicionado para rastrear assentos ocupados
)

data class Seat(
    val id: String = "",
    val row: String = "",
    val number: Int = 0,
    val isOccupied: Boolean = false,
    val sessionId: String = ""
)

data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stockQuantity: Int = 0,
    val imageUrl: String = ""
)

data class Ticket(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sessionId: String = "",
    val movieTitle: String = "",
    val seat: String = "",
    val price: Double = 0.0,
    val purchaseDate: Long = System.currentTimeMillis()
)

data class Sale(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val products: List<SaleItem> = emptyList(),
    val tickets: List<Ticket> = emptyList(), // Adicionado para salvar ingressos na venda
    val totalAmount: Double = 0.0,
    val date: Long = System.currentTimeMillis()
)

data class SaleItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)
