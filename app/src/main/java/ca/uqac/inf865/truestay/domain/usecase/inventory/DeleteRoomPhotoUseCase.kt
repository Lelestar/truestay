package ca.uqac.inf865.truestay.domain.usecase.inventory

import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import javax.inject.Inject

/**
 * Deletes a photo from a room and removes it from the inventory.
 *
 * @return Result<Unit> on success.
 */
class DeleteRoomPhotoUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
    private val inventoryRepository: InventoryRepository
) {

    suspend operator fun invoke(
        inventoryId: String,
        roomId: String,
        photoUrl: String
    ): Result<Unit> {
        return try {
            // Get current inventory
            val inventory = inventoryRepository.getInventoryById(inventoryId).getOrThrow()

            // Find the room and remove the photo URL directly from room.photoUrls
            val updatedRooms = inventory.rooms.map { room ->
                if (room.roomId == roomId) {
                    room.copy(photoUrls = room.photoUrls.filterNot { it == photoUrl })
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
