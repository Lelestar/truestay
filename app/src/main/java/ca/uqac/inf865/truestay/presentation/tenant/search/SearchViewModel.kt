package ca.uqac.inf865.truestay.presentation.tenant.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SearchViewModel"

data class SearchUiState(
    val properties: List<Property> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val filters: SearchFilters = SearchFilters()
)

data class SearchFilters(
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val minSurface: Int? = null,
    val maxSurface: Int? = null,
    val city: String? = null,
    val isAvailable: Boolean? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "SearchViewModel initialized")
        loadProperties()
    }

    private fun loadProperties() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            propertyRepository.getProperties().fold(
                onSuccess = { properties ->
                    properties.forEachIndexed { index, property ->
                        Log.d(TAG, "  [$index] ${property.name} - ${property.monthlyRent}$ - ${property.address.city}")
                    }
                    _uiState.update {
                        it.copy(
                            properties = properties,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
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

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // TODO: Implement search logic
    }

    fun updateFilters(filters: SearchFilters) {
        _uiState.update { it.copy(filters = filters) }
        // TODO: Apply filters
    }
}