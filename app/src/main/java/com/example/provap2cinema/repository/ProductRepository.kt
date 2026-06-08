package com.example.provap2cinema.repository

import com.example.provap2cinema.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("produtos")

    suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            productsCollection.add(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val snapshot = productsCollection.get().await()
            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToProducts(onUpdate: (List<Product>) -> Unit): ListenerRegistration {
        return productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val products = snapshot?.toObjects(Product::class.java) ?: emptyList()
            onUpdate(products)
        }
    }

    suspend fun updateStock(productId: String, newQuantity: Int): Result<Unit> {
        return try {
            productsCollection.document(productId).update("stockQuantity", newQuantity).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            productsCollection.document(product.id).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
