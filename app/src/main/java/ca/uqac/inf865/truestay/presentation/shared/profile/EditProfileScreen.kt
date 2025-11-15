package ca.uqac.inf865.truestay.presentation.shared.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R

// Tes composants personnalisés
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStaySwitch


import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons

// Pour ArrowBack ou autre navigation (selon où il se trouve)
import androidx.compose.material3.Icon
import androidx.compose.ui.res.stringResource
import ca.uqac.inf865.truestay.presentation.shared.auth.rememberCurrentUser


@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val colors = LocalAppColors.current

    // 🔥 Récupère l'utilisateur connecté
    val currentUser = rememberCurrentUser()

    var lastName by rememberSaveable { mutableStateOf("") }
    var firstName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    // 🔥 Remplit automatiquement les champs quand currentUser arrive
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            firstName = user.firstName ?: ""
            lastName = user.lastName ?: ""
            phone = user.phoneNumber ?: ""
            email = user.email ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large)
    ) {

        Spacer(modifier = Modifier.height(AppSpacing.large))

        // CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.grayBorder, RoundedCornerShape(16.dp))
                .background(colors.white, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {

            // NOM
            Text(
                text = stringResource(R.string.edit_last_name),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark
            )
            Spacer(modifier = Modifier.height(4.dp))

            ProfileEditInput(
                icon = TrueStayIcons.User,
                value = lastName,
                placeholder = stringResource(R.string.placeholder_last_name),
                onValueChange = { lastName = it }
            )

            Spacer(modifier = Modifier.height(AppSpacing.large))

            // PRÉNOM
            Text(
                text = stringResource(R.string.edit_first_name),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            ProfileEditInput(
                icon = TrueStayIcons.User,
                value = firstName,
                placeholder = stringResource(R.string.placeholder_first_name),
                onValueChange = { firstName = it }
            )

            Spacer(modifier = Modifier.height(AppSpacing.large))

            // TELEPHONE
            Text(
                text = stringResource(R.string.edit_phone),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            ProfileEditInput(
                icon = TrueStayIcons.Phone,
                value = phone,
                placeholder = stringResource(R.string.placeholder_phone),
                onValueChange = { phone = it }
            )

            Spacer(modifier = Modifier.height(AppSpacing.large))

            // EMAIL
            Text(
                text = stringResource(R.string.edit_email),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            ProfileEditInput(
                icon = TrueStayIcons.Mail,
                value = email,
                placeholder = stringResource(R.string.placeholder_email),
                onValueChange = { email = it },
                enabled = false    // désactivé comme dans le design
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.edit_change_email),
                color = colors.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { }
            )

            Spacer(modifier = Modifier.height(AppSpacing.large))

            // BOUTON
            TrueStayButton(
                text = stringResource(R.string.save_changes),
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}



@Composable
fun ProfileEditInput(
    icon: Int,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    val colors = LocalAppColors.current

    val textColor = if (enabled) colors.grayDark else colors.grayDark.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp) // 👈 TU PEUX METTRE 36dp, 32dp, ce que tu veux !
            .border(1.dp, colors.grayBorder, RoundedCornerShape(10.dp))
            .background(colors.grayLight.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            TrueStayIcon(
                iconRes = icon,
                contentDescriptionRes = null,
                tint = textColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Champ texte ultra compact
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            color = colors.grayDark.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
