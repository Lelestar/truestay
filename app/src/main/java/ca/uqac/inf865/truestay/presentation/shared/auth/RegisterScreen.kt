package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.UserRole
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

@Composable
fun RegisterScreen(
    userRole: UserRole,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    registerViewModel: RegisterViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by registerViewModel.uiState.collectAsStateWithLifecycle()

    // Handle navigation after success
    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) {
            authViewModel.refreshAuthStatus()
            onRegisterSuccess()
            registerViewModel.onRegisterSuccessHandled()
        }
    }

    RegisterScreenContent(
        userRole = userRole,
        uiState = uiState,
        onFirstNameChanged = registerViewModel::onFirstNameChanged,
        onLastNameChanged = registerViewModel::onLastNameChanged,
        onEmailChanged = registerViewModel::onEmailChanged,
        onPhoneChanged = registerViewModel::onPhoneChanged,
        onPasswordChanged = registerViewModel::onPasswordChanged,
        onPasswordConfirmChanged = registerViewModel::onPasswordConfirmChanged,
        onRegisterClick = { registerViewModel.register(userRole) },
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToTerms = onNavigateToTerms,
        onNavigateToPrivacy = onNavigateToPrivacy
    )
}

/**
 * Component without ViewModel to facilitate previews
 */
@Composable
private fun RegisterScreenContent(
    userRole: UserRole,
    uiState: RegisterUiState,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmChanged: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    // Get icon and background color based on userRole
    val iconRes = when (userRole) {
        UserRole.TENANT -> TrueStayIcons.House
        UserRole.LANDLORD -> TrueStayIcons.Building
    }
    val backgroundColor = when (userRole) {
        UserRole.TENANT -> LocalAppColors.current.primary
        UserRole.LANDLORD -> LocalAppColors.current.primaryVariant
    }

    // Get title and subtitle based on userRole
    val title = when (userRole) {
        UserRole.TENANT -> stringResource(R.string.register_title_tenant)
        UserRole.LANDLORD -> stringResource(R.string.register_title_landlord)
    }
    val subtitle = when (userRole) {
        UserRole.TENANT -> stringResource(R.string.register_subtitle_tenant)
        UserRole.LANDLORD -> stringResource(R.string.register_subtitle_landlord)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with circular background
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = backgroundColor,
                    shape = CircleShape
                )
                .padding(AppSpacing.xlarge),
            contentAlignment = Alignment.Center
        ) {
            TrueStayIcon(
                iconRes = iconRes,
                contentDescriptionRes = null,
                tint = LocalAppColors.current.white,
                size = 32.dp
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.large))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = LocalAppColors.current.black
        )

        Spacer(modifier = Modifier.height(AppSpacing.small))

        // Subtitle
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = LocalAppColors.current.grayDark
        )

        Spacer(modifier = Modifier.height(AppSpacing.xxlarge))

        TrueStayCard(modifier = Modifier.fillMaxWidth()) {
            // First Name
            TrueStayTextField(
                value = uiState.firstName,
                onValueChange = onFirstNameChanged,
                label = stringResource(R.string.register_first_name),
                placeholder = "Jean",
                leadingIcon = TrueStayIcons.User,
                enabled = !uiState.isLoading,
                errorMessage = uiState.firstNameError,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(AppSpacing.large))

            // Last Name
            TrueStayTextField(
                value = uiState.lastName,
                onValueChange = onLastNameChanged,
                label = stringResource(R.string.register_last_name),
                placeholder = "Dupont",
                leadingIcon = TrueStayIcons.User,
                enabled = !uiState.isLoading,
                errorMessage = uiState.lastNameError,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(AppSpacing.large))

            // Email
            TrueStayTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = stringResource(R.string.register_email),
                placeholder = "jean.dupont@exemple.com",
                leadingIcon = TrueStayIcons.Mail,
                enabled = !uiState.isLoading,
                errorMessage = uiState.emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(AppSpacing.large))

            // Phone
            TrueStayTextField(
                value = uiState.phone,
                onValueChange = onPhoneChanged,
                label = stringResource(R.string.register_phone),
                placeholder = "+33612345678",
                leadingIcon = TrueStayIcons.Phone,
                enabled = !uiState.isLoading,
                errorMessage = uiState.phoneError,
                keyboardType = KeyboardType.Phone,
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
                label = stringResource(R.string.register_password),
                placeholder = "••••••••",
                isPassword = true,
                enabled = !uiState.isLoading,
                errorMessage = uiState.passwordError,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(AppSpacing.large))

            // Confirm Password
            TrueStayTextField(
                value = uiState.passwordConfirm,
                onValueChange = onPasswordConfirmChanged,
                label = stringResource(R.string.register_password_confirm),
                placeholder = "••••••••",
                isPassword = true,
                enabled = !uiState.isLoading,
                errorMessage = uiState.passwordConfirmError,
                imeAction = ImeAction.Done,
                onImeAction = onRegisterClick
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Terms and Privacy text
            TermsAndPrivacyText(
                onTermsClick = onNavigateToTerms,
                onPrivacyClick = onNavigateToPrivacy
            )

            Spacer(modifier = Modifier.height(AppSpacing.large))

            // Create Account button
            TrueStayButton(
                text = stringResource(R.string.register_button),
                onClick = onRegisterClick,
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

            // Login text and button
            Text(
                text = stringResource(R.string.register_has_account),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(AppSpacing.large))
            TrueStayButton(
                text = stringResource(R.string.register_login_button),
                onClick = onNavigateToLogin,
                variant = ButtonVariant.SECONDARY,
                enabled = !uiState.isLoading
            )
        }
    }
}

@Composable
fun TermsAndPrivacyText(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val body = MaterialTheme.typography.bodySmall.toSpanStyle().copy(color = colors.black)
    val link = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(color = colors.primary)

    // Reusable clickable annotations (remember so they’re stable)
    val termsAnno = remember(onTermsClick) {
        LinkAnnotation.Clickable(tag = "terms") { onTermsClick() }
    }
    val privacyAnno = remember(onPrivacyClick) {
        LinkAnnotation.Clickable(tag = "privacy") { onPrivacyClick() }
    }

    val text = buildAnnotatedString {
        withStyle(body) { append(stringResource(R.string.register_terms_intro)) }
        append(" ")
        appendLink(text = stringResource(R.string.register_terms_link), link = termsAnno, style = link)
        append(" ")
        withStyle(body) { append(stringResource(R.string.register_terms_and)) }
        append(" ")
        appendLink(text = stringResource(R.string.register_privacy_link), link = privacyAnno, style = link)
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall
    )
}

// Helper to append link with LinkAnnotation
private fun AnnotatedString.Builder.appendLink(
    text: String,
    link: LinkAnnotation,
    style: SpanStyle
) {
    withLink(link) { withStyle(style) { append(text) } }
}

// ==========================================
// Previews
// ==========================================
@Preview(name = "Register Tenant - Empty", showBackground = true)
@Composable
private fun RegisterTenantEmptyPreview() {
    TrueStayTheme {
        RegisterScreenContent(
            userRole = UserRole.TENANT,
            uiState = RegisterUiState(),
            onFirstNameChanged = {},
            onLastNameChanged = {},
            onEmailChanged = {},
            onPhoneChanged = {},
            onPasswordChanged = {},
            onPasswordConfirmChanged = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Register Landlord - Empty", showBackground = true)
@Composable
private fun RegisterLandlordEmptyPreview() {
    TrueStayTheme {
        RegisterScreenContent(
            userRole = UserRole.LANDLORD,
            uiState = RegisterUiState(),
            onFirstNameChanged = {},
            onLastNameChanged = {},
            onEmailChanged = {},
            onPhoneChanged = {},
            onPasswordChanged = {},
            onPasswordConfirmChanged = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Register - Filled", showBackground = true)
@Composable
private fun RegisterFilledPreview() {
    TrueStayTheme {
        RegisterScreenContent(
            userRole = UserRole.TENANT,
            uiState = RegisterUiState(
                firstName = "Jean",
                lastName = "Dupont",
                email = "jean.dupont@exemple.com",
                phone = "+33612345678",
                password = "password123",
                passwordConfirm = "password123"
            ),
            onFirstNameChanged = {},
            onLastNameChanged = {},
            onEmailChanged = {},
            onPhoneChanged = {},
            onPasswordChanged = {},
            onPasswordConfirmChanged = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Register - With Errors", showBackground = true)
@Composable
private fun RegisterErrorsPreview() {
    TrueStayTheme {
        RegisterScreenContent(
            userRole = UserRole.TENANT,
            uiState = RegisterUiState(
                firstName = "",
                lastName = "",
                email = "invalid-email",
                phone = "123",
                password = "123",
                passwordConfirm = "456",
                firstNameError = "Le prénom est requis",
                lastNameError = "Le nom est requis",
                emailError = "Email invalide",
                phoneError = "Format invalide. Utilisez le format international (ex: +33612345678 ou +15141234567)",
                passwordError = "Minimum 8 caractères",
                passwordConfirmError = "Les mots de passe ne correspondent pas"
            ),
            onFirstNameChanged = {},
            onLastNameChanged = {},
            onEmailChanged = {},
            onPhoneChanged = {},
            onPasswordChanged = {},
            onPasswordConfirmChanged = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Register - Global Error", showBackground = true)
@Composable
private fun RegisterGlobalErrorPreview() {
    TrueStayTheme {
        RegisterScreenContent(
            userRole = UserRole.TENANT,
            uiState = RegisterUiState(
                firstName = "Jean",
                lastName = "Dupont",
                email = "jean.dupont@exemple.com",
                phone = "+33612345678",
                password = "password123",
                passwordConfirm = "password123",
                globalError = "Cet email est déjà utilisé"
            ),
            onFirstNameChanged = {},
            onLastNameChanged = {},
            onEmailChanged = {},
            onPhoneChanged = {},
            onPasswordChanged = {},
            onPasswordConfirmChanged = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Register - Loading", showBackground = true)
@Composable
private fun RegisterLoadingPreview() {
    TrueStayTheme {
        RegisterScreenContent(
            userRole = UserRole.TENANT,
            uiState = RegisterUiState(
                firstName = "Jean",
                lastName = "Dupont",
                email = "jean.dupont@exemple.com",
                phone = "+33612345678",
                password = "password123",
                passwordConfirm = "password123",
                isLoading = true
            ),
            onFirstNameChanged = {},
            onLastNameChanged = {},
            onEmailChanged = {},
            onPhoneChanged = {},
            onPasswordChanged = {},
            onPasswordConfirmChanged = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}