package ca.uqac.inf865.truestay.presentation.shared.profile

import ca.uqac.inf865.truestay.presentation.shared.auth.AuthViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import ca.uqac.inf865.truestay.presentation.common.components.BadgeVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayBadge
import ca.uqac.inf865.truestay.presentation.common.components.TrueStaySwitch
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import androidx.compose.ui.tooling.preview.Preview
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.model.UserRole
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayDropdown
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import coil3.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onEditPassword: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showPictureSourceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshProfile()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfilePicture(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val uri = saveBitmapToCache(context, it)
            uri?.let { validUri -> viewModel.updateProfilePicture(validUri) }
        }
    }

    LaunchedEffect(uiState.profileUpdateErrorRes) {
        val messageRes = uiState.profileUpdateErrorRes ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(context.getString(messageRes))
        viewModel.clearProfileUpdateError()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (user != null) {
                val memberSinceText = remember(user.createdAt) {
                    formatMemberSinceDate(user.createdAt)
                }
                ProfileHeader(
                    name = "${user.firstName} ${user.lastName}".trim(),
                    role = if (user.role == UserRole.LANDLORD) stringResource(R.string.role_landlord) else stringResource(
                        R.string.role_tenant
                    ),
                    memberSince = memberSinceText,
                    profilePictureUrl = user.profilePictureUrl,
                    onEditProfilePicture = { showPictureSourceDialog = true }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading && user == null && uiState.errorMessage == null -> {
                        CircularProgressIndicator(
                            color = LocalAppColors.current.primary
                        )
                    }

                    uiState.errorMessage != null && user == null -> {
                        ProfileErrorState(
                            message = stringResource(R.string.auth_error_generic),
                            onRetry = viewModel::refreshProfile
                        )
                    }

                    else -> {
                        ProfileContent(
                            user = user,
                            isDarkTheme = uiState.isDarkTheme,
                            isEmailVerified = uiState.isEmailVerified,
                            onEditProfile = onEditProfile,
                            onEditPassword = onEditPassword,
                            onLogoutClick = {
                                authViewModel.logout()
                                onLogout()
                            },
                            onDarkThemeChanged = viewModel::setTheme,
                            onNotificationsChanged = { pushEnabled, emailEnabled ->
                                viewModel.updateNotificationPreferences(
                                    pushEnabled = pushEnabled,
                                    emailEnabled = emailEnabled
                                )
                            },
                            onTwoFactorChanged = viewModel::updateTwoFactorEnabled
                        )
                    }
                }

                if (showPictureSourceDialog) {
                    ProfilePictureSourceDialog(
                        onDismiss = { showPictureSourceDialog = false },
                        onPickFromGallery = {
                            showPictureSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        onTakePhoto = {
                            showPictureSourceDialog = false
                            cameraLauncher.launch(null)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: User?,
    isDarkTheme: Boolean,
    isEmailVerified: Boolean,
    onEditProfile: () -> Unit,
    onEditPassword: () -> Unit,
    onLogoutClick: () -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
    onNotificationsChanged: (pushEnabled: Boolean, emailEnabled: Boolean) -> Unit,
    onTwoFactorChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.large))

        AccountCard(
            name = "${user?.firstName.orEmpty()} ${user?.lastName.orEmpty()}".trim(),
            email = user?.email.orEmpty(),
            phone = user?.phoneNumber.orEmpty(),
            onEditProfile = onEditProfile
        )

        Spacer(modifier = Modifier.height(AppSpacing.large))

        VerificationCard(isEmailVerified = isEmailVerified)

        Spacer(modifier = Modifier.height(AppSpacing.large))

        PreferencesCard(
            user = user,
            isDarkTheme = isDarkTheme,
            onDarkThemeChanged = onDarkThemeChanged,
            onNotificationsChanged = onNotificationsChanged
        )

        Spacer(modifier = Modifier.height(AppSpacing.large))

        SecurityCard(
            onEditPassword = onEditPassword,
            twoFactorEnabled = user?.twoFactorEnabled ?: false,
            onTwoFactorChanged = onTwoFactorChanged
        )

        Spacer(modifier = Modifier.height(AppSpacing.large))

        HelpCard()

        Spacer(modifier = Modifier.height(AppSpacing.large))

        TrueStayButton(
            text = stringResource(R.string.profile_logout),
            leadingIcon = R.drawable.ic_log_out,
            onClick = onLogoutClick,
            variant = ButtonVariant.DANGER,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppSpacing.large))
    }
}

// Display-only row with icon, label and value
@Composable
fun AccountInfoRow(
    icon: Int,
    label: String,
    value: String,
) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.large),
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 56.dp)
            .padding(vertical = AppSpacing.small)
    ) {
        TrueStayIcon(
            iconRes = icon,
            contentDescriptionRes = null,
            tint = colors.grayDark,
            size = 20.dp
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.grayDark
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = colors.black
            )
        }
    }
}

// Display-only row with icon, one main text and a trailing slot (badge, switch, etc.)
@Composable
fun AccountInfoWithTrailingRow(
    icon: Int,
    text: String,
    trailingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.large),
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 56.dp)
            .padding(vertical = AppSpacing.small)
    ) {
        TrueStayIcon(
            iconRes = icon,
            contentDescriptionRes = null,
            tint = colors.grayDark,
            size = 20.dp
        )

        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = colors.black,
            modifier = Modifier.weight(1f)
        )

        Box(
            contentAlignment = Alignment.CenterEnd
        ) {
            trailingContent()
        }
    }
}

@Composable
fun ProfileHeader(
    name: String,
    role: String,
    memberSince: String,
    profilePictureUrl: String?,
    onEditProfilePicture: () -> Unit
) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primary)
            .padding(AppSpacing.large)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            // Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clickable(onClick = onEditProfilePicture)
            ) {

                if (!profilePictureUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profilePictureUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(colors.primaryLight, CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.img_placeholder),
                        error = painterResource(R.drawable.img_placeholder),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(colors.primaryLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.white
                        )
                    }
                }

                // Edit icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .background(colors.white, CircleShape)
                        .clickable(onClick = onEditProfilePicture),
                    contentAlignment = Alignment.Center
                ) {
                    TrueStayIcon(
                        iconRes = TrueStayIcons.SquarePen,
                        contentDescriptionRes = null,
                        size = 16.dp,
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.large))

            Column {
                // Name and role
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.white
                    )

                    Spacer(modifier = Modifier.width(AppSpacing.small))

                    TrueStayBadge(
                        text = role,
                        variant = BadgeVariant.INFO,
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.small))

                Text(
                    text = stringResource(R.string.profile_member_since) + " $memberSince",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.white
                )
            }
        }
    }
}

@Composable
fun AccountCard(
    name: String,
    email: String,
    phone: String,
    onEditProfile: () -> Unit
) {
    val colors = LocalAppColors.current

    TrueStayCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_account_title),
            style = MaterialTheme.typography.headlineSmall,
            color = colors.black
        )

        Spacer(modifier = Modifier.height(AppSpacing.small))

        AccountInfoRow(
            icon = TrueStayIcons.User,
            label = stringResource(R.string.profile_account_name),
            value = name
        )
        HorizontalDivider(color = colors.grayBorder, thickness = 1.dp)
        AccountInfoRow(
            icon = TrueStayIcons.Mail,
            label = stringResource(R.string.profile_account_email),
            value = email
        )
        HorizontalDivider(color = colors.grayBorder, thickness = 1.dp)
        AccountInfoRow(
            icon = TrueStayIcons.Phone,
            label = stringResource(R.string.profile_account_phone),
            value = phone
        )

        Spacer(modifier = Modifier.height(AppSpacing.small))

        TrueStayButton(
            text = stringResource(R.string.profile_account_edit_info),
            onClick = onEditProfile,
            variant = ButtonVariant.PRIMARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun VerificationCard(
    isEmailVerified: Boolean
) {
    val colors = LocalAppColors.current

    TrueStayCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_verification_title),
            style = MaterialTheme.typography.headlineSmall,
            color = colors.black
        )

        Spacer(modifier = Modifier.height(AppSpacing.small))

        // Email
        AccountInfoWithTrailingRow(
            icon = TrueStayIcons.Mail,
            text = stringResource(R.string.profile_account_email),
            trailingContent = {
                TrueStayBadge(
                    text = if (isEmailVerified) {
                        stringResource(R.string.profile_verification_verified)
                    } else {
                        stringResource(R.string.profile_verification_unverified)
                    },
                    variant = if (isEmailVerified) BadgeVariant.SUCCESS else BadgeVariant.WARNING,
                )
            }
        )

        HorizontalDivider(color = colors.grayBorder, thickness = 1.dp)

        // Phone
        AccountInfoWithTrailingRow(
            icon = TrueStayIcons.Phone,
            text = stringResource(R.string.profile_account_phone),
            trailingContent = {
                TrueStayBadge(
                    text = stringResource(R.string.profile_verification_unverified),
                    variant = BadgeVariant.WARNING,
                )
            }
        )
    }
}

@Composable
fun PreferencesCard(
    user: User?,
    isDarkTheme: Boolean,
    onDarkThemeChanged: (Boolean) -> Unit,
    onNotificationsChanged: (pushEnabled: Boolean, emailEnabled: Boolean) -> Unit
) {
    val colors = LocalAppColors.current

    var pushNotif by rememberSaveable { mutableStateOf(false) }
    var emailNotif by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(user?.id) {
        if (user != null) {
            pushNotif = user.pushNotificationsEnabled
            emailNotif = user.emailNotificationsEnabled
        }
    }

    TrueStayCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_preferences_title),
            style = MaterialTheme.typography.headlineSmall,
            color = colors.black
        )

        Spacer(modifier = Modifier.height(AppSpacing.small))

        // Dark theme
        AccountInfoWithTrailingRow(
            icon = TrueStayIcons.SunMoon,
            text = stringResource(R.string.profile_preferences_dark_theme),
            trailingContent = {
                TrueStaySwitch(
                    checked = isDarkTheme,
                    onCheckedChange = onDarkThemeChanged
                )
            }
        )

        HorizontalDivider(color = colors.grayBorder, thickness = 1.dp)

        // Language
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.small)
        ) {
            TrueStayIcon(
                iconRes = TrueStayIcons.Globe,
                contentDescriptionRes = null,
                tint = colors.grayDark,
                size = 20.dp
            )

            Spacer(modifier = Modifier.width(AppSpacing.large))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_preferences_language),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.black
                )

                Spacer(modifier = Modifier.height(AppSpacing.xsmall))

                TrueStayDropdown(
                    options = listOf("Français"),
                    selectedValue = "Français",
                    onOptionSelected = { }
                )
            }
        }

        HorizontalDivider(color = colors.grayBorder, thickness = 1.dp)

        // Push notifications
        AccountInfoWithTrailingRow(
            icon = TrueStayIcons.Bell,
            text = stringResource(R.string.profile_preferences_push),
            trailingContent = {
                TrueStaySwitch(
                    checked = pushNotif,
                    onCheckedChange = {
                        pushNotif = it
                        onNotificationsChanged(pushNotif, emailNotif)
                    }
                )
            }
        )

        HorizontalDivider(color = colors.grayBorder, thickness = 1.dp)

        // Email notifications
        AccountInfoWithTrailingRow(
            icon = TrueStayIcons.Mail,
            text = stringResource(R.string.profile_preferences_email_notifications),
            trailingContent = {
                TrueStaySwitch(
                    checked = emailNotif,
                    onCheckedChange = {
                        emailNotif = it
                        onNotificationsChanged(pushNotif, emailNotif)
                    }
                )
            }
        )
    }
}

@Composable
fun SecurityCard(
    onEditPassword: () -> Unit,
    twoFactorEnabled: Boolean,
    onTwoFactorChanged: (Boolean) -> Unit
) {
    val colors = LocalAppColors.current

    TrueStayCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_security_title),
            style = MaterialTheme.typography.headlineSmall,
            color = colors.black
        )

        Spacer(modifier = Modifier.height(AppSpacing.small))

        // Update password
        AccountInfoWithTrailingRow(
            icon = TrueStayIcons.Key,
            text = stringResource(R.string.profile_security_change_password),
            trailingContent = { },
            modifier = Modifier.clickable { onEditPassword() }
        )

        HorizontalDivider(color = colors.grayBorder, thickness = 1.dp)

        // 2FA
        AccountInfoWithTrailingRow(
            icon = TrueStayIcons.ShieldCheck,
            text = stringResource(R.string.profile_security_2fa),
            trailingContent = {
                TrueStaySwitch(
                    checked = twoFactorEnabled,
                    onCheckedChange = onTwoFactorChanged
                )
            }
        )
    }
}

@Composable
private fun ProfileErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = LocalAppColors.current.error,
            textAlign = TextAlign.Center
        )

        TrueStayButton(
            text = stringResource(R.string.common_retry),
            onClick = onRetry,
            variant = ButtonVariant.SECONDARY
        )
    }
}

@Composable
private fun ProfilePictureSourceDialog(
    onDismiss: () -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.profile_picture_choose_source_title)) },
        text = { Text(text = stringResource(R.string.profile_picture_choose_source_message), color = LocalAppColors.current.grayDark) },
        confirmButton = {
            TrueStayButton(
                text = stringResource(R.string.profile_picture_gallery),
                onClick = { onPickFromGallery() },
                variant = ButtonVariant.SECONDARY,
                leadingIcon = TrueStayIcons.Image
            )
        },
        dismissButton = {
            TrueStayButton(
                text = stringResource(R.string.profile_picture_camera),
                onClick = { onTakePhoto() },
                variant = ButtonVariant.SECONDARY,
                leadingIcon = TrueStayIcons.Camera
            )
        },
        shape = AppShapes.large,
        containerColor = LocalAppColors.current.white,
    )
}

private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.toUri()
    } catch (_: Exception) {
        null
    }
}

private fun formatMemberSinceDate(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    return try {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        formatter.format(date)
    } catch (_: Exception) {
        ""
    }
}


@Composable
fun HelpCard() {
    val colors = LocalAppColors.current

    TrueStayCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_help_title),
            style = MaterialTheme.typography.headlineSmall,
            color = colors.black
        )

        Spacer(modifier = Modifier.height(AppSpacing.small))

        // Help center
        HelpItem(
            label = stringResource(R.string.profile_help_faq),
            icon = TrueStayIcons.ChevronRight
        )

        HorizontalDivider(color = colors.grayBorder, thickness = 1.dp)

        // Contact us
        HelpItem(
            label = stringResource(R.string.profile_help_contact),
            icon = TrueStayIcons.ChevronRight
        )

        HorizontalDivider(color = colors.grayBorder, thickness = 1.dp)

        // Conditions d'utilisation
        HelpItem(
            label = stringResource(R.string.profile_help_terms),
            icon = TrueStayIcons.ChevronRight
        )
    }
}

@Composable
fun HelpItem(label: String, icon: Int) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .sizeIn(minHeight = 56.dp)
            .padding(vertical = AppSpacing.small)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = colors.black,
            modifier = Modifier.weight(1f)
        )

        TrueStayIcon(
            iconRes = icon,
            contentDescriptionRes = null,
            tint = colors.grayDark
        )
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true, name = "Profile - Content")
@Composable
private fun ProfileScreenContentPreview() {
    TrueStayTheme {
        val sampleUser = User(
            id = "user_1",
            email = "jean.dupont@example.com",
            firstName = "Jean",
            lastName = "Dupont",
            role = UserRole.TENANT,
            phoneNumber = "+33612345678",
            twoFactorEnabled = true,
            emailNotificationsEnabled = true,
            pushNotificationsEnabled = false,
            profilePictureUrl = "https://example.com/avatar.jpg"
        )

        Column(modifier = Modifier.fillMaxSize()) {
            ProfileHeader(
                name = "${sampleUser.firstName} ${sampleUser.lastName}",
                role = stringResource(R.string.role_tenant),
                memberSince = formatMemberSinceDate(System.currentTimeMillis()),
                profilePictureUrl = sampleUser.profilePictureUrl,
                onEditProfilePicture = {}
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppSpacing.large)
            ) {
                ProfileContent(
                    user = sampleUser,
                    isDarkTheme = false,
                    isEmailVerified = true,
                    onEditProfile = {},
                    onEditPassword = {},
                    onLogoutClick = {},
                    onDarkThemeChanged = {},
                    onNotificationsChanged = { _, _ -> },
                    onTwoFactorChanged = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Profile - Loading")
@Composable
private fun ProfileScreenLoadingPreview() {
    TrueStayTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = LocalAppColors.current.primary
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Profile - Error")
@Composable
private fun ProfileScreenErrorPreview() {
    TrueStayTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                ProfileErrorState(
                    message = stringResource(R.string.auth_error_generic),
                    onRetry = {}
                )
            }
        }
    }
}

