package ca.uqac.inf865.truestay.presentation.tenant.favorites

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Favorite
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.repository.AuthRepository
import ca.uqac.inf865.truestay.domain.repository.FavoriteRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Combination of a property and its associated favorite for display
 */
data class FavoritePropertyItem(
    val property: Property,
    val favorite: Favorite
)

/**
 * Types of errors that may occur in the favorites screen
 */
enum class FavoritesError {
    NOT_AUTHENTICATED,
    LOAD_FAILED,
    REMOVE_FAILED
}

/**
 * Sorting options available for the favorites list
 */
enum class FavoritesSortOption(@param:StringRes val labelRes: Int) {
    DATE_ADDED(R.string.favorites_sort_recent),
    PRICE(R.string.favorites_sort_price),
    RATING(R.string.favorites_sort_rating)
}

/**
 * UI state for the favorites screen
 */
data class FavoritesUiState(
    val favorites: List<FavoritePropertyItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: FavoritesError? = null,
    val sortOption: FavoritesSortOption = FavoritesSortOption.DATE_ADDED,
    val isSortDescending: Boolean = true,
    val currentUserId: String? = null,
    val pendingRemovalIds: Set<String> = emptySet() // IDs of favorites being deleted
)

/**
 * ViewModel for managing the favorites screen
 *
 * Manages the loading, sorting, and deletion of favorite accommodations.
 * Sorting can be done by date added, price, or rating, in ascending or descending order.
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val propertyRepository: PropertyRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState(isLoading = true))
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        refreshFavorites()
    }

    /**
     * Reloads the favorites list from the repositories
     */
    fun refreshFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                favorites = emptyList(),
                                error = FavoritesError.NOT_AUTHENTICATED,
                                currentUserId = null,
                                pendingRemovalIds = emptySet()
                            )
                        }
                        return@onSuccess
                    }
                    fetchFavoritesForUser(user.id)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            favorites = emptyList(),
                            error = FavoritesError.LOAD_FAILED,
                            currentUserId = null,
                            pendingRemovalIds = emptySet()
                        )
                    }
                }
        }
    }

    /**
     * Retrieves a user's favorites and combines them with accommodation information
     */
    private suspend fun fetchFavoritesForUser(userId: String) {
        favoriteRepository.getFavorites(userId)
            .onSuccess { favorites ->
                if (favorites.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            favorites = emptyList(),
                            isLoading = false,
                            error = null,
                            currentUserId = userId,
                            pendingRemovalIds = emptySet()
                        )
                    }
                    return@onSuccess
                }

                propertyRepository.getProperties()
                    .onSuccess { properties ->
                        val propertyMap = properties.associateBy { it.id }
                        val items = favorites.mapNotNull { favorite ->
                            propertyMap[favorite.propertyId]?.let { property ->
                                FavoritePropertyItem(property = property, favorite = favorite)
                            }
                        }

                        _uiState.update { state ->
                            val sorted = applySort(
                                favorites = items,
                                option = state.sortOption,
                                descending = state.isSortDescending
                            )
                            state.copy(
                                favorites = sorted,
                                isLoading = false,
                                error = null,
                                currentUserId = userId,
                                pendingRemovalIds = emptySet()
                            )
                        }
                    }
                    .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            favorites = emptyList(),
                            isLoading = false,
                            error = FavoritesError.LOAD_FAILED,
                            currentUserId = userId,
                            pendingRemovalIds = emptySet()
                        )
                    }
                }
            }
            .onFailure {
                _uiState.update { state ->
                    state.copy(
                        favorites = emptyList(),
                        isLoading = false,
                        error = FavoritesError.LOAD_FAILED,
                        currentUserId = userId,
                        pendingRemovalIds = emptySet()
                    )
                }
            }
    }

    /**
     * Changes the sort option and reapplies the sort to the list
     */
    fun onSortOptionSelected(option: FavoritesSortOption) {
        _uiState.update { state ->
            val sorted = applySort(state.favorites, option, state.isSortDescending)
            state.copy(
                sortOption = option,
                favorites = sorted
            )
        }
    }

    /**
     * Reverse the sort order (ascending/descending)
     */
    fun toggleSortOrder() {
        _uiState.update { state ->
            val newDescending = !state.isSortDescending
            val sorted = applySort(state.favorites, state.sortOption, newDescending)
            state.copy(
                isSortDescending = newDescending,
                favorites = sorted
            )
        }
    }

    /**
     * Remove a listing from favorites
     * Prevents multiple simultaneous deletions of the same bookmark
     */
    fun toggleFavorite(propertyId: String) {
        val userId = _uiState.value.currentUserId
        if (userId == null) {
            _uiState.update { it.copy(error = FavoritesError.NOT_AUTHENTICATED) }
            return
        }

        if (_uiState.value.pendingRemovalIds.contains(propertyId)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(pendingRemovalIds = it.pendingRemovalIds + propertyId) }

            favoriteRepository.removeFavorite(userId, propertyId)
                .onSuccess {
                    _uiState.update { state ->
                        val remaining = state.favorites.filterNot { it.property.id == propertyId }
                        val sorted = applySort(remaining, state.sortOption, state.isSortDescending)
                        state.copy(
                            favorites = sorted,
                            pendingRemovalIds = state.pendingRemovalIds - propertyId,
                            error = null
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            pendingRemovalIds = state.pendingRemovalIds - propertyId,
                            error = FavoritesError.REMOVE_FAILED
                        )
                    }
                }
        }
    }

    /**
     * Applies sorting to a list of favorites according to the specified option and order
     */
    private fun applySort(
        favorites: List<FavoritePropertyItem>,
        option: FavoritesSortOption,
        descending: Boolean
    ): List<FavoritePropertyItem> {
        val sorted = when (option) {
            FavoritesSortOption.DATE_ADDED -> favorites.sortedBy { it.favorite.addedAt }
            FavoritesSortOption.PRICE -> favorites.sortedBy { it.property.monthlyRent }
            FavoritesSortOption.RATING -> favorites.sortedBy { it.property.ratings.propertyAverageRating }
        }
        return if (descending) sorted.reversed() else sorted
    }
}
