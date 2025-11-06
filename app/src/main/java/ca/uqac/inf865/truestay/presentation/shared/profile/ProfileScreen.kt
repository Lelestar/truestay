package ca.uqac.inf865.truestay.presentation.shared.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.shared.auth.AuthViewModel
import ca.uqac.inf865.truestay.presentation.shared.auth.rememberCurrentUser
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Profile Screen - TODO")

        Spacer(modifier = Modifier.height(AppSpacing.xlarge))

        // Logout button for testing
        TrueStayButton(
            text = "Se déconnecter",
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            variant = ButtonVariant.SECONDARY
        )
    }
}