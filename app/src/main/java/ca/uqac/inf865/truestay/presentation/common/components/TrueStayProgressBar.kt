package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

/**
 * Simple progress bar with label and step counter.
 *
 * @param label Text displayed above the bar on the left
 * @param currentStep Current step number
 * @param totalSteps Total number of steps
 * @param modifier Compose modifier
 */
@Composable
fun TrueStayProgressBar(
    label: String,
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val clampedTotal = totalSteps.coerceAtLeast(1)
    val progress = (currentStep.toFloat() / clampedTotal)
        .coerceAtLeast(0f)
        .coerceAtMost(1f)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark
            )
            Text(
                text = "$currentStep/$totalSteps",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark
            )
        }

        Box(
            modifier = Modifier
                .padding(top = AppSpacing.small)
                .clip(AppShapes.medium)
                .background(colors.grayDefault)
                .height(8.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(AppShapes.medium)
                    .background(colors.primary)
            )
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
fun TrueStayProgressBarPreview() {
    TrueStayTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        ) {
            TrueStayProgressBar(
                label = "Progression",
                currentStep = 2,
                totalSteps = 5,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
