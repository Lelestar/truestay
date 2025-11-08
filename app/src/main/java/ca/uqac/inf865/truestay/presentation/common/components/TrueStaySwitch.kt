package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

/**
 * Custom switch component matching the TrueStay design
 *
 * @param checked Current state of the switch
 * @param onCheckedChange Callback when the switch state changes
 * @param modifier Compose modifier
 * @param enabled If false, the switch is disabled
 * @param activeColor Color when the switch is checked (default black)
 */
@Composable
fun TrueStaySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = LocalAppColors.current.black
) {
    val colors = LocalAppColors.current

    // Track width and height
    val trackWidth = 42.dp
    val trackHeight = 24.dp
    val thumbSize = 20.dp
    val thumbPadding = 2.dp

    // Calculate thumb offset
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding else thumbPadding,
        animationSpec = tween(durationMillis = 200),
        label = "thumbOffset"
    )

    // Background color based on state
    val backgroundColor = when {
        !enabled -> colors.grayLight
        checked -> activeColor
        else -> colors.grayLight
    }

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(RoundedCornerShape(trackHeight / 2))
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(colors.white)
        )
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
fun TrueStaySwitchPreview() {
    TrueStayTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // Unchecked state
            TrueStaySwitch(
                checked = false,
                onCheckedChange = {}
            )

            // Checked state
            TrueStaySwitch(
                checked = true,
                onCheckedChange = {}
            )

            // Disabled unchecked
            TrueStaySwitch(
                checked = false,
                onCheckedChange = {},
                enabled = false
            )

            // Disabled checked
            TrueStaySwitch(
                checked = true,
                onCheckedChange = {},
                enabled = false
            )

            // Custom active color
            TrueStaySwitch(
                checked = true,
                onCheckedChange = {},
                activeColor = LocalAppColors.current.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStaySwitchInteractivePreview() {
    TrueStayTheme {
        var checked by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrueStaySwitch(
                checked = checked,
                onCheckedChange = { checked = it }
            )
        }
    }
}

