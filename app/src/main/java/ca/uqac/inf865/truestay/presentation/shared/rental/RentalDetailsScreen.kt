package ca.uqac.inf865.truestay.presentation.shared.rental

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.presentation.shared.auth.rememberCurrentUser

enum class RentalDetailsSection {
    INFO,
    PROGRESS,
    INVENTORIES,
    REVIEWS
}

@Composable
fun RentalDetailsScreen(
    rentalId: String,
    initialSection: RentalDetailsSection? = null,
    onAddOrEditReviewClick: (String) -> Unit, // Tenant only
    onInventoryClick: (String) -> Unit,
    onEndLease: (() -> Unit)? = null, // Landlord only
    viewModel: RentalDetailsViewModel = hiltViewModel()
) {
    val currentUser = rememberCurrentUser()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Rental Details Screen - TODO")
    }
}