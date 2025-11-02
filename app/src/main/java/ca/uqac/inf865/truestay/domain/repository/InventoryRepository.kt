package ca.uqac.inf865.truestay.domain.repository

import ca.uqac.inf865.truestay.domain.model.Inventory

interface InventoryRepository {
    suspend fun createInventory(inventory: Inventory): Result<String>
    suspend fun getInventoryById(id: String): Result<Inventory>
    suspend fun updateInventory(inventory: Inventory): Result<Unit>
}