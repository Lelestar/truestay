package ca.uqac.inf865.truestay.data.repository

import ca.uqac.inf865.truestay.data.model.InventoryDto
import ca.uqac.inf865.truestay.data.model.SignatureDto
import ca.uqac.inf865.truestay.data.model.toDto
import ca.uqac.inf865.truestay.data.model.toDomain
import ca.uqac.inf865.truestay.data.source.FirestoreDataSource
import ca.uqac.inf865.truestay.domain.model.Inventory
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource
) : InventoryRepository {

    override suspend fun createInventory(inventory: Inventory): Result<String> {
        return try {
            val inventoryDto = inventory.toDto().copy(createdAt = System.currentTimeMillis())
            val id = firestoreDataSource.addDocument("inventories", inventoryDto)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getInventoryById(id: String): Result<Inventory> {
        return try {
            val inventory = firestoreDataSource.getDocument("inventories", id, InventoryDto::class.java)
                ?: return Result.failure(Exception("Inventory not found"))
            Result.success(inventory.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateInventory(inventory: Inventory): Result<Unit> {
        return try {
            firestoreDataSource.updateDocument("inventories", inventory.id, inventory.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeInventory(id: String): Flow<Inventory?> {
        return firestoreDataSource.observeDocument("inventories", id, InventoryDto::class.java)
            .map { it?.toDomain() }
    }
}