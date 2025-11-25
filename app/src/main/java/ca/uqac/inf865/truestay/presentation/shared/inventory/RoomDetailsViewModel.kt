package ca.uqac.inf865.truestay.presentation.shared.inventory

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.ElementCondition
import ca.uqac.inf865.truestay.domain.model.Inventory
import ca.uqac.inf865.truestay.domain.model.InventoryRoom
import ca.uqac.inf865.truestay.domain.model.RoomInventoryStatus
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.InventoryRepository
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.usecase.inventory.AddElementPhotoUseCase
import ca.uqac.inf865.truestay.domain.usecase.inventory.AddRoomPhotoUseCase
import ca.uqac.inf865.truestay.domain.usecase.inventory.DeleteElementPhotoUseCase
import ca.uqac.inf865.truestay.domain.usecase.inventory.DeleteRoomPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Error states for room details operations
 */
enum class RoomDetailsError {
    LOAD_FAILED
}

/**
 * UI state for room details screen
 *
 * Key business rules:
 * - Elements with TO_CHECK condition cannot have comments or photos added until a condition is selected
 * - DAMAGED elements require both a comment AND at least one photo to be considered complete
 * - Room status automatically updates based on element completion progress
 * - Tenants have read-only access and can only view elements with existing content
 */
data class RoomDetailsUiState(
    val inventory: Inventory? = null,
    val room: InventoryRoom? = null,
    val currentRoomIndex: Int = 0,
    val totalRooms: Int = 0,
    val currentUserId: String = "",
    val landlordId: String = "",
    val tenantId: String = "",
    val photoUrls: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSavingPhoto: Boolean = false,
    val error: RoomDetailsError? = null,
    val photoUploadError: String? = null,
    val photoDeleteError: String? = null,
    val elementErrors: Map<String, String> = emptyMap(), // elementId -> error message
    val expandedElementId: String? = null,
    val completedElements: Int = 0,
    val totalElements: Int = 0
)

@HiltViewModel
class RoomDetailsViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val rentalRepository: RentalRepository,
    private val authRepository: AuthRepository,
    private val addRoomPhotoUseCase: AddRoomPhotoUseCase,
    private val deleteRoomPhotoUseCase: DeleteRoomPhotoUseCase,
    private val addElementPhotoUseCase: AddElementPhotoUseCase,
    private val deleteElementPhotoUseCase: DeleteElementPhotoUseCase
) : ViewModel() {

    var uiState by mutableStateOf(RoomDetailsUiState(isLoading = true))
        private set

    fun loadRoomDetails(inventoryId: String, roomId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            inventoryRepository.getInventoryById(inventoryId)
                .onSuccess { inventory ->
                    val room = inventory.rooms.find { it.roomId == roomId }
                    if (room != null) {
                        val roomIndex = inventory.rooms.indexOfFirst { it.roomId == roomId }

                        // Get current user and rental info
                        val currentUser = authRepository.getCurrentUser().getOrNull()
                        val rental = rentalRepository.getRentalById(inventory.rentalId).getOrNull()

                        // Calculate completed elements based on validation rules:
                        // - TO_CHECK: Not completed (must select a condition)
                        // - DAMAGED: Requires both comment AND at least one photo
                        // - GOOD/NOT_APPLICABLE: Completed immediately
                        val totalElements = room.elements.size
                        val completedElements = room.elements.count { element ->
                            when (element.condition) {
                                ElementCondition.TO_CHECK -> false
                                ElementCondition.DAMAGED -> {
                                    element.comment.isNotBlank() && element.photoUrls.isNotEmpty()
                                }
                                else -> true
                            }
                        }

                        uiState = uiState.copy(
                            inventory = inventory,
                            room = room,
                            currentRoomIndex = roomIndex,
                            totalRooms = inventory.rooms.size,
                            currentUserId = currentUser?.id.orEmpty(),
                            landlordId = rental?.landlordId.orEmpty(),
                            tenantId = rental?.tenantId.orEmpty(),
                            photoUrls = room.photoUrls,
                            isLoading = false,
                            error = null,
                            completedElements = completedElements,
                            totalElements = totalElements
                        )
                    } else {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = RoomDetailsError.LOAD_FAILED
                        )
                    }
                }
                .onFailure {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = RoomDetailsError.LOAD_FAILED
                    )
                }
        }
    }

    fun addPhoto(uri: Uri) {
        val inventory = uiState.inventory ?: return
        val room = uiState.room ?: return

        viewModelScope.launch {
            uiState = uiState.copy(isSavingPhoto = true, photoUploadError = null)

            addRoomPhotoUseCase(inventory.id, room.roomId, uri)
                .onSuccess { photoUrl ->
                    val newPhotoUrls = uiState.photoUrls + photoUrl
                    uiState = uiState.copy(
                        photoUrls = newPhotoUrls,
                        isSavingPhoto = false,
                        photoUploadError = null
                    )
                }
                .onFailure { exception ->
                    uiState = uiState.copy(
                        isSavingPhoto = false,
                        photoUploadError = exception.message ?: "Failed to upload photo"
                    )
                }
        }
    }

    fun deletePhoto(photoUrl: String) {
        val inventory = uiState.inventory ?: return
        val room = uiState.room ?: return

        viewModelScope.launch {
            uiState = uiState.copy(photoDeleteError = null)

            deleteRoomPhotoUseCase(inventory.id, room.roomId, photoUrl)
                .onSuccess {
                    val newPhotoUrls = uiState.photoUrls.filterNot { it == photoUrl }
                    uiState = uiState.copy(
                        photoUrls = newPhotoUrls,
                        photoDeleteError = null
                    )
                }
                .onFailure { exception ->
                    uiState = uiState.copy(
                        photoDeleteError = exception.message ?: "Failed to delete photo"
                    )
                }
        }
    }

    fun toggleElementExpanded(elementId: String) {
        uiState = uiState.copy(
            expandedElementId = if (uiState.expandedElementId == elementId) null else elementId
        )
    }

    /**
     * Updates an element's condition and recalculates completion status
     *
     * Triggers automatic room status update after successful save.
     */
    fun updateElementCondition(elementId: String, condition: ElementCondition) {
        val inventory = uiState.inventory ?: return
        val room = uiState.room ?: return

        viewModelScope.launch {
            // Clear any existing error for this element
            val errors = uiState.elementErrors.toMutableMap()
            errors.remove(elementId)
            uiState = uiState.copy(elementErrors = errors)

            // Update element condition
            val updatedElements = room.elements.map { element ->
                if (element.elementId == elementId) {
                    element.copy(condition = condition)
                } else {
                    element
                }
            }

            val updatedRoom = room.copy(elements = updatedElements)
            val updatedRooms = inventory.rooms.map { r ->
                if (r.roomId == room.roomId) updatedRoom else r
            }

            val updatedInventory = inventory.copy(rooms = updatedRooms)

            inventoryRepository.updateInventory(updatedInventory)
                .onSuccess {
                    // Recalculate completed elements after update
                    val totalElements = updatedRoom.elements.size
                    val completedElements = updatedRoom.elements.count { element ->
                        when (element.condition) {
                            ElementCondition.TO_CHECK -> false
                            ElementCondition.DAMAGED -> {
                                element.comment.isNotBlank() && element.photoUrls.isNotEmpty()
                            }
                            else -> true
                        }
                    }

                    uiState = uiState.copy(
                        inventory = updatedInventory,
                        room = updatedRoom,
                        completedElements = completedElements,
                        totalElements = totalElements
                    )
                    updateRoomStatus()
                }
                .onFailure { exception ->
                    errors[elementId] = exception.message ?: "Update failed"
                    uiState = uiState.copy(elementErrors = errors)
                }
        }
    }

    /**
     * Updates an element's comment and recalculates completion status
     *
     * For DAMAGED elements, comment is required for completion (along with at least one photo).
     * Triggers automatic room status update after successful save.
     */
    fun updateElementComment(elementId: String, comment: String) {
        val inventory = uiState.inventory ?: return
        val room = uiState.room ?: return

        viewModelScope.launch {
            // Clear any existing error for this element
            val errors = uiState.elementErrors.toMutableMap()
            errors.remove(elementId)
            uiState = uiState.copy(elementErrors = errors)

            // Update element comment
            val updatedElements = room.elements.map { element ->
                if (element.elementId == elementId) {
                    element.copy(comment = comment)
                } else {
                    element
                }
            }

            val updatedRoom = room.copy(elements = updatedElements)
            val updatedRooms = inventory.rooms.map { r ->
                if (r.roomId == room.roomId) updatedRoom else r
            }

            val updatedInventory = inventory.copy(rooms = updatedRooms)

            inventoryRepository.updateInventory(updatedInventory)
                .onSuccess {
                    // Recalculate completed elements (comment affects completion for DAMAGED elements)
                    val totalElements = updatedRoom.elements.size
                    val completedElements = updatedRoom.elements.count { element ->
                        when (element.condition) {
                            ElementCondition.TO_CHECK -> false
                            ElementCondition.DAMAGED -> {
                                element.comment.isNotBlank() && element.photoUrls.isNotEmpty()
                            }
                            else -> true
                        }
                    }

                    uiState = uiState.copy(
                        inventory = updatedInventory,
                        room = updatedRoom,
                        completedElements = completedElements,
                        totalElements = totalElements
                    )
                    updateRoomStatus()
                }
                .onFailure { exception ->
                    errors[elementId] = exception.message ?: "Update failed"
                    uiState = uiState.copy(elementErrors = errors)
                }
        }
    }

    /**
     * Adds a photo to an element and recalculates completion status
     *
     * For DAMAGED elements, at least one photo is required for completion (along with a comment).
     * Refreshes the entire inventory after upload to get the updated photo URL.
     * Triggers automatic room status update after successful save.
     */
    fun addElementPhoto(elementId: String, uri: Uri) {
        val inventory = uiState.inventory ?: return
        val room = uiState.room ?: return

        viewModelScope.launch {
            // Clear any existing error for this element
            val errors = uiState.elementErrors.toMutableMap()
            errors.remove(elementId)
            uiState = uiState.copy(elementErrors = errors, isSavingPhoto = true)

            addElementPhotoUseCase(inventory.id, room.roomId, elementId, uri)
                .onSuccess { photoUrl ->
                    // Refresh the inventory to get updated data
                    inventoryRepository.getInventoryById(inventory.id)
                        .onSuccess { updatedInventory ->
                            val updatedRoom = updatedInventory.rooms.find { it.roomId == room.roomId }

                            // Recalculate completed elements
                            val totalElements = updatedRoom?.elements?.size ?: 0
                            val completedElements = updatedRoom?.elements?.count { element ->
                                when (element.condition) {
                                    ElementCondition.TO_CHECK -> false
                                    ElementCondition.DAMAGED -> {
                                        element.comment.isNotBlank() && element.photoUrls.isNotEmpty()
                                    }
                                    else -> true
                                }
                            } ?: 0

                            uiState = uiState.copy(
                                inventory = updatedInventory,
                                room = updatedRoom,
                                isSavingPhoto = false,
                                completedElements = completedElements,
                                totalElements = totalElements
                            )
                            updateRoomStatus()
                        }
                        .onFailure { exception ->
                            errors[elementId] = exception.message ?: "Failed to refresh data"
                            uiState = uiState.copy(elementErrors = errors, isSavingPhoto = false)
                        }
                }
                .onFailure { exception ->
                    errors[elementId] = exception.message ?: "Failed to upload photo"
                    uiState = uiState.copy(elementErrors = errors, isSavingPhoto = false)
                }
        }
    }

    fun deleteElementPhoto(elementId: String, photoUrl: String) {
        val inventory = uiState.inventory ?: return
        val room = uiState.room ?: return

        viewModelScope.launch {
            // Clear error for this element
            val errors = uiState.elementErrors.toMutableMap()
            errors.remove(elementId)
            uiState = uiState.copy(elementErrors = errors)

            deleteElementPhotoUseCase(inventory.id, room.roomId, elementId, photoUrl)
                .onSuccess {
                    // Refresh the inventory to get updated data
                    inventoryRepository.getInventoryById(inventory.id)
                        .onSuccess { updatedInventory ->
                            val updatedRoom = updatedInventory.rooms.find { it.roomId == room.roomId }

                            // Recalculate completed elements
                            val totalElements = updatedRoom?.elements?.size ?: 0
                            val completedElements = updatedRoom?.elements?.count { element ->
                                when (element.condition) {
                                    ElementCondition.TO_CHECK -> false
                                    ElementCondition.DAMAGED -> {
                                        element.comment.isNotBlank() && element.photoUrls.isNotEmpty()
                                    }
                                    else -> true
                                }
                            } ?: 0

                            uiState = uiState.copy(
                                inventory = updatedInventory,
                                room = updatedRoom,
                                completedElements = completedElements,
                                totalElements = totalElements
                            )
                            updateRoomStatus()
                        }
                        .onFailure { exception ->
                            errors[elementId] = exception.message ?: "Failed to refresh data"
                            uiState = uiState.copy(elementErrors = errors)
                        }
                }
                .onFailure { exception ->
                    errors[elementId] = exception.message ?: "Failed to delete photo"
                    uiState = uiState.copy(elementErrors = errors)
                }
        }
    }

    /**
     * Automatically updates room status based on element completion progress
     *
     * Status transitions:
     * - _TODO: No elements completed yet
     * - IN_PROGRESS: At least one element completed, but not all
     * - COMPLETED: All elements completed
     *
     * Status can transition in any direction (including from COMPLETED back to IN_PROGRESS)
     * if elements are modified after completion.
     */
    private fun updateRoomStatus() {
        val inventory = uiState.inventory ?: return
        val room = uiState.room ?: return

        // Determine new status based on completion progress
        val newStatus = when (uiState.completedElements) {
            0 -> RoomInventoryStatus.TODO
            uiState.totalElements -> RoomInventoryStatus.COMPLETED
            else -> RoomInventoryStatus.IN_PROGRESS
        }

        // Update if status changed
        if (room.status != newStatus) {
            viewModelScope.launch {
                val updatedRoom = room.copy(status = newStatus)
                val updatedRooms = inventory.rooms.map { r ->
                    if (r.roomId == room.roomId) updatedRoom else r
                }

                val updatedInventory = inventory.copy(rooms = updatedRooms)

                inventoryRepository.updateInventory(updatedInventory)
                    .onSuccess {
                        uiState = uiState.copy(
                            inventory = updatedInventory,
                            room = updatedRoom
                        )
                    }
            }
        }
    }

    fun retry(inventoryId: String, roomId: String) {
        loadRoomDetails(inventoryId, roomId)
    }
}
