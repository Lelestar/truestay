package ca.uqac.inf865.truestay.presentation.landlord.property

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun PropertiesScreen(
    onAddProperty: () -> Unit,
    onEditProperty: (String) -> Unit,
    viewModel: PropertiesViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Properties Screen - TODO")
    }
}