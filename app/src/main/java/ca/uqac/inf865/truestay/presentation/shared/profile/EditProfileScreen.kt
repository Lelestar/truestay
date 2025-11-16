package ca.uqac.inf865.truestay.presentation.shared.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
fun EditProfileScreen(
    onEditEmail: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val colors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    val focusManager = LocalFocusManager.current

    var lastName by rememberSaveable { mutableStateOf("") }
    var firstName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var lastNameErrorRes by rememberSaveable { mutableStateOf<Int?>(null) }
    var firstNameErrorRes by rememberSaveable { mutableStateOf<Int?>(null) }
    var phoneErrorRes by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(user) {
        user?.let {
            firstName = it.firstName
            lastName = it.lastName
            phone = it.phoneNumber
            email = it.email
        }
    }

    val hasChanges = user != null &&
            (firstName != user.firstName ||
                    lastName != user.lastName ||
                    phone != user.phoneNumber)

    fun validateAndSave() {
        lastNameErrorRes = null
        firstNameErrorRes = null
        phoneErrorRes = null

        var hasError = false

        if (lastName.isBlank()) {
            lastNameErrorRes = R.string.register_error_last_name_required
            hasError = true
        }

        if (firstName.isBlank()) {
            firstNameErrorRes = R.string.register_error_first_name_required
            hasError = true
        }

        if (phone.isBlank()) {
            phoneErrorRes = R.string.register_error_phone_required
            hasError = true
        }

        if (hasError || !hasChanges || uiState.isSaving) return

        viewModel.saveProfile(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phone
        )
    }

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
            uiState.isLoading && user == null -> {
                CircularProgressIndicator(color = colors.primary)
            }

            uiState.errorMessage != null && user == null -> {
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
                    TrueStayTextField(
                        leadingIcon = TrueStayIcons.User,
                        value = lastName,
                        label = stringResource(R.string.edit_last_name),
                        placeholder = stringResource(R.string.edit_placeholder_last_name),
                        onValueChange = { lastName = it },
                        enabled = !uiState.isSaving,
                        errorMessage = lastNameErrorRes?.let { stringResource(it) },
                        imeAction = ImeAction.Next,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                        Spacer(modifier = Modifier.height(AppSpacing.large))

                    TrueStayTextField(
                        leadingIcon = TrueStayIcons.User,
                        value = firstName,
                        label = stringResource(R.string.edit_first_name),
                        placeholder = stringResource(R.string.edit_placeholder_first_name),
                        onValueChange = { firstName = it },
                        enabled = !uiState.isSaving,
                        errorMessage = firstNameErrorRes?.let { stringResource(it) },
                        imeAction = ImeAction.Next,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                        Spacer(modifier = Modifier.height(AppSpacing.large))

                    TrueStayTextField(
                        leadingIcon = TrueStayIcons.Phone,
                        value = phone,
                        label = stringResource(R.string.edit_phone),
                        placeholder = stringResource(R.string.edit_placeholder_phone),
                        onValueChange = { phone = it },
                        enabled = !uiState.isSaving,
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            validateAndSave()
                        },
                        errorMessage = phoneErrorRes?.let { stringResource(it) }
                    )

                        Spacer(modifier = Modifier.height(AppSpacing.large))

                        TrueStayTextField(
                            leadingIcon = TrueStayIcons.Mail,
                            value = email,
                            label = stringResource(R.string.edit_email),
                            placeholder = stringResource(R.string.edit_placeholder_email),
                            onValueChange = { },
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.small))

                    TrueStayButton(
                        text = stringResource(R.string.edit_change_email),
                        onClick = onEditEmail,
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.SECONDARY,
                        enabled = !uiState.isSaving
                    )

                        Spacer(modifier = Modifier.height(AppSpacing.xlarge))

                    TrueStayButton(
                        text = stringResource(R.string.edit_save_changes),
                        onClick = { validateAndSave() },
                        enabled = !uiState.isSaving && hasChanges,
                        isLoading = uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.saveSuccess) {
                            Spacer(modifier = Modifier.height(AppSpacing.large))
                            Text(
                                text = stringResource(R.string.edit_profile_success),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.success,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else if (uiState.saveErrorMessage != null) {
                            Spacer(modifier = Modifier.height(AppSpacing.large))
                            Text(
                                text = stringResource(R.string.edit_profile_error,
                                    uiState.saveErrorMessage!!
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
