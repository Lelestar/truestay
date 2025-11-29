package ca.uqac.inf865.truestay.domain.usecase.inventory

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import ca.uqac.inf865.truestay.domain.model.InventoryStatus
import ca.uqac.inf865.truestay.domain.model.Signature
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class SignInventoryUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val rentalRepository: RentalRepository,
    private val storageRepository: StorageRepository,
    @param:ApplicationContext private val context: Context
) {
    /**
     * Signs an inventory with the provided signature bitmap.
     *
     * @param inventoryId The ID of the inventory to be signed.
     * @param userId The ID of the user signing the inventory.
     * @param signatureBitmap The signature image as a Bitmap.
     * @return A [Result] containing [Unit] if successful, or an [Exception] if an error occurs.
     */
    suspend operator fun invoke(
        inventoryId: String,
        userId: String,
        signatureBitmap: Bitmap
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

            // 5. Upload signature image
            val signatureUrl = uploadSignature(inventoryId, userId, signatureBitmap)

            // 6. Create the signature object
            val signature = Signature(
                userId = userId,
                signatureImageUrl = signatureUrl,
                signedAt = System.currentTimeMillis()
            )

            // 7. Update the inventory with the signature
            val updatedInventory = if (isLandlord) {
                inventory.copy(landlordSignature = signature)
            } else {
                inventory.copy(tenantSignature = signature)
            }

            // 8. Update status based on signatures present
            val bothSignaturesPresent = updatedInventory.landlordSignature != null &&
                updatedInventory.tenantSignature != null

            val finalInventory = when {
                // Both signatures are present -> mark as SIGNED
                bothSignaturesPresent -> {
                    updatedInventory.copy(status = InventoryStatus.SIGNED)
                }
                // Only one signature is present -> mark as PENDING_SIGNATURE
                updatedInventory.landlordSignature != null || updatedInventory.tenantSignature != null -> {
                    updatedInventory.copy(status = InventoryStatus.PENDING_SIGNATURE)
                }
                // No signature (should not happen at this point)
                else -> updatedInventory
            }

            // 9. Save
            inventoryRepository.updateInventory(finalInventory).getOrThrow()

            // PDF generation is automatically triggered by Cloud Function
            // when status changes to SIGNED with both signatures present

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadSignature(inventoryId: String, userId: String, bitmap: Bitmap): String {
        // Create temp file
        val file = File(context.cacheDir, "signature_${inventoryId}_${userId}.png")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()

        val uri = Uri.fromFile(file)
        val path = "signatures/$inventoryId/${userId}_${System.currentTimeMillis()}.png"

        return storageRepository.uploadImage(uri, path).getOrThrow()
    }
}
