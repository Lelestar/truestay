package ca.uqac.inf865.truestay.presentation.shared.inventory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.presentation.shared.auth.rememberCurrentUser

@Composable
fun InventoryScreen(
    inventoryId: String,
    onRoomClick: (String) -> Unit,
    onSignClick: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val currentUser = rememberCurrentUser()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Inventory Screen - TODO")
    }
}