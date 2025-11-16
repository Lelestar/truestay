package ca.uqac.inf865.truestay.presentation.shared.profile

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun ChangeEmailScreen(
    viewModel: ChangeEmailViewModel = hiltViewModel()
) {
    val colors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val hasChanges = uiState.newEmail.isNotBlank()
            && uiState.newEmail != uiState.currentEmail
            && uiState.password.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = colors.primary)
            }

            uiState.errorMessage != null && uiState.currentEmail.isEmpty() -> {
                Text(
                    text = uiState.errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.error
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(AppSpacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TrueStayCard(modifier = Modifier.fillMaxWidth()) {
                        // New email
                        TrueStayTextField(
                            leadingIcon = TrueStayIcons.Mail,
                            value = uiState.newEmail,
                            label = stringResource(R.string.change_email_new_email),
                            placeholder = stringResource(R.string.edit_placeholder_email),
                            onValueChange = viewModel::onNewEmailChanged,
                            enabled = !uiState.isSubmitting,
                            errorMessage = uiState.newEmailError,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.large))

                        // Password
                        TrueStayTextField(
                            leadingIcon = TrueStayIcons.Lock,
                            value = uiState.password,
                            label = stringResource(R.string.login_password),
                            placeholder = "********",
                            onValueChange = viewModel::onPasswordChanged,
                            isPassword = true,
                            enabled = !uiState.isSubmitting,
                             errorMessage = uiState.passwordError,
                            imeAction = ImeAction.Done,
                            onImeAction = {
                                if (!uiState.isSubmitting && hasChanges) {
                                    viewModel.submitChange()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.xlarge))

                        TrueStayButton(
                            text = stringResource(R.string.forgot_password_send_button),
                            onClick = { viewModel.submitChange() },
                            enabled = !uiState.isSubmitting && hasChanges,
                            isLoading = uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                            variant = ButtonVariant.PRIMARY
                        )

                        if (uiState.submitSuccess) {
                            Spacer(modifier = Modifier.height(AppSpacing.small))
                            Text(
                                text = stringResource(R.string.change_email_success),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.success,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else if (uiState.submitErrorMessage != null) {
                            Spacer(modifier = Modifier.height(AppSpacing.small))
                            Text(
                                text = stringResource(R.string.change_email_error,
                                    uiState.submitErrorMessage!!
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.error,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}
