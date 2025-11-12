package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

/**
 * TrueStay Custom Dropdown component
 *
 * @param selectedValue Currently selected value to display (null if nothing selected)
 * @param options List of available options
 * @param onOptionSelected Callback when an option is selected
 * @param modifier Compose modifier
 * @param label Optional label displayed above the dropdown
 * @param placeholder Text to display when no value is selected
 * @param enabled If false, the dropdown is disabled
 */
@Composable
fun <T> TrueStayDropdown(
    selectedValue: String?,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    optionLabel: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }

    var anchorWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    // Rotation animation for chevron
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevronRotation"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        // Label (optional)
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) LocalAppColors.current.black else LocalAppColors.current.grayDark
            )
        }

        // Dropdown trigger
        Box {
            val triggerModifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (enabled) LocalAppColors.current.grayLight
                    else LocalAppColors.current.grayLight.copy(alpha = 0.5f)
                )
                .clickable(enabled = enabled) { expanded = true }
                .onGloballyPositioned { coordinates ->
                    anchorWidthPx = coordinates.size.width
                }
                .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small)

            Row(
                modifier = triggerModifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selected value text or placeholder
                if (selectedValue != null || placeholder != null) {
                    Text(
                        text = selectedValue ?: placeholder ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (!enabled) {
                            LocalAppColors.current.grayDark
                        } else if (selectedValue == null) {
                            LocalAppColors.current.grayDark // Placeholder color
                        } else {
                            LocalAppColors.current.black // Selected value color
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Box(modifier = Modifier.weight(1f))
                }

                // Chevron icon
                TrueStayIcon(
                    iconRes = TrueStayIcons.ChevronDown,
                    contentDescriptionRes = null,
                    tint = LocalAppColors.current.grayDark,
                    size = 16.dp,
                    modifier = Modifier.rotate(chevronRotation)
                )
            }

            // Dropdown menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(density) { anchorWidthPx.toDp() })
                    .background(LocalAppColors.current.white)
                    .padding(vertical = 0.dp),
                shape = AppShapes.medium,
                offset = DpOffset(0.dp, AppSpacing.xsmall)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = optionLabel(option),
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalAppColors.current.black
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = LocalAppColors.current.black
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = AppSpacing.medium,
                            vertical = 0.dp
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
fun TrueStayDropdownBasicPreview() {
    TrueStayTheme {
        var selectedOption by remember { mutableStateOf("Prix") }
        TrueStayDropdown(
            selectedValue = selectedOption,
            options = listOf("Prix", "Date d'ajout", "Note", "Mise à jour"),
            onOptionSelected = { selectedOption = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayDropdownWithPlaceholderPreview() {
    TrueStayTheme {
        var selectedOption by remember { mutableStateOf<String?>(null) }
        TrueStayDropdown(
            selectedValue = selectedOption,
            options = listOf("Prix", "Date d'ajout", "Note", "Mise à jour"),
            onOptionSelected = { selectedOption = it },
            placeholder = "Sélectionner un critère de tri",
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayDropdownWithLabelPreview() {
    TrueStayTheme {
        var selectedOption by remember { mutableStateOf("Appartement") }
        TrueStayDropdown(
            selectedValue = selectedOption,
            options = listOf("Appartement", "Maison", "Studio", "Loft"),
            onOptionSelected = { selectedOption = it },
            label = "Type de logement",
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayDropdownDisabledPreview() {
    TrueStayTheme {
        TrueStayDropdown(
            selectedValue = "Option désactivée",
            options = listOf("Option 1", "Option 2", "Option 3"),
            onOptionSelected = {},
            label = "Champ désactivé",
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true, name = "Custom Object Example")
@Composable
fun TrueStayDropdownCustomObjectPreview() {
    data class SortOption(val id: String, val label: String)

    TrueStayTheme {
        var selectedOption by remember {
            mutableStateOf(SortOption("price", "Prix"))
        }

        val options = listOf(
            SortOption("price", "Prix"),
            SortOption("date", "Date d'ajout"),
            SortOption("rating", "Note"),
            SortOption("update", "Mise à jour")
        )

        TrueStayDropdown(
            selectedValue = selectedOption.label,
            options = options,
            onOptionSelected = { selectedOption = it },
            optionLabel = { it.label },
            label = "Trier par",
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}
