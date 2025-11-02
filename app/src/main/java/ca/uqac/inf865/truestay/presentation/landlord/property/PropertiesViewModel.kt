package ca.uqac.inf865.truestay.presentation.landlord.property

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PropertiesViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository
) : ViewModel() {
    // TODO: Implement properties logic
}