package ca.uqac.inf865.truestay.domain.usecase.inventory

import android.net.Uri
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import javax.inject.Inject

/**
 * Uploads a photo for an element and adds it to the inventory.
 *
 * @return Result containing the new photo URL on success.
 */
class AddElementPhotoUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
    private val inventoryRepository: InventoryRepository
) {

    suspend operator fun invoke(
        inventoryId: String,
        roomId: String,
        elementId: String,
        imageUri: Uri
    ): Result<String> {
        return try {
            // Upload image to storage
            val path = "inventories/$inventoryId/rooms/$roomId/elements/$elementId/${System.currentTimeMillis()}"
            val url = storageRepository.uploadImage(imageUri, path).getOrThrow()

            // Get current inventory
            val inventory = inventoryRepository.getInventoryById(inventoryId).getOrThrow()

            // Find the room and element, then add the photo URL
            val updatedRooms = inventory.rooms.map { room ->
                if (room.roomId == roomId) {
                    val updatedElements = room.elements.map { element ->
                        if (element.elementId == elementId) {
                            element.copy(photoUrls = element.photoUrls + url)
                        } else {
                            element
                        }
                    }
                    room.copy(elements = updatedElements)
                } else {
                    room
                }
            }

            val updatedInventory = inventory.copy(rooms = updatedRooms)
            inventoryRepository.updateInventory(updatedInventory).getOrThrow()

            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
