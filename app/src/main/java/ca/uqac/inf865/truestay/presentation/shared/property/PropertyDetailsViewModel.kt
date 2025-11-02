package ca.uqac.inf865.truestay.presentation.shared.property

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.FavoriteRepository
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PropertyDetailsViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val favoriteRepository: FavoriteRepository,
    private val ReviewRepository: ReviewRepository
) : ViewModel() {
    // TODO: Implement property details logic
}