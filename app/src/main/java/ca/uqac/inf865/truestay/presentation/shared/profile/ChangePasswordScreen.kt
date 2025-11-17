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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors

@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val colors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val hasChanges = uiState.currentPassword.isNotBlank() && uiState.newPassword.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isSubmitting && !hasChanges -> {
                CircularProgressIndicator(color = colors.primary)
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
                        // Current password
                        TrueStayTextField(
                            leadingIcon = TrueStayIcons.Lock,
                            value = uiState.currentPassword,
                            label = stringResource(R.string.change_password_current),
                            placeholder = "********",
                            onValueChange = viewModel::onCurrentPasswordChanged,
                            isPassword = true,
                            enabled = !uiState.isSubmitting,
                            errorMessage = uiState.currentPasswordError,
                            imeAction = ImeAction.Next,
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.large))

                        // New password
                        TrueStayTextField(
                            leadingIcon = TrueStayIcons.Lock,
                            value = uiState.newPassword,
                            label = stringResource(R.string.change_password_new),
                            placeholder = "********",
                            onValueChange = viewModel::onNewPasswordChanged,
                            isPassword = true,
                            enabled = !uiState.isSubmitting,
                            errorMessage = uiState.newPasswordError,
                            imeAction = ImeAction.Done,
                            onImeAction = {
                                if (!uiState.isSubmitting && hasChanges) {
                                    viewModel.submitChange()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.xlarge))

                        TrueStayButton(
                            text = stringResource(R.string.change_password_submit),
                            onClick = { viewModel.submitChange() },
                            enabled = !uiState.isSubmitting && hasChanges,
                            isLoading = uiState.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                            variant = ButtonVariant.PRIMARY
                        )

                        if (uiState.successMessage != null) {
                            Spacer(modifier = Modifier.height(AppSpacing.small))
                            Text(
                                text = uiState.successMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.success,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else if (uiState.globalError != null) {
                            Spacer(modifier = Modifier.height(AppSpacing.small))
                            Text(
                                text = uiState.globalError ?: "",
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

