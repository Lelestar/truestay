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

@Composable
fun ForgotPasswordScreen(
    onEmailSent: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by forgotPasswordViewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Handle navigation after email sent
    LaunchedEffect(uiState.emailSent) {
        if (uiState.emailSent) {
            onEmailSent(uiState.email)
            forgotPasswordViewModel.onEmailSentHandled()
        }
    }

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
                text = stringResource(R.string.forgot_password_title),
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.black
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

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
                onValueChange = forgotPasswordViewModel::onEmailChanged,
                label = stringResource(R.string.forgot_password_email),
                placeholder = stringResource(R.string.forgot_password_email_placeholder),
                leadingIcon = TrueStayIcons.Mail,
                enabled = !uiState.isLoading,
                errorMessage = uiState.emailError,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        forgotPasswordViewModel.sendPasswordResetEmail()
                    }
                )
            )

            Spacer(modifier = Modifier.height(AppSpacing.medium))

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
                onClick = forgotPasswordViewModel::sendPasswordResetEmail,
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

            // Back to login text and button
            TrueStayButton(
                text = stringResource(R.string.forgot_password_back_to_login),
                onClick = onNavigateToLogin,
                enabled = !uiState.isLoading,
                variant = ButtonVariant.SECONDARY
            )
        }
    }
}