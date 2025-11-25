package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

/**
 * Button types
 */
enum class ButtonVariant {
    PRIMARY,
    SECONDARY,
    DANGER
}

/**
 * TrueStay Custom Button
 *
 * @param text Button text
 * @param onClick Click action
 * @param modifier Compose modifier
 * @param variant Button type (primary, secondary, danger)
 * @param enabled If false, the button is disabled
 * @param leadingIcon Icon displayed to the left of the text (optional)
 * @param iconTint Custom color for the icon (optional, defaults to contentColor)
 * @param isLoading If true, shows a loader and disables the button
 */
@Composable
fun TrueStayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    leadingIcon: Int? = null,
    iconTint: androidx.compose.ui.graphics.Color? = null,
    isLoading: Boolean = false
) {
    val backgroundColor = when {
        !enabled || isLoading -> LocalAppColors.current.grayMedium
        variant == ButtonVariant.PRIMARY -> LocalAppColors.current.primary
        variant == ButtonVariant.SECONDARY -> LocalAppColors.current.white
        variant == ButtonVariant.DANGER -> LocalAppColors.current.error
        else -> LocalAppColors.current.primary
    }

    val contentColor = when (variant) {
        ButtonVariant.SECONDARY -> LocalAppColors.current.primary
        else -> LocalAppColors.current.white
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = LocalAppColors.current.grayMedium,
            disabledContentColor = LocalAppColors.current.white
        ),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(AppSpacing.medium, AppSpacing.small),
        border = if (variant == ButtonVariant.SECONDARY) {
            androidx.compose.foundation.BorderStroke(1.dp, LocalAppColors.current.grayBorder)
        } else null
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    TrueStayIcon(
                        iconRes = leadingIcon,
                        contentDescriptionRes = null,
                        tint = iconTint ?: contentColor,
                        size = 16.dp
                    )
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
fun TrueStayButtonPrimaryPreview() {
    TrueStayTheme {
        TrueStayButton(
            text = "Primary button",
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayButtonSecondaryPreview() {
    TrueStayTheme {
        TrueStayButton(
            text = "Secondary button",
            onClick = {},
            variant = ButtonVariant.SECONDARY,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayButtonDangerPreview() {
    TrueStayTheme {
        TrueStayButton(
            text = "Danger button",
            onClick = {},
            variant = ButtonVariant.DANGER,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayButtonDisabledPreview() {
    TrueStayTheme {
        TrueStayButton(
            text = "Disabled button",
            onClick = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayButtonLoadingPreview() {
    TrueStayTheme {
        TrueStayButton(
            text = "Loading button",
            onClick = {},
            isLoading = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayButtonWithIconPreview() {
    TrueStayTheme {
        TrueStayButton(
            text = "With icon",
            onClick = {},
            leadingIcon = R.drawable.ic_plus,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}