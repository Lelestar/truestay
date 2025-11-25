package ca.uqac.inf865.truestay.domain.usecase.inventory

import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import javax.inject.Inject

/**
 * Deletes a photo from an element and removes it from the inventory.
 *
 * @return Result<Unit> on success.
 */
class DeleteElementPhotoUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
    private val inventoryRepository: InventoryRepository
) {

    suspend operator fun invoke(
        inventoryId: String,
        roomId: String,
        elementId: String,
        photoUrl: String
    ): Result<Unit> {
        return try {
            // Get current inventory
            val inventory = inventoryRepository.getInventoryById(inventoryId).getOrThrow()

            // Find the room and element, then remove the photo URL
            val updatedRooms = inventory.rooms.map { room ->
                if (room.roomId == roomId) {
                    val updatedElements = room.elements.map { element ->
                        if (element.elementId == elementId) {
                            element.copy(photoUrls = element.photoUrls.filterNot { it == photoUrl })
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

            // Delete from storage
            storageRepository.deleteImage(photoUrl).getOrThrow()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
