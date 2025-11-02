package ca.uqac.inf865.truestay.presentation.tenant.rentals

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RentalsViewModel @Inject constructor(
    private val rentalRepository: RentalRepository
) : ViewModel() {
    // TODO: Implement rentals logic
}