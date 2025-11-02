package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.domain.model.UserRole
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing

@Composable
fun RegisterScreen(
    userRole: UserRole,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    registerViewModel: RegisterViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        Text("Register Screen - TODO")
    }
}