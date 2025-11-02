package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing

@Composable
fun ForgotPasswordScreen(
    onEmailSent: () -> Unit,
    onNavigateToLogin: () -> Unit,
    forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        Text("Forgot Password Screen - TODO")
    }
}