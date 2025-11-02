package ca.uqac.inf865.truestay.presentation.tenant.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun FavoritesScreen(
    onPropertyClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Favorites Screen - TODO")
    }
}