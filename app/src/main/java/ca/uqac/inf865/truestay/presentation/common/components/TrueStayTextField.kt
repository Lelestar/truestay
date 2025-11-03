package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

@Composable
fun TrueStayTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isPassword: Boolean = false,
    leadingIcon: Int? = null,
    errorMessage: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = when {
                errorMessage != null -> LocalAppColors.current.error
                !enabled -> LocalAppColors.current.grayDark else -> LocalAppColors.current.black
            }
        )

        // TextField container
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = if (enabled) LocalAppColors.current.black else LocalAppColors.current.grayDark
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = if (keyboardActions != KeyboardActions.Default) {
                keyboardActions
            } else {
                KeyboardActions(
                    onDone = { onImeAction?.invoke() },
                    onNext = { onImeAction?.invoke() },
                    onGo = { onImeAction?.invoke() },
                    onSearch = { onImeAction?.invoke() }
                )
            },
            enabled = enabled,
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            cursorBrush = SolidColor(
                if (errorMessage != null) LocalAppColors.current.error else LocalAppColors.current.primary
            ),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            if (enabled) LocalAppColors.current.grayLight
                            else LocalAppColors.current.grayLight.copy(alpha = 0.5f)
                        )
                        .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Leading icon
                    when {
                        isPassword -> {
                            TrueStayIcon(
                                iconRes = TrueStayIcons.Lock,
                                contentDescriptionRes = null,
                                tint = LocalAppColors.current.grayDark,
                                size = 16.dp
                            )
                        }
                        leadingIcon != null -> {
                            TrueStayIcon(
                                iconRes = leadingIcon,
                                contentDescriptionRes = null,
                                tint = LocalAppColors.current.grayDark,
                                size = 16.dp
                            )
                        }
                    }

                    // Text field content
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Placeholder
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (enabled) LocalAppColors.current.grayDark else LocalAppColors.current.grayLight )
                        }

                        // Actual text input
                        innerTextField()
                    }

                    // Trailing icon (password toggle)
                    if (isPassword) {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.size(16.dp)
                        ) {
                            TrueStayIcon(
                                iconRes = if (passwordVisible) {
                                    TrueStayIcons.EyeOff
                                } else {
                                    TrueStayIcons.Eye
                                },
                                tint = LocalAppColors.current.grayDark,
                                contentDescriptionRes = if (passwordVisible) {
                                    R.string.cd_hide_password
                                } else {
                                    R.string.cd_show_password
                                },
                                size = 16.dp
                            )
                        }
                    }
                }
            }
        )

        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = LocalAppColors.current.error
            )
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
fun TrueStayTextFieldBasicPreview() {
    TrueStayTheme {
        var text by remember { mutableStateOf("") }
        TrueStayTextField(
            value = text,
            onValueChange = { text = it },
            label = "Username",
            placeholder = "Enter your username",
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayTextFieldPasswordPreview() {
    TrueStayTheme {
        var password by remember { mutableStateOf("") }
        TrueStayTextField(
            value = password,
            onValueChange = { password = it },
            label = "Mot de passe",
            placeholder = "Entrez votre mot de passe",
            isPassword = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayTextFieldEmailPreview() {
    TrueStayTheme {
        var email by remember { mutableStateOf("") }
        TrueStayTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "exemple@email.com",
            leadingIcon = TrueStayIcons.Mail,
            keyboardType = KeyboardType.Email,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayTextFieldErrorPreview() {
    TrueStayTheme {
        var text by remember { mutableStateOf("") }
        TrueStayTextField(
            value = text,
            onValueChange = { text = it },
            label = "Email",
            placeholder = "exemple@email.com",
            leadingIcon = TrueStayIcons.Mail,
            errorMessage = "Invalid email format",
            keyboardType = KeyboardType.Email,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayTextFieldDisabledPreview() {
    TrueStayTheme {
        TrueStayTextField(
            value = "Disabled",
            onValueChange = {},
            label = "Disabled Field",
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayTextFieldPhonePreview() {
    TrueStayTheme {
        var phone by remember { mutableStateOf("") }
        TrueStayTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Phone",
            placeholder = "+1 (555) 123-4567",
            leadingIcon = TrueStayIcons.Phone,
            keyboardType = KeyboardType.Phone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}