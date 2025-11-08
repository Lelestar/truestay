package ca.uqac.inf865.truestay.presentation.tenant.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.GeoBounds
import ca.uqac.inf865.truestay.domain.model.AutocompleteSuggestion
import ca.uqac.inf865.truestay.domain.repository.GeocodingRepository
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyFilters
import ca.uqac.inf865.truestay.domain.model.BedroomCount
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for search.
 * - `allProperties`: unfiltered list (used for initial camera fit and slider ranges).
 * - `properties`: filtered list rendered by the map and the bottom sheet.
 * - `filters`: domain filters applied via the repository (incl. geo bounds).
 * - `searchLocation`/`searchZoomLevel`/`searchBounds`: camera targets for the map.
 * - `suggestions`: Places autocomplete suggestions for the text field.
 */
data class SearchUiState(
    val allProperties: List<Property> = emptyList(),
    val properties: List<Property> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val filters: PropertyFilters = PropertyFilters(availableOnly = false),
    val searchLocation: LatLng? = null,
    val searchZoomLevel: Float? = null,
    val searchBounds: LatLngBounds? = null,
    val suggestions: List<AutocompleteSuggestion> = emptyList()
)

/**
 * ViewModel for the tenant search screen.
 *
 * Responsibilities:
 * - Holds current `PropertyFilters` and filtered `properties` list.
 * - Debounces refreshes and delegates filtering to `PropertyRepository.searchProperties`.
 * - Handles geocoding for the text field and maps the result to camera targets only.
 * - Updates `geoBounds` from the visible map area to constrain results.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val geocodingRepository: GeocodingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var refreshJob: kotlinx.coroutines.Job? = null

    init {
        loadProperties()
    }

    private fun loadProperties() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            propertyRepository.getProperties().fold(
                onSuccess = { properties ->
                    // Save all properties for initial camera fit and other needs
                    _uiState.update { it.copy(allProperties = properties) }
                    // Immediately populate UI with filtered results (defaults apply)
                    val filters = _uiState.value.filters
                    propertyRepository.searchProperties(filters).fold(
                        onSuccess = { list ->
                            _uiState.update {
                                it.copy(
                                    properties = list,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    properties = properties, // fallback to all
                                    isLoading = false,
                                    errorMessage = e.message
                                )
                            }
                        }
                    )
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
            )
        }
    }

    /** Update the query typed by the user (and refresh suggestions with debounce). */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        fetchSuggestionsDebounced(query)
    }

    /**
     * Resolve the user query to a place using the geocoding repository, then map the result
     * into UI types (LatLng/LatLngBounds) used only for camera movement.
     * No text-based filtering is applied.
     */
    fun searchLocation(query: String) {
        if (query.isBlank()) {
            clearSearchLocation()
            return
        }

        viewModelScope.launch {
            // Retrieve first suggestion (may be null)
            val suggestion = geocodingRepository.search(query).getOrNull()
            suggestion?.let { s ->
                // Map domain models to Google Maps types for UI
                val uiLatLng = LatLng(s.location.latitude, s.location.longitude)
                val zoom = computeZoomFromBounds(s.bounds) // fallback if no bounds
                val uiBounds = s.bounds?.let { b ->
                    val ne = LatLng(b.northEast.latitude, b.northEast.longitude)
                    val sw = LatLng(b.southWest.latitude, b.southWest.longitude)
                    LatLngBounds(sw, ne)
                }
                _uiState.update {
                    it.copy(
                        searchLocation = uiLatLng,
                        searchZoomLevel = zoom,
                        searchBounds = uiBounds,
                        suggestions = emptyList()
                    )
                }
            }
        }
    }

    /** Called when the user taps a suggestion: move camera only (no text filtering). */
    fun onSuggestionSelected(suggestion: AutocompleteSuggestion) {
        viewModelScope.launch {
            val result = geocodingRepository.getById(suggestion.placeId).getOrNull()
            result?.let { s ->
                val uiLatLng = LatLng(s.location.latitude, s.location.longitude)
                val zoom = computeZoomFromBounds(s.bounds)
                val uiBounds = s.bounds?.let { b ->
                    val ne = LatLng(b.northEast.latitude, b.northEast.longitude)
                    val sw = LatLng(b.southWest.latitude, b.southWest.longitude)
                    LatLngBounds(sw, ne)
                }
                _uiState.update {
                    it.copy(
                        searchQuery = suggestion.primaryText,
                        searchLocation = uiLatLng,
                        searchZoomLevel = zoom,
                        searchBounds = uiBounds,
                        suggestions = emptyList()
                    )
                }
            }
        }
    }

    private var suggestionsJob: kotlinx.coroutines.Job? = null
    private fun fetchSuggestionsDebounced(query: String) {
        suggestionsJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }
        suggestionsJob = viewModelScope.launch {
            kotlinx.coroutines.delay(250)
            val list = geocodingRepository.suggest(query, limit = 3).getOrNull().orEmpty()
            _uiState.update { it.copy(suggestions = list) }
        }
    }

    /** Small heuristic used only when Places does not provide a viewport. */
    private fun computeZoomFromBounds(bounds: GeoBounds?): Float {
        if (bounds == null) return 12f
        val latDiff = kotlin.math.abs(bounds.northEast.latitude - bounds.southWest.latitude)
        val lngDiff = kotlin.math.abs(bounds.northEast.longitude - bounds.southWest.longitude)
        val maxDiff = maxOf(latDiff, lngDiff)
        return when {
            maxDiff < 0.001 -> 17f
            maxDiff < 0.01 -> 15f
            maxDiff < 0.1 -> 12f
            maxDiff < 1.0 -> 9f
            else -> 6f
        }
    }

    /** Clear current search camera targets so the next search animates properly. */
    fun clearSearchLocation() {
        _uiState.update { it.copy(searchLocation = null, searchZoomLevel = null, searchBounds = null) }
    }

    /** Update geo-bounds filter from current map viewport and refresh (debounced). */
    fun updateGeoBounds(
        northEast: Pair<Double, Double>,
        southWest: Pair<Double, Double>
    ) {
        val newBounds = GeoBounds(
            northEast = ca.uqac.inf865.truestay.domain.model.LatLng(northEast.first, northEast.second),
            southWest = ca.uqac.inf865.truestay.domain.model.LatLng(southWest.first, southWest.second)
        )
        _uiState.update { it.copy(filters = it.filters.copy(geoBounds = newBounds)) }
        scheduleRefresh()
    }

    /** Clears current suggestions list (used by dropdown dismiss). */
    fun clearSuggestions() {
        _uiState.update { it.copy(suggestions = emptyList()) }
    }

    // ============================
    // Filters update API
    // ============================
    fun updatePriceRange(min: Int?, max: Int?) {
        _uiState.update {
            it.copy(
                filters = it.filters.copy(
                    minPrice = min?.toDouble(),
                    maxPrice = max?.toDouble()
                )
            )
        }
        scheduleRefresh()
    }

    fun updateSurfaceRange(min: Int?, max: Int?) {
        _uiState.update {
            it.copy(
                filters = it.filters.copy(
                    minSurface = min?.toDouble(),
                    maxSurface = max?.toDouble()
                )
            )
        }
        scheduleRefresh()
    }

    fun toggleBedroomCount(count: BedroomCount) {
        val current = _uiState.value.filters.bedroomCounts
        val updated = if (current.contains(count)) current.filterNot { it == count } else current + count
        _uiState.update { it.copy(filters = it.filters.copy(bedroomCounts = updated)) }
        scheduleRefresh()
    }

    fun setMinRating(rating: Float?) {
        _uiState.update { it.copy(filters = it.filters.copy(minRating = rating)) }
        scheduleRefresh()
    }

    fun setAvailableOnly(value: Boolean) {
        _uiState.update { it.copy(filters = it.filters.copy(availableOnly = value)) }
        scheduleRefresh()
    }

    fun resetFilters() {
        _uiState.update { it.copy(filters = PropertyFilters(availableOnly = false)) }
        scheduleRefresh()
    }

    /** Debounce helper to avoid spamming repository calls while user interacts. */
    private fun scheduleRefresh(debounceMs: Long = 250L) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            kotlinx.coroutines.delay(debounceMs)
            refreshSearch()
        }
    }

    /** Run the repository search with current filters and update UI state. */
    private suspend fun refreshSearch() {
        val filters = _uiState.value.filters
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = propertyRepository.searchProperties(filters)
        result.fold(
            onSuccess = { list ->
                _uiState.update { it.copy(properties = list, isLoading = false) }
            },
            onFailure = { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        )
    }
}
