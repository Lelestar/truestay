package ca.uqac.inf865.truestay.presentation.common.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

/**
 * A versatile two-state button that supports icon, text, or any custom content via [content].
 * Use this single component across contexts (filters, segmented choices, etc.) to keep style consistent.
 */
@Composable
fun SelectableButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    centerContent: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val colors = LocalAppColors.current
    val container = if (selected) colors.black else colors.white
    val contentColor = if (selected) colors.white else colors.black
    val border = if (selected) colors.black else colors.grayBorder

    Card(
        modifier = modifier
            .defaultMinSize(minHeight = 33.dp)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        shape = AppShapes.medium,
        border = BorderStroke(1.dp, border),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = contentColor)
    ) {
        Row(
            modifier = (if (centerContent) Modifier.fillMaxWidth() else Modifier)
                .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall, Alignment.CenterHorizontally),
            content = content
        )
    }
}

/** Convenience wrapper for an icon */
@Composable
fun IconSelectableButton(
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    modifier: Modifier = Modifier,
    @StringRes contentDescriptionRes: Int? = null,
    enabled: Boolean = true
) {
    SelectableButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        TrueStayIcon(
            iconRes = iconRes,
            contentDescriptionRes = contentDescriptionRes,
            size = 24.dp,
            tint = if (selected) LocalAppColors.current.white else LocalAppColors.current.black
        )
    }
}

/** Convenience wrapper for a text-only choice. */
@Composable
fun TextSelectableButton(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    centerContent: Boolean = false
) {
    SelectableButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        centerContent = centerContent
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
fun SelectableButtonSelectedPreview() {
    TrueStayTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            TextSelectableButton(
                selected = true,
                onClick = {},
                text = "Selected"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectableButtonUnselectedPreview() {
    TrueStayTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            TextSelectableButton(
                selected = false,
                onClick = {},
                text = "Unselected"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IconSelectableButtonWithoutLabelPreview() {
    TrueStayTheme {
        Row(
            modifier = Modifier.padding(AppSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            IconSelectableButton(
                selected = true,
                onClick = {},
                iconRes = R.drawable.ic_funnel
            )
            IconSelectableButton(
                selected = false,
                onClick = {},
                iconRes = R.drawable.ic_funnel
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TextSelectableButtonGroupPreview() {
    TrueStayTheme {
        Row(
            modifier = Modifier.padding(AppSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            TextSelectableButton(
                selected = true,
                onClick = {},
                text = "All"
            )
            TextSelectableButton(
                selected = false,
                onClick = {},
                text = "Active"
            )
            TextSelectableButton(
                selected = false,
                onClick = {},
                text = "Inactive"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectableButtonDisabledPreview() {
    TrueStayTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            TextSelectableButton(
                selected = true,
                onClick = {},
                text = "Disabled Selected",
                enabled = false
            )
            TextSelectableButton(
                selected = false,
                onClick = {},
                text = "Disabled Unselected",
                enabled = false
            )
        }
    }
}
