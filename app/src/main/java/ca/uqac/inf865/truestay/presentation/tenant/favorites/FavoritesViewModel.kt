package ca.uqac.inf865.truestay.presentation.tenant.favorites

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.FavoriteRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val propertyRepository: PropertyRepository
) : ViewModel() {
    // TODO: Implement favorites logic
}