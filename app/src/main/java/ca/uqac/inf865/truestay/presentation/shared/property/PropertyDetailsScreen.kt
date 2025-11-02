package ca.uqac.inf865.truestay.presentation.shared.property

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.navigation.TrueStayTopAppBar
import ca.uqac.inf865.truestay.presentation.shared.auth.rememberCurrentUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailsScreen(
    propertyId: String,
    onBackClick: () -> Unit,
    viewModel: PropertyDetailsViewModel = hiltViewModel()
) {
    val currentUser = rememberCurrentUser()

    Scaffold(
        topBar = {
            TrueStayTopAppBar(
                titleRes = R.string.screen_title_property_details,
                onNavigateBack = onBackClick,
                hasActions = true,
                actions = {
                    // TODO
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Property Details Screen - TODO")
        }
    }
}