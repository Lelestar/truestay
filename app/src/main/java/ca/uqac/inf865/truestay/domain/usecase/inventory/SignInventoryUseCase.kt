package ca.uqac.inf865.truestay.domain.usecase.inventory

import ca.uqac.inf865.truestay.domain.model.InventoryStatus
import ca.uqac.inf865.truestay.domain.model.Signature
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import javax.inject.Inject

class SignInventoryUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val rentalRepository: RentalRepository
) {
    /**
     * Signs an inventory with the provided signature.
     *
     * @param inventoryId The ID of the inventory to be signed.
     * @param userId The ID of the user signing the inventory.
     * @param signatureUrl The URL of the signature image.
     * @return A [Result] containing [Unit] if successful, or an [Exception] if an error occurs.
     */
    suspend operator fun invoke(
        inventoryId: String,
        userId: String,
        signatureUrl: String
    ): Result<Unit> {
        return try {
            // 1. Get the inventory
            val inventory = inventoryRepository.getInventoryById(inventoryId).getOrNull()
                ?: return Result.failure(Exception("Inventory not found"))

            // 2. Get the rental associated with the inventory
            val rental = rentalRepository.getRentalById(inventory.rentalId).getOrNull()
                ?: return Result.failure(Exception("Rental not found"))

            // 3. Determine the role of the user
            val isLandlord = userId == rental.landlordId
            val isTenant = userId == rental.tenantId

            if (!isLandlord && !isTenant) {
                return Result.failure(Exception("User is neither landlord nor tenant associated with this inventory"))
            }

            // 4. Check if already signed
            if (isLandlord && inventory.landlordSignature != null) {
                return Result.failure(Exception("Landlord has already signed this inventory"))
            }
            if (isTenant && inventory.tenantSignature != null) {
                return Result.failure(Exception("Tenant has already signed this inventory"))
            }

            // 5. Create the signature object
            val signature = Signature(
                userId = userId,
                signatureImageUrl = signatureUrl,
                signedAt = System.currentTimeMillis()
            )

            // 6. Update the inventory with the signature
            val updatedInventory = if (isLandlord) {
                inventory.copy(landlordSignature = signature)
            } else {
                inventory.copy(tenantSignature = signature)
            }

            // 7. If both signatures are present, mark as signed
            val finalInventory = if (updatedInventory.landlordSignature != null &&
                updatedInventory.tenantSignature != null) {
                updatedInventory.copy(status = InventoryStatus.SIGNED)
            } else {
                updatedInventory
            }

            // 8. Save
            inventoryRepository.updateInventory(finalInventory).getOrThrow()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}