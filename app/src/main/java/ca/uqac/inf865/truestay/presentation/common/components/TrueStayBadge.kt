package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

/**
 * Badge variants
 */
enum class BadgeVariant {
    SUCCESS,
    WARNING,
    ERROR,
    INFO
}

/**
 * TrueStay Custom Badge
 *
 * @param text Badge text
 * @param variant Badge variant (success, warning, error, neutral)
 * @param modifier Compose modifier
 */
@Composable
fun TrueStayBadge(
    text: String,
    variant: BadgeVariant,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    val backgroundColor = when (variant) {
        BadgeVariant.SUCCESS -> colors.success
        BadgeVariant.WARNING -> colors.warning
        BadgeVariant.ERROR -> colors.error
        BadgeVariant.INFO -> colors.white
    }

    val textColor = when (variant) {
        BadgeVariant.INFO -> colors.primary
        else -> colors.white
    }

    val borderModifier = if (variant == BadgeVariant.INFO) {
        Modifier.border(1.dp, colors.grayBorder, MaterialTheme.shapes.medium)
    } else {
        Modifier
    }

    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = textColor,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .then(borderModifier)
            .padding(
                horizontal = AppSpacing.small,
                vertical = AppSpacing.xsmall
            )
    )
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
fun TrueStayBadgeSuccessPreview() {
    TrueStayTheme {
        TrueStayBadge(
            text = "Disponible",
            variant = BadgeVariant.SUCCESS,
            modifier = Modifier.padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayBadgeWarningPreview() {
    TrueStayTheme {
        TrueStayBadge(
            text = "En attente",
            variant = BadgeVariant.WARNING,
            modifier = Modifier.padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayBadgeErrorPreview() {
    TrueStayTheme {
        TrueStayBadge(
            text = "Indisponible",
            variant = BadgeVariant.ERROR,
            modifier = Modifier.padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayBadgeNeutralPreview() {
    TrueStayTheme {
        TrueStayBadge(
            text = "Neutre",
            variant = BadgeVariant.INFO,
            modifier = Modifier.padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayBadgeAllVariantsPreview() {
    TrueStayTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            TrueStayBadge(text = "Succès", variant = BadgeVariant.SUCCESS)
            TrueStayBadge(text = "Avertissement", variant = BadgeVariant.WARNING)
            TrueStayBadge(text = "Erreur", variant = BadgeVariant.ERROR)
            TrueStayBadge(text = "Neutre", variant = BadgeVariant.INFO)
        }
    }
}

