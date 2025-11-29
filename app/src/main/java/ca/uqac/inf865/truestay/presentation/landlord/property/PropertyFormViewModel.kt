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
    val type: RoomType? = null
)

data class PropertyFormUiState(
    // Informations générales
    val name: String = "",
    val addressQuery: String = "",
    val addressSuggestions: List<AddressAutocompleteSuggestion> = emptyList(),
    val selectedAddress: Address? = null,
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
    private val authRepository: AuthRepository,
    private val geocodingRepository: GeocodingRepository
) : ViewModel() {

    var uiState by mutableStateOf(
        PropertyFormUiState(
            rooms = listOf(RoomFormData()) // Ajouter une pièce par défaut
        )
    )
        private set

    private var suggestionsJob: Job? = null

    // Informations générales
    fun setName(name: String) {
        uiState = uiState.copy(name = name)
    }

    // Appelé quand l'utilisateur tape dans le champ d'adresse
    fun onAddressQueryChanged(query: String) {
        uiState = uiState.copy(addressQuery = query)
        fetchAddressSuggestionsDebounced(query)
    }

    // Debounce pour éviter trop de requêtes API
    private fun fetchAddressSuggestionsDebounced(query: String) {
        suggestionsJob?.cancel()

        if (query.isBlank()) {
            uiState = uiState.copy(addressSuggestions = emptyList())
            return
        }

        suggestionsJob = viewModelScope.launch {
            delay(300) // Attendre 300ms après la dernière frappe

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

    // Appelé quand l'utilisateur clique sur une suggestion
    fun onSuggestionSelected(suggestion: AddressAutocompleteSuggestion) {
        viewModelScope.launch {
            geocodingRepository.getAddressById(suggestion.placeId).fold(
                onSuccess = { address ->
                    address?.let { addr ->
                        android.util.Log.d("PropertyFormVM", "Adresse parsée: street=${addr.street}, city=${addr.city}, postalCode=${addr.postalCode}, province=${addr.province}, country=${addr.country}, lat=${addr.latitude}, lng=${addr.longitude}")
                        uiState = uiState.copy(
                            addressQuery = suggestion.primaryText,
                            selectedAddress = addr,
                            addressSuggestions = emptyList()
                        )
                    }
                },
                onFailure = {
                    android.util.Log.e("PropertyFormVM", "Erreur lors de la récupération de l'adresse: ${it.message}")
                    uiState = uiState.copy(addressSuggestions = emptyList())
                }
            )
        }
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
        android.util.Log.d("PropertyFormVM", "addPhotos appelé avec ${uris.size} photos")
        val maxPhotos = 10
        val totalPhotos = uiState.photoUris.size
        val availableSlots = maxPhotos - totalPhotos
        val photosToAdd = uris.take(availableSlots)

        android.util.Log.d("PropertyFormVM", "Total photos avant: $totalPhotos, à ajouter: ${photosToAdd.size}")

        uiState = uiState.copy(
            photoUris = uiState.photoUris + photosToAdd
        )

        android.util.Log.d("PropertyFormVM", "Total photos après: ${uiState.photoUris.size}")
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
               uiState.selectedAddress != null &&
               uiState.description.isNotBlank() &&
               uiState.monthlyRent.isNotBlank() &&
               uiState.surface.isNotBlank() &&
               uiState.rooms.isNotEmpty() &&
               uiState.rooms.all { it.name.isNotBlank() && it.type != null } &&
               uiState.photoUris.isNotEmpty()
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

                // Utiliser l'adresse sélectionnée (déjà complète avec tous les champs)
                val address = uiState.selectedAddress ?: Address()
                android.util.Log.d("PropertyFormVM", "Adresse utilisée pour la propriété: street=${address.street}, city=${address.city}, postalCode=${address.postalCode}, province=${address.province}, country=${address.country}")

                // Créer l'objet Property
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