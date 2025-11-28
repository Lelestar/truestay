package ca.uqac.inf865.truestay.presentation.landlord.property

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyStatus
import ca.uqac.inf865.truestay.domain.model.Room
import ca.uqac.inf865.truestay.domain.model.RoomElement
import ca.uqac.inf865.truestay.domain.model.RoomType
import ca.uqac.inf865.truestay.domain.model.RoomTypeElementMapping
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class RoomFormData(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: RoomType? = null
)

data class PropertyFormUiState(
    // Informations générales
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val monthlyRent: String = "",
    val surface: String = "",
    val isInBuilding: Boolean = true,
    val isAvailable: Boolean = true,

    // Pièces
    val rooms: List<RoomFormData> = emptyList(),

    // Photos
    val photoUris: List<Uri> = emptyList(),

    // UI State
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PropertyFormViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val storageRepository: StorageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(PropertyFormUiState())
        private set

    // Informations générales
    fun setName(name: String) {
        uiState = uiState.copy(name = name)
    }

    fun setAddress(address: String) {
        uiState = uiState.copy(address = address)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    fun setMonthlyRent(rent: String) {
        // Ne garder que les chiffres
        val filtered = rent.filter { it.isDigit() }
        uiState = uiState.copy(monthlyRent = filtered)
    }

    fun setSurface(surface: String) {
        // Ne garder que les chiffres
        val filtered = surface.filter { it.isDigit() }
        uiState = uiState.copy(surface = filtered)
    }

    fun setIsInBuilding(isInBuilding: Boolean) {
        uiState = uiState.copy(isInBuilding = isInBuilding)
    }

    fun setIsAvailable(isAvailable: Boolean) {
        uiState = uiState.copy(isAvailable = isAvailable)
    }

    // Pièces
    fun addRoom() {
        uiState = uiState.copy(
            rooms = uiState.rooms + RoomFormData()
        )
    }

    fun removeRoom(roomId: String) {
        uiState = uiState.copy(
            rooms = uiState.rooms.filter { it.id != roomId }
        )
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
    }

    // Photos
    fun addPhotos(uris: List<Uri>) {
        val maxPhotos = 10
        val totalPhotos = uiState.photoUris.size
        val availableSlots = maxPhotos - totalPhotos
        val photosToAdd = uris.take(availableSlots)

        uiState = uiState.copy(
            photoUris = uiState.photoUris + photosToAdd
        )
    }

    fun removePhoto(uri: Uri) {
        uiState = uiState.copy(
            photoUris = uiState.photoUris.filter { it != uri }
        )
    }

    /**
     * Valide que le formulaire est complet
     */
    private fun validateForm(): Boolean {
        return uiState.name.isNotBlank() &&
               uiState.address.isNotBlank() &&
               uiState.description.isNotBlank() &&
               uiState.monthlyRent.isNotBlank() &&
               uiState.surface.isNotBlank() &&
               uiState.rooms.isNotEmpty() &&
               uiState.rooms.all { it.type != null } &&
               uiState.photoUris.size >= 3
    }

    /**
     * Upload toutes les photos en attente
     */
    private suspend fun uploadAllPhotos(propertyId: String): List<String> {
        val uploadedUrls = mutableListOf<String>()

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
     * Crée les Room avec les éléments par défaut basés sur le type de pièce
     */
    private fun createRoomsWithElements(): List<Room> {
        return uiState.rooms.mapNotNull { roomData ->
            roomData.type?.let { type ->
                val elements = RoomTypeElementMapping.getDefaultElementsForRoomType(type).map { elementType ->
                    RoomElement(
                        id = UUID.randomUUID().toString(),
                        type = elementType
                    )
                }

                Room(
                    id = UUID.randomUUID().toString(),
                    name = roomData.name.ifBlank { null } ?: "",
                    type = type,
                    elements = elements
                )
            }
        }
    }

    /**
     * Soumet le formulaire et crée la propriété
     */
    fun submitProperty(onSuccess: () -> Unit) {
        if (!validateForm()) {
            uiState = uiState.copy(errorMessage = "Veuillez remplir tous les champs obligatoires")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSubmitting = true, errorMessage = null)

            try {
                // Obtenir l'utilisateur actuel
                val currentUser = authRepository.getCurrentUser().getOrThrow()
                    ?: throw IllegalStateException("Utilisateur non connecté")

                // Générer un ID temporaire pour la propriété
                val propertyId = UUID.randomUUID().toString()

                // Upload des photos
                val photoUrls = uploadAllPhotos(propertyId)

                // Créer les pièces avec leurs éléments
                val rooms = createRoomsWithElements()

                // Créer l'objet Property
                val property = Property(
                    id = propertyId,
                    name = uiState.name,
                    description = uiState.description,
                    address = Address(
                        street = uiState.address
                        // Les autres champs d'adresse pourraient être parsés ou laissés vides
                    ),
                    monthlyRent = uiState.monthlyRent.toIntOrNull() ?: 0,
                    surface = uiState.surface.toIntOrNull() ?: 0,
                    rooms = rooms,
                    photos = photoUrls,
                    landlordId = currentUser.id,
                    isInBuilding = uiState.isInBuilding,
                    isAvailable = uiState.isAvailable,
                    status = PropertyStatus.PUBLISHED,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                // Sauvegarder dans Firestore
                val result = propertyRepository.addProperty(property)

                result.onSuccess {
                    uiState = uiState.copy(isSubmitting = false)
                    onSuccess()
                }.onFailure { exception ->
                    uiState = uiState.copy(
                        isSubmitting = false,
                        errorMessage = "Erreur lors de la publication: ${exception.message}"
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isSubmitting = false,
                    errorMessage = "Erreur: ${e.message}"
                )
            }
        }
    }
}