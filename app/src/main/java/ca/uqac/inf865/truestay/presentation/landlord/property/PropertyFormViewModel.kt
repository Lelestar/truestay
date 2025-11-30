package ca.uqac.inf865.truestay.presentation.landlord.property

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.AddressAutocompleteSuggestion
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyStatus
import ca.uqac.inf865.truestay.domain.model.Room
import ca.uqac.inf865.truestay.domain.model.RoomElement
import ca.uqac.inf865.truestay.domain.model.RoomType
import ca.uqac.inf865.truestay.domain.model.RoomTypeElementMapping
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.GeocodingRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class RoomFormData(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: RoomType? = null,
    val elements: List<RoomElement> = emptyList() // Keep elements in edit mode
)

data class PropertyFormUiState(
    // General information
    val name: String = "",
    val addressQuery: String = "",
    val addressSuggestions: List<AddressAutocompleteSuggestion> = emptyList(),
    val selectedAddress: Address? = null,
    val description: String = "",
    val monthlyRent: String = "",
    val surface: String = "",
    val isInBuilding: Boolean = true,
    val isPublished: Boolean = true, // Controls status: PUBLISHED if true, PAUSED if false

    // Rooms
    val rooms: List<RoomFormData> = emptyList(),

    // Photos
    val photoUris: List<Uri> = emptyList(), // New photos to upload
    val existingPhotoUrls: List<String> = emptyList(), // Already uploaded photos (edit mode)
    val deletedPhotoUrls: List<String> = emptyList(), // Photos to delete from storage

    // Edit mode
    val propertyId: String? = null,
    val isEditMode: Boolean = false,
    val isLoadingProperty: Boolean = false,
    val createdAt: Long = 0L, // Keep creation date in edit mode

    // UI State
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isFormValid: Boolean = false
)

@HiltViewModel
class PropertyFormViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val storageRepository: StorageRepository,
    private val authRepository: AuthRepository,
    private val geocodingRepository: GeocodingRepository
) : ViewModel() {

    var uiState by mutableStateOf(
        PropertyFormUiState(
            rooms = listOf(RoomFormData()) // Add a default room
        )
    )
        private set

    private var suggestionsJob: Job? = null

    // General information
    fun setName(name: String) {
        uiState = uiState.copy(name = name)
        validateForm()
    }

    // Called when user types in address field
    fun onAddressQueryChanged(query: String) {
        uiState = uiState.copy(addressQuery = query)
        fetchAddressSuggestionsDebounced(query)
    }

    // Debounce to avoid too many API requests
    private fun fetchAddressSuggestionsDebounced(query: String) {
        suggestionsJob?.cancel()

        if (query.isBlank()) {
            uiState = uiState.copy(addressSuggestions = emptyList())
            return
        }

        suggestionsJob = viewModelScope.launch {
            delay(300) // Wait 300ms after last keystroke

            geocodingRepository.suggestAddresses(query, limit = 5).fold(
                onSuccess = { suggestions ->
                    uiState = uiState.copy(addressSuggestions = suggestions)
                },
                onFailure = {
                    uiState = uiState.copy(addressSuggestions = emptyList())
                }
            )
        }
    }

    // Called when user clicks on a suggestion
    fun onSuggestionSelected(suggestion: AddressAutocompleteSuggestion) {
        viewModelScope.launch {
            geocodingRepository.getAddressById(suggestion.placeId).fold(
                onSuccess = { address ->
                    address?.let { addr ->
                        android.util.Log.d("PropertyFormVM", "Parsed address: street=${addr.street}, city=${addr.city}, postalCode=${addr.postalCode}, province=${addr.province}, country=${addr.country}, lat=${addr.latitude}, lng=${addr.longitude}")
                        uiState = uiState.copy(
                            addressQuery = suggestion.primaryText,
                            selectedAddress = addr,
                            addressSuggestions = emptyList()
                        )
                        validateForm()
                    }
                },
                onFailure = {
                    android.util.Log.e("PropertyFormVM", "Error retrieving address: ${it.message}")
                    uiState = uiState.copy(addressSuggestions = emptyList())
                }
            )
        }
    }

    fun clearAddressSuggestions() {
        uiState = uiState.copy(addressSuggestions = emptyList())
    }


    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
        validateForm()
    }

    fun setMonthlyRent(rent: String) {
        // Keep only digits
        val filtered = rent.filter { it.isDigit() }
        uiState = uiState.copy(monthlyRent = filtered)
        validateForm()
    }

    fun setSurface(surface: String) {
        // Keep only digits
        val filtered = surface.filter { it.isDigit() }
        uiState = uiState.copy(surface = filtered)
        validateForm()
    }

    fun setIsInBuilding(isInBuilding: Boolean) {
        uiState = uiState.copy(isInBuilding = isInBuilding)
    }

    fun setIsPublished(isPublished: Boolean) {
        uiState = uiState.copy(isPublished = isPublished)
    }

    // Rooms
    fun addRoom() {
        uiState = uiState.copy(
            rooms = uiState.rooms + RoomFormData()
        )
        validateForm()
    }

    fun removeRoom(roomId: String) {
        uiState = uiState.copy(
            rooms = uiState.rooms.filter { it.id != roomId }
        )
        validateForm()
    }

    fun updateRoomName(roomId: String, name: String) {
        uiState = uiState.copy(
            rooms = uiState.rooms.map { room ->
                if (room.id == roomId) room.copy(name = name) else room
            }
        )
    }

    fun updateRoomType(roomId: String, type: RoomType) {
        uiState = uiState.copy(
            rooms = uiState.rooms.map { room ->
                if (room.id == roomId) room.copy(type = type) else room
            }
        )
        validateForm()
    }

    // Photos
    fun addPhotos(uris: List<Uri>) {
        android.util.Log.d("PropertyFormVM", "addPhotos called with ${uris.size} photos")
        val maxPhotos = 10
        val totalPhotos = uiState.photoUris.size
        val availableSlots = maxPhotos - totalPhotos
        val photosToAdd = uris.take(availableSlots)

        android.util.Log.d("PropertyFormVM", "Total photos before: $totalPhotos, to add: ${photosToAdd.size}")

        uiState = uiState.copy(
            photoUris = uiState.photoUris + photosToAdd
        )

        android.util.Log.d("PropertyFormVM", "Total photos after: ${uiState.photoUris.size}")
        validateForm()
    }

    fun removePhoto(uri: Uri) {
        uiState = uiState.copy(
            photoUris = uiState.photoUris.filter { it != uri }
        )
        validateForm()
    }

    fun removeExistingPhoto(url: String) {
        uiState = uiState.copy(
            existingPhotoUrls = uiState.existingPhotoUrls.filter { it != url },
            deletedPhotoUrls = uiState.deletedPhotoUrls + url
        )
        validateForm()
    }

    /**
     * Load existing property data for editing
     */
    fun loadProperty(propertyId: String) {
        android.util.Log.d("PropertyFormVM", "loadProperty called with propertyId=$propertyId")
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingProperty = true)

            try {
                android.util.Log.d("PropertyFormVM", "Fetching property from repository...")
                propertyRepository.getPropertyById(propertyId).fold(
                    onSuccess = { property ->
                        android.util.Log.d("PropertyFormVM", "Property retrieved: ${property.id}, name=${property.name}")

                        // Convert rooms to RoomFormData with their elements
                        val roomsData = property.rooms.map { room ->
                            android.util.Log.d("PropertyFormVM", "Room: id=${room.id}, name=${room.name}, type=${room.type}, elements=${room.elements.size}")
                            RoomFormData(
                                id = room.id,
                                name = room.name,
                                type = room.type,
                                elements = room.elements // Keep existing elements
                            )
                        }

                        android.util.Log.d("PropertyFormVM", "Existing photos: ${property.photos.size}")
                        property.photos.forEach { photo ->
                            android.util.Log.d("PropertyFormVM", "Photo URL: $photo")
                        }

                        uiState = uiState.copy(
                            propertyId = propertyId,
                            isEditMode = true,
                            name = property.name,
                            addressQuery = "${property.address.street}, ${property.address.city}",
                            selectedAddress = property.address,
                            description = property.description,
                            monthlyRent = property.monthlyRent.toString(),
                            surface = property.surface.toString(),
                            isInBuilding = property.isInBuilding,
                            // isPublished keeps default value, not loaded from property
                            rooms = roomsData.ifEmpty { listOf(RoomFormData()) },
                            existingPhotoUrls = property.photos,
                            createdAt = property.createdAt,
                            isLoadingProperty = false
                        )
                        android.util.Log.d("PropertyFormVM", "State updated successfully - isEditMode=${uiState.isEditMode}")
                        validateForm()
                    },
                    onFailure = { exception ->
                        android.util.Log.e("PropertyFormVM", "Error retrieving property: ${exception.message}", exception)
                        uiState = uiState.copy(
                            isLoadingProperty = false,
                            errorMessage = "Error loading property: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("PropertyFormVM", "Exception while loading: ${e.message}", e)
                uiState = uiState.copy(
                    isLoadingProperty = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Validate that the form is complete
     */
    private fun validateForm() {
        val totalPhotos = uiState.photoUris.size + uiState.existingPhotoUrls.size
        val isValid = uiState.name.isNotBlank() &&
                      uiState.selectedAddress != null &&
                      uiState.description.isNotBlank() &&
                      uiState.monthlyRent.isNotBlank() &&
                      uiState.surface.isNotBlank() &&
                      uiState.rooms.isNotEmpty() &&
                      uiState.rooms.all { it.type != null } && // Name is optional, only type is mandatory
                      totalPhotos >= 3 // Minimum 3 photos required

        uiState = uiState.copy(isFormValid = isValid)
    }

    /**
     * Upload all new photos and return complete list (existing + new)
     */
    private suspend fun uploadAllPhotos(propertyId: String): List<String> {
        val uploadedUrls = mutableListOf<String>()

        // Keep existing photos that haven't been deleted
        uploadedUrls.addAll(uiState.existingPhotoUrls)

        // Upload new photos
        uiState.photoUris.forEachIndexed { index, uri ->
            try {
                val path = "properties/$propertyId/${System.currentTimeMillis()}_$index"
                val result = storageRepository.uploadImage(uri, path)

                result.onSuccess { url ->
                    uploadedUrls.add(url)
                }.onFailure { exception ->
                    throw exception
                }
            } catch (e: Exception) {
                throw Exception("Failed to upload photo: ${e.message}", e)
            }
        }

        return uploadedUrls
    }

    /**
     * Get French name for room type
     */
    private fun getRoomTypeName(type: RoomType): String {
        return when (type) {
            RoomType.BEDROOM -> "Chambre"
            RoomType.LIVING_ROOM -> "Salon"
            RoomType.KITCHEN -> "Cuisine"
            RoomType.BATHROOM -> "Salle de bain"
            RoomType.TOILET -> "Toilettes"
            RoomType.ENTRANCE -> "Entrée"
            RoomType.HALLWAY -> "Couloir"
            RoomType.DINING_ROOM -> "Salle à manger"
            RoomType.OFFICE -> "Bureau"
            RoomType.LAUNDRY_ROOM -> "Buanderie"
            RoomType.STORAGE_ROOM -> "Débarras"
            RoomType.GARAGE -> "Garage"
            RoomType.BASEMENT -> "Sous-sol"
            RoomType.ATTIC -> "Grenier"
            RoomType.BALCONY -> "Balcon"
            RoomType.TERRACE -> "Terrasse"
            RoomType.GARDEN -> "Jardin"
            RoomType.VERANDA -> "Véranda"
            RoomType.STAIRCASE -> "Escalier"
            RoomType.OTHER -> "Autre"
        }
    }

    /**
     * Create Rooms with elements (existing in edit mode, default in creation mode)
     */
    private fun createRoomsWithElements(): List<Room> {
        return uiState.rooms.mapNotNull { roomData ->
            roomData.type?.let { type ->
                // In edit mode, use existing elements
                // In creation mode, create default elements
                val elements = if (uiState.isEditMode && roomData.elements.isNotEmpty()) {
                    roomData.elements
                } else {
                    RoomTypeElementMapping.getDefaultElementsForRoomType(type).map { elementType ->
                        RoomElement(
                            id = UUID.randomUUID().toString(),
                            type = elementType
                        )
                    }
                }

                Room(
                    id = if (uiState.isEditMode) roomData.id else UUID.randomUUID().toString(),
                    name = roomData.name.ifBlank { getRoomTypeName(type) }, // Use French type name if name is empty
                    type = type,
                    elements = elements
                )
            }
        }
    }

    /**
     * Submit the form and create/update the property
     */
    fun submitProperty(onSuccess: () -> Unit) {
        // Validate form before submitting
        validateForm()

        if (!uiState.isFormValid) {
            uiState = uiState.copy(errorMessage = "Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSubmitting = true, errorMessage = null)

            try {
                // Get current user
                val currentUser = authRepository.getCurrentUser().getOrThrow()
                    ?: throw IllegalStateException("User not logged in")

                // Use existing ID or generate a new one
                val propertyId = uiState.propertyId ?: UUID.randomUUID().toString()

                // Delete removed photos from storage (in edit mode)
                if (uiState.isEditMode && uiState.deletedPhotoUrls.isNotEmpty()) {
                    storageRepository.deleteImages(uiState.deletedPhotoUrls)
                        .onFailure { exception ->
                            android.util.Log.e("PropertyFormVM", "Error deleting photos: ${exception.message}")
                            // Continue anyway - deletion errors shouldn't block the update
                        }
                }

                // Upload photos (new + existing)
                val photoUrls = uploadAllPhotos(propertyId)

                // Create rooms with their elements
                val rooms = createRoomsWithElements()

                // Use selected address (already complete with all fields)
                val address = uiState.selectedAddress ?: Address()
                android.util.Log.d("PropertyFormVM", "Address used for property: street=${address.street}, city=${address.city}, postalCode=${address.postalCode}, province=${address.province}, country=${address.country}")

                // Create Property object
                val property = Property(
                    id = propertyId,
                    name = uiState.name,
                    description = uiState.description,
                    address = address,
                    monthlyRent = uiState.monthlyRent.toIntOrNull() ?: 0,
                    surface = uiState.surface.toIntOrNull() ?: 0,
                    rooms = rooms,
                    photos = photoUrls,
                    landlordId = currentUser.id,
                    isInBuilding = uiState.isInBuilding,
                    isAvailable = true, // Keep default value
                    status = if (uiState.isPublished) PropertyStatus.PUBLISHED else PropertyStatus.PAUSED,
                    createdAt = if (uiState.isEditMode) uiState.createdAt else System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                // Save to Firestore (add or update)
                val result = if (uiState.isEditMode) {
                    propertyRepository.updateProperty(property)
                } else {
                    propertyRepository.addProperty(property)
                }

                result.onSuccess {
                    uiState = uiState.copy(isSubmitting = false)
                    onSuccess()
                }.onFailure { exception ->
                    uiState = uiState.copy(
                        isSubmitting = false,
                        errorMessage = "Error during publication: ${exception.message}"
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSubmitting = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }
}