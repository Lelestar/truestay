package ca.uqac.inf865.truestay.presentation.shared.profile

import ca.uqac.inf865.truestay.presentation.shared.auth.AuthViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.KeyboardType

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
        ) {





            AccountCard(
                userId = currentUser?.id ?: "",
                initialName = "${currentUser?.firstName ?: ""} ${currentUser?.lastName ?: ""}".trim(),
                initialEmail = currentUser?.email ?: "",
                initialPhone = currentUser?.phoneNumber ?: "",
                onSaveClick = { firstName, lastName, email, phone ->
                    val id = currentUser?.id ?: return@onSaveClick

                    viewModel.updateUser(
                        userId = id,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phoneNumber = phone
                    )
                }
            )



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
