package ca.uqac.inf865.truestay.presentation.tenant.rentals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun RentalsScreen(
    onRentalClick: (String) -> Unit,
    onInventoryClick: (String) -> Unit,
    onReviewClick: (String) -> Unit,
    viewModel: RentalsViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Rentals Screen - TODO")
    }
}