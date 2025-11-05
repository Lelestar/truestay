package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

@Composable
fun ForgotPasswordScreen(
    onEmailSent: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by forgotPasswordViewModel.uiState.collectAsStateWithLifecycle()

    // Handle navigation after email sent
    LaunchedEffect(uiState.emailSent) {
        if (uiState.emailSent) {
            onEmailSent(uiState.email)
            forgotPasswordViewModel.onEmailSentHandled()
        }
    }

    ForgotPasswordScreenContent(
        uiState = uiState,
        onEmailChanged = forgotPasswordViewModel::onEmailChanged,
        onSendClick = forgotPasswordViewModel::sendPasswordResetEmail,
        onNavigateToLogin = onNavigateToLogin
    )
}

/**
 * Component without ViewModel to facilitate previews
 */
@Composable
private fun ForgotPasswordScreenContent(
    uiState: ForgotPasswordUiState,
    onEmailChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.large)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo_truestay),
            contentDescription = stringResource(R.string.app_name),
        )
        Spacer(modifier = Modifier.height(AppSpacing.small))
        Text(
            text = stringResource(R.string.forgot_password_tagline),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = LocalAppColors.current.grayDark
        )

        Spacer(modifier = Modifier.height(AppSpacing.xxlarge))

        TrueStayCard(modifier = Modifier.fillMaxWidth()) {
            // Title
            Text(
                text = stringResource(R.string.forgot_password_title),
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.black
            )

            Spacer(modifier = Modifier.height(AppSpacing.small))

            // Subtitle
            Text(
                text = stringResource(R.string.forgot_password_subtitle),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Email field
            TrueStayTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = stringResource(R.string.forgot_password_email),
                placeholder = stringResource(R.string.forgot_password_email_placeholder),
                leadingIcon = TrueStayIcons.Mail,
                enabled = !uiState.isLoading,
                errorMessage = uiState.emailError,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSendClick()
                    }
                )
            )

            Spacer(modifier = Modifier.height(AppSpacing.small))

            // Note
            Text(
                text = stringResource(R.string.forgot_password_note),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = LocalAppColors.current.grayDark,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Send button
            TrueStayButton(
                text = stringResource(R.string.forgot_password_send_button),
                onClick = onSendClick,
                enabled = !uiState.isLoading,
                isLoading = uiState.isLoading
            )

            // Global error message
            uiState.globalError?.let { errorMessage ->
                Spacer(modifier = Modifier.height(AppSpacing.large))
                Text(
                    text = errorMessage,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = LocalAppColors.current.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = LocalAppColors.current.grayBorder
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Back to login button
            TrueStayButton(
                text = stringResource(R.string.forgot_password_back_to_login),
                onClick = onNavigateToLogin,
                enabled = !uiState.isLoading,
                variant = ButtonVariant.SECONDARY
            )
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(name = "Forgot Password - Empty", showBackground = true)
@Composable
private fun ForgotPasswordEmptyPreview() {
    TrueStayTheme {
        ForgotPasswordScreenContent(
            uiState = ForgotPasswordUiState(),
            onEmailChanged = {},
            onSendClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Forgot Password - Filled", showBackground = true)
@Composable
private fun ForgotPasswordFilledPreview() {
    TrueStayTheme {
        ForgotPasswordScreenContent(
            uiState = ForgotPasswordUiState(
                email = "user@example.com"
            ),
            onEmailChanged = {},
            onSendClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Forgot Password - With Error", showBackground = true)
@Composable
private fun ForgotPasswordErrorPreview() {
    TrueStayTheme {
        ForgotPasswordScreenContent(
            uiState = ForgotPasswordUiState(
                email = "invalid-email",
                emailError = "Email invalide"
            ),
            onEmailChanged = {},
            onSendClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Forgot Password - Global Error", showBackground = true)
@Composable
private fun ForgotPasswordGlobalErrorPreview() {
    TrueStayTheme {
        ForgotPasswordScreenContent(
            uiState = ForgotPasswordUiState(
                email = "user@example.com",
                globalError = "Aucun utilisateur trouvé avec cet email"
            ),
            onEmailChanged = {},
            onSendClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Forgot Password - Loading", showBackground = true)
@Composable
private fun ForgotPasswordLoadingPreview() {
    TrueStayTheme {
        ForgotPasswordScreenContent(
            uiState = ForgotPasswordUiState(
                email = "user@example.com",
                isLoading = true
            ),
            onEmailChanged = {},
            onSendClick = {},
            onNavigateToLogin = {}
        )
    }
}

