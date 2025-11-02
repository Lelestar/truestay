package ca.uqac.inf865.truestay.domain.usecase.inventory

import ca.uqac.inf865.truestay.domain.model.InventoryStatus
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import javax.inject.Inject

class GenerateInventoryPdfUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository
) {
    /**
     * Generates a PDF for the specified inventory.
     *
     * @param inventoryId The ID of the inventory for which the PDF is to be generated.
     * @return A [Result] containing the URL of the generated PDF if successful, or an [Exception] if an error occurs.
     */
    suspend operator fun invoke(inventoryId: String): Result<String> {
        return try {
            // 1. Get the inventory
            val inventory = inventoryRepository.getInventoryById(inventoryId).getOrNull()
                ?: return Result.failure(Exception("Inventory not found"))

            // 2. VALIDATION : Check signatures
            if (inventory.landlordSignature == null || inventory.tenantSignature == null) {
                return Result.failure(Exception("Both landlord and tenant must sign the inventory before generating the PDF"))
            }

            // 3. TODO: Generate PDF file and upload to storage
            // A Firebase Cloud Function will be used

            // For now, we will simulate the PDF URL
            val pdfUrl = "https://example.com/inventories/${inventoryId}.pdf"

            // 5. Update inventory with PDF URL and update status to COMPLETED
            val updatedInventory = inventory.copy(
                pdfUrl = pdfUrl,
                status = InventoryStatus.COMPLETED,
                completedAt = System.currentTimeMillis()
            )
            inventoryRepository.updateInventory(updatedInventory).getOrThrow()

            Result.success(pdfUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}