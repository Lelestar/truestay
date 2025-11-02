package ca.uqac.inf865.truestay.presentation.tenant.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing

@Composable
fun SearchScreen(
    onPropertyClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TODO: Search bar with filters
        Text("Search Screen - TODO")

        // TODO: Map and Property list with LazyColumn
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.large)
        ) {
            // TODO: Populate with map and property items
        }
    }
}