package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.UserRole
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = viewModel(LocalContext.current as ComponentActivity)
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    // Handle navigation after success
    LaunchedEffect(uiState.loginSuccess) {
        uiState.loginSuccess?.let { role ->
            authViewModel.refreshAuthStatus()
            onLoginSuccess(role)
            loginViewModel.onLoginSuccessHandled()
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChanged = loginViewModel::onEmailChanged,
        onPasswordChanged = loginViewModel::onPasswordChanged,
        onLoginClick = loginViewModel::login,
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToForgotPassword = onNavigateToForgotPassword
    )
}

/**
 * Component without ViewModel to facilitate previews
 */
@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
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
            text = stringResource(R.string.login_tagline),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = LocalAppColors.current.grayDark
        )

        Spacer(modifier = Modifier.height(AppSpacing.xxlarge))

        TrueStayCard(modifier = Modifier.fillMaxWidth()) {
            // Title
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.black
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Email
            TrueStayTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = stringResource(R.string.login_email),
                placeholder = "email@exemple.com",
                leadingIcon = TrueStayIcons.Mail,
                enabled = !uiState.isLoading,
                errorMessage = uiState.emailError,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(AppSpacing.large))

            // Password
            TrueStayTextField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                label = stringResource(R.string.login_password),
                placeholder = "••••••••",
                isPassword = true,
                enabled = !uiState.isLoading,
                errorMessage = uiState.passwordError,
                imeAction = ImeAction.Done,
                onImeAction = onLoginClick
            )

            // Forgot password link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onNavigateToForgotPassword,
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = stringResource(R.string.login_forgot_password),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalAppColors.current.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Connexion button
            TrueStayButton(
                text = stringResource(R.string.login_button),
                onClick = onLoginClick,
                enabled = !uiState.isLoading,
                isLoading = uiState.isLoading
            )

            // Global error message
            if (uiState.globalError != null) {
                Spacer(modifier = Modifier.height(AppSpacing.large))
                Text(
                    text = uiState.globalError,
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

            // Register text and button
            Text(
                text = stringResource(R.string.login_no_account),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(AppSpacing.large))
            TrueStayButton(
                text = stringResource(R.string.login_register_button),
                onClick = onNavigateToRegister,
                variant = ButtonVariant.SECONDARY,
                enabled = !uiState.isLoading
            )
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(name = "Login - Empty", showBackground = true)
@Composable
private fun LoginScreenEmptyPreview() {
    TrueStayTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClick = {},
            onNavigateToRegister = {},
            onNavigateToForgotPassword = {}
        )
    }
}

@Preview(name = "Login - Filled", showBackground = true)
@Composable
private fun LoginScreenFilledPreview() {
    TrueStayTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                email = "user@example.com",
                password = "password123"
            ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClick = {},
            onNavigateToRegister = {},
            onNavigateToForgotPassword = {}
        )
    }
}

@Preview(name = "Login - With Errors", showBackground = true)
@Composable
private fun LoginScreenErrorsPreview() {
    TrueStayTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                email = "invalid-email",
                password = "123",
                emailError = "Email invalide",
                passwordError = "Minimum 8 caractères"
            ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClick = {},
            onNavigateToRegister = {},
            onNavigateToForgotPassword = {}
        )
    }
}

@Preview(name = "Login - Global Error", showBackground = true)
@Composable
private fun LoginScreenGlobalErrorPreview() {
    TrueStayTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                email = "user@example.com",
                password = "password",
                globalError = "Le mot de passe est incorrect"
            ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClick = {},
            onNavigateToRegister = {},
            onNavigateToForgotPassword = {}
        )
    }
}

@Preview(name = "Login - Loading", showBackground = true)
@Composable
private fun LoginScreenLoadingPreview() {
    TrueStayTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                email = "user@example.com",
                password = "password",
                isLoading = true
            ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClick = {},
            onNavigateToRegister = {},
            onNavigateToForgotPassword = {}
        )
    }
}
