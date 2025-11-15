package ca.uqac.inf865.truestay.presentation.shared.profile

import ca.uqac.inf865.truestay.presentation.shared.auth.AuthViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.shared.auth.rememberCurrentUser
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.material3.Icon
import androidx.compose.runtime.saveable.rememberSaveable
import ca.uqac.inf865.truestay.presentation.common.components.TrueStaySwitch


// --------------------------------------------------
// 1. HEADER BLEU
// --------------------------------------------------
@Composable
fun ProfileHeader(
    name: String,
    role: String,
    memberSince: String
) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primary)
            .padding(AppSpacing.large)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            // Avatar rond
            Box(modifier = Modifier.size(64.dp)) {

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(colors.white.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = name.firstOrNull()?.uppercase() ?: "?",

                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.white
                    )
                }

                // Icône crayon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .background(colors.white, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✏️", color = colors.primary)
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.large))

            Column {

                // Nom + badge rôle
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.white
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .background(colors.white, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = role,
                            color = colors.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Membre depuis $memberSince",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.white.copy(alpha = 0.9f)
                )
            }
        }
    }
}

// --------------------------------------------------
// 2. INPUT AVEC ICONE
// --------------------------------------------------
@Composable
fun AccountInputRow(
    icon: Int,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icône TrueStay
        TrueStayIcon(
            iconRes = icon,
            contentDescriptionRes = null,
            tint = colors.grayDark
        )

        // TextField flottant
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Normal   // ← PAS GRAS
                    )
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold           // ← LA VALEUR EN GRAS
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = colors.primary,
                unfocusedIndicatorColor = colors.grayBorder,
                focusedContainerColor = colors.white,
                unfocusedContainerColor = colors.white,
                cursorColor = colors.primary
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = when (label) {
                    "Téléphone" -> KeyboardType.Phone
                    "E-mail", "Email" -> KeyboardType.Email
                    else -> KeyboardType.Text
                }
            ),
            modifier = Modifier.weight(1f)
        )

    }
}

// --------------------------------------------------
// 3. CARTE BLANCHE : FORMULAIRE
// --------------------------------------------------
@Composable
fun AccountCard(
    userId: String,
    initialName: String,
    initialEmail: String,
    initialPhone: String,
    onSaveClick: (String, String, String, String) -> Unit

) {
    val colors = LocalAppColors.current

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }

    // 🔥 Met à jour les champs quand currentUser arrive
    LaunchedEffect(initialName, initialEmail, initialPhone) {
        name = initialName
        email = initialEmail
        phoneNumber = initialPhone
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.grayBorder, RoundedCornerShape(16.dp))
            .background(colors.white, RoundedCornerShape(16.dp))
            .padding(AppSpacing.large)
    ) {

        Text(
            text = "Compte",
            style = MaterialTheme.typography.titleMedium,
            color = colors.grayDark
        )

        Spacer(modifier = Modifier.height(AppSpacing.medium))

        AccountInputRow(
            icon = TrueStayIcons.User,
            label = "Nom",
            value = name,
            onValueChange = { name = it }
        )

        Spacer(modifier = Modifier.height(AppSpacing.medium))

        AccountInputRow(
            icon = TrueStayIcons.Mail,
            label = "E-mail",
            value = email,
            onValueChange = { email = it }
        )

        Spacer(modifier = Modifier.height(AppSpacing.medium))

        AccountInputRow(
            icon = TrueStayIcons.Phone,
            label = "Téléphone",
            value = phoneNumber,
            onValueChange = { phoneNumber = it }
        )

        Spacer(modifier = Modifier.height(AppSpacing.large))

        TrueStayButton(
            text = "Enregistrer les modifications",
            onClick = {
                onSaveClick(
                    name.substringBefore(" "),
                    name.substringAfter(" ", ""),
                    email,
                    phoneNumber
                )
            },
            variant = ButtonVariant.PRIMARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --------------------------------------------------
// 4. ÉCRAN PROFIL COMPLET
// --------------------------------------------------
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {


    Column(modifier = Modifier.fillMaxSize()) {
        val currentUser = rememberCurrentUser()

        ProfileHeader(
            name = "${currentUser?.firstName ?: ""} ${currentUser?.lastName ?: ""}".trim(),
            role = "Propriétaire",
            memberSince = "octobre 2025"
        )

        Spacer(modifier = Modifier.height(AppSpacing.large))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.large)
                .verticalScroll(rememberScrollState())
        ) {


            AccountCard(
                userId = currentUser?.id ?: "",
                initialName = "${currentUser?.firstName ?: ""} ${currentUser?.lastName ?: ""}".trim(),
                initialEmail = currentUser?.email ?: "",
                initialPhone = currentUser?.phoneNumber ?: "",
                onSaveClick = { firstName, lastName, email, phone ->
                    val id = currentUser?.id
                    if(id != null) {
                        viewModel.updateUser(
                            userId = id,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            phoneNumber = phone
                        )
                    }


                }
            )

            Spacer(modifier = Modifier.height(AppSpacing.large))

            VerificationCard()

            Spacer(modifier = Modifier.height(AppSpacing.large))

            PreferencesCard()

            Spacer(modifier = Modifier.height(AppSpacing.large))

            SecuriteCard()

            Spacer(modifier = Modifier.height(AppSpacing.large))

            AideCard()

            Spacer(modifier = Modifier.height(AppSpacing.large))

            TrueStayButton(
                text = "Se déconnecter",
                leadingIcon = R.drawable.ic_log_out,
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                variant = ButtonVariant.DANGER,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun VerificationCard() {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.grayBorder, RoundedCornerShape(16.dp))
            .background(colors.white, RoundedCornerShape(16.dp))
            .padding(AppSpacing.large)
    ) {

        Text(
            text = "Vérifications",
            style = MaterialTheme.typography.titleMedium,
            color = colors.grayDark
        )

        Spacer(modifier = Modifier.height(AppSpacing.medium))

        // EMAIL
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacing.small)
        ) {

            TrueStayIcon(
                iconRes = TrueStayIcons.Mail,
                contentDescriptionRes = null,
                tint = colors.grayDark
            )

            Spacer(modifier = Modifier.width(AppSpacing.medium))

            Text(
                text = "Email",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .background(colors.yellow, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "Non vérifié",
                    color = colors.black,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Divider(color = colors.grayBorder, thickness = 1.dp)

        // TELEPHONE
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacing.small)
        ) {

            TrueStayIcon(
                iconRes = TrueStayIcons.Phone,
                contentDescriptionRes = null,
                tint = colors.grayDark
            )

            Spacer(modifier = Modifier.width(AppSpacing.medium))

            Text(
                text = "Téléphone",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .background(colors.yellow, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "Non vérifié",
                    color = colors.black,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}



@Composable
fun PreferencesCard() {
    val colors = LocalAppColors.current

    var darkTheme by rememberSaveable { mutableStateOf(false) }
    var pushNotif by rememberSaveable { mutableStateOf(false) }
    var emailNotif by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.grayBorder, RoundedCornerShape(16.dp))
            .background(colors.white, RoundedCornerShape(16.dp))
            .padding(AppSpacing.large)
    ) {

        Text(
            text = "Préférences",
            style = MaterialTheme.typography.titleMedium,
            color = colors.grayDark
        )

        Spacer(modifier = Modifier.height(AppSpacing.medium))

        // 1) Thème sombre
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TrueStayIcon(
                iconRes = TrueStayIcons.SunMoon,
                contentDescriptionRes = null,
                tint = colors.grayDark
            )

            Spacer(modifier = Modifier.width(AppSpacing.medium))

            Text(
                text = "Thème sombre",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark,
                modifier = Modifier.weight(1f)
            )

            TrueStaySwitch(
                checked = darkTheme,
                onCheckedChange = { darkTheme = it }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.large))
        Divider(color = colors.grayBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(AppSpacing.large))

        // 2) Langue + select
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            TrueStayIcon(
                iconRes = TrueStayIcons.Globe,
                contentDescriptionRes = null,
                tint = colors.grayDark
            )

            Spacer(modifier = Modifier.width(AppSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Langue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.grayBorder, RoundedCornerShape(12.dp))
                        .background(colors.grayLight.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Français",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.grayDark
                        )
                        TrueStayIcon(
                            iconRes = TrueStayIcons.ChevronDown,
                            contentDescriptionRes = null,
                            tint = colors.grayDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.large))
        Divider(color = colors.grayBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(AppSpacing.large))

        // 3) Notification push
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TrueStayIcon(
                iconRes = TrueStayIcons.Bell,
                contentDescriptionRes = null,
                tint = colors.grayDark
            )

            Spacer(modifier = Modifier.width(AppSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notification push",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayDark
                )
                Text(
                    text = "Nouveaux avis, favoris, rappels EDL",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.grayDark.copy(alpha = 0.6f)
                )
            }

            TrueStaySwitch(
                checked = pushNotif,
                onCheckedChange = { pushNotif = it }
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.large))
        Divider(color = colors.grayBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(AppSpacing.large))

        // 4) Notifications email
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TrueStayIcon(
                iconRes = TrueStayIcons.Mail,
                contentDescriptionRes = null,
                tint = colors.grayDark
            )

            Spacer(modifier = Modifier.width(AppSpacing.medium))

            Text(
                text = "Notifications email",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark,
                modifier = Modifier.weight(1f)
            )

            TrueStaySwitch(
                checked = emailNotif,
                onCheckedChange = { emailNotif = it }
            )
        }
    }
}

@Composable
fun SecuriteCard() {
    val colors = LocalAppColors.current
    var twoFA by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.grayBorder, RoundedCornerShape(16.dp))
            .background(colors.white, RoundedCornerShape(16.dp))
            .padding(AppSpacing.large)
    ) {

        Text(
            text = "Sécurité",
            style = MaterialTheme.typography.titleMedium,
            color = colors.grayDark
        )

        Spacer(modifier = Modifier.height(AppSpacing.large))

        // Modifier mot de passe
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
        ) {

            TrueStayIcon(
                iconRes = TrueStayIcons.Key,
                contentDescriptionRes = null,
                tint = colors.grayDark
            )

            Spacer(modifier = Modifier.width(AppSpacing.medium))

            Text(
                text = "Modifier le mot de passe",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.large))
        Divider(color = colors.grayBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(AppSpacing.large))

        // Double authentification
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TrueStayIcon(
                iconRes = TrueStayIcons.ShieldCheck,
                contentDescriptionRes = null,
                tint = colors.grayDark
            )

            Spacer(modifier = Modifier.width(AppSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Double authentification",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayDark
                )
                Text(
                    text = "Sécurise davantage votre compte",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.grayDark.copy(alpha = 0.6f)
                )
            }

            TrueStaySwitch(
                checked = twoFA,
                onCheckedChange = { twoFA = it }
            )
        }
    }
}


@Composable
fun AideCard() {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.grayBorder, RoundedCornerShape(16.dp))
            .background(colors.white, RoundedCornerShape(16.dp))
            .padding(AppSpacing.large)
    ) {

        Text(
            text = "Aide",
            style = MaterialTheme.typography.titleMedium,
            color = colors.grayDark
        )

        Spacer(modifier = Modifier.height(AppSpacing.large))

        // Centre d'aide
        AideItem(
            label = "Centre d'aide & FAQ",
            icon = TrueStayIcons.ChevronRight
        )

        Spacer(modifier = Modifier.height(AppSpacing.large))
        Divider(color = colors.grayBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(AppSpacing.large))

        // Nous contacter
        AideItem(
            label = "Nous contacter",
            icon = TrueStayIcons.ChevronRight
        )

        Spacer(modifier = Modifier.height(AppSpacing.large))
        Divider(color = colors.grayBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(AppSpacing.large))

        // Conditions d'utilisation
        AideItem(
            label = "Conditions d’utilisation",
            icon = TrueStayIcons.ChevronRight
        )
    }
}

@Composable
fun AideItem(label: String, icon: Int) {
    val colors = LocalAppColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.grayDark,
            modifier = Modifier.weight(1f)
        )

        TrueStayIcon(
            iconRes = icon,
            contentDescriptionRes = null,
            tint = colors.grayDark
        )
    }
}

