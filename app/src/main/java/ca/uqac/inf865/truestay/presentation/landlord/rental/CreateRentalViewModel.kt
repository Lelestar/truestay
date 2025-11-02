package ca.uqac.inf865.truestay.presentation.landlord.rental

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.RentalRepository
import ca.uqac.inf865.truestay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateRentalViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    // TODO: Implement create rental logic
}