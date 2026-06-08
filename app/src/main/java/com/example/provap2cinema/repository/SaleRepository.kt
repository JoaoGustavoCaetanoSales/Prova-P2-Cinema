package com.example.provap2cinema.repository

import com.example.provap2cinema.model.Sale
import com.example.provap2cinema.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SaleRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun finalizePurchase(tickets: List<Ticket>, sale: Sale): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuário não logado"))
            
            db.runTransaction { transaction ->
                val now = System.currentTimeMillis()

                // 1. LEITURAS PRIMEIRO (Obrigatório no Firestore)
                
                // Verificar se algum assento já foi ocupado (usando ID previsível)
                tickets.forEach { ticket ->
                    val ticketId = "${ticket.sessionId}_${ticket.seat}"
                    val ticketRef = db.collection("ingressos").document(ticketId)
                    if (transaction.get(ticketRef).exists()) {
                        throw Exception("O assento ${ticket.seat} já foi reservado por outro usuário.")
                    }
                }

                // Ler estoque dos produtos antes de qualquer escrita
                val productRefsAndSnapshots = sale.products.map { item ->
                    val productRef = db.collection("produtos").document(item.productId)
                    productRef to transaction.get(productRef)
                }

                // 2. ESCRITAS DEPOIS das leituras
                
                // Salvar os ingressos
                tickets.forEach { ticket ->
                    val ticketId = "${ticket.sessionId}_${ticket.seat}"
                    val ticketRef = db.collection("ingressos").document(ticketId)
                    transaction.set(ticketRef, ticket.copy(id = ticketId, userId = userId, purchaseDate = now))
                }

                // Salvar a venda de produtos
                if (sale.products.isNotEmpty()) {
                    val saleRef = db.collection("vendas").document()
                    transaction.set(saleRef, sale.copy(userId = userId, date = now))
                    
                    // Atualizar estoque
                    productRefsAndSnapshots.forEach { (ref, snapshot) ->
                        val item = sale.products.find { it.productId == snapshot.id }
                        if (item != null && snapshot.exists()) {
                            val currentStock = snapshot.getLong("stockQuantity") ?: 0
                            val newStock = (currentStock - item.quantity).coerceAtLeast(0)
                            transaction.update(ref, "stockQuantity", newStock)
                        }
                    }
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSales(): Result<List<Sale>> {
        return try {
            val snapshot = db.collection("vendas").get().await()
            val sales = snapshot.toObjects(Sale::class.java)
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSalesByUser(userId: String): Result<List<Sale>> {
        return try {
            val snapshot = db.collection("vendas")
                .whereEqualTo("userId", userId)
                .get().await()
            val sales = snapshot.toObjects(Sale::class.java)
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTicketsByUser(userId: String): Result<List<Ticket>> {
        return try {
            val snapshot = db.collection("ingressos")
                .whereEqualTo("userId", userId)
                .get().await()
            val tickets = snapshot.toObjects(Ticket::class.java)
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTicketsBySession(sessionId: String): Result<List<Ticket>> {
        return try {
            val snapshot = db.collection("ingressos")
                .whereEqualTo("sessionId", sessionId)
                .get().await()
            val tickets = snapshot.toObjects(Ticket::class.java)
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllTickets(): Result<List<Ticket>> {
        return try {
            val snapshot = db.collection("ingressos").get().await()
            val tickets = snapshot.toObjects(Ticket::class.java)
            Result.success(tickets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
