package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

/**
 * TrueStay Custom Range Slider
 *
 * @param label Label displayed above the slider
 * @param value Current slider value range
 * @param onValueChange Callback when the value changes
 * @param valueRange The range of values the slider can take
 * @param steps Number of discrete steps between min and max (0 for continuous)
 * @param modifier Compose modifier
 * @param enabled If false, the slider is disabled
 * @param formatValue Function to format the value text displayed below the slider
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueStaySlider(
    label: String,
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true,
    formatValue: (Float) -> String = { it.toInt().toString() }
) {
    val colors = LocalAppColors.current

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) colors.black else colors.grayDark,
            modifier = Modifier.padding(bottom = AppSpacing.small)
        )

        RangeSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            // Hide default thumbs
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                disabledThumbColor = Color.Transparent
            ),

            // Custom ring thumbs
            startThumb = {
                Canvas(Modifier.size(20.dp)) {
                    drawCircle(color = if (enabled) colors.white else colors.grayLight)
                    drawCircle(
                        color = if (enabled) colors.black else colors.grayDark,
                        radius = size.minDimension / 2 - 1.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            },
            endThumb = {
                Canvas(Modifier.size(20.dp)) {
                    drawCircle(color = if (enabled) colors.white else colors.grayLight)
                    drawCircle(
                        color = if (enabled) colors.black else colors.grayDark,
                        radius = size.minDimension / 2 - 1.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            },

            // Track computed from 'value' and 'valueRange'
            track = {
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp) // room for overlap
                ) {
                    val barH = 16.dp.toPx()
                    val centerY = size.height / 2f
                    val thumbRadius = 10.dp.toPx() // 20.dp thumbs -> 10.dp radius
                    val width = size.width

                    // map current values to [0, 1] then to pixels
                    val total = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
                    val fracStart = ((value.start - valueRange.start) / total).coerceIn(0f, 1f)
                    val fracEnd   = ((value.endInclusive - valueRange.start) / total).coerceIn(0f, 1f)

                    val rawStart = fracStart * width
                    val rawEnd   = fracEnd * width

                    // extend so the bar reaches under the thumbs
                    val startX = (rawStart - thumbRadius).coerceAtLeast(0f)
                    val endX   = (rawEnd + thumbRadius).coerceAtMost(width)

                    // inactive bar
                    drawRoundRect(
                        color = colors.grayLight,
                        topLeft = androidx.compose.ui.geometry.Offset(0f, centerY - barH / 2),
                        size = androidx.compose.ui.geometry.Size(width, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barH / 2)
                    )

                    // active bar
                    if (endX > startX) {
                        drawRoundRect(
                            color = if (enabled) colors.black else colors.grayMedium,
                            topLeft = androidx.compose.ui.geometry.Offset(startX, centerY - barH / 2),
                            size = androidx.compose.ui.geometry.Size(endX - startX, barH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barH / 2)
                        )
                    }
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.xsmall),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatValue(value.start),
                color = colors.grayDark,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatValue(value.endInclusive),
                color = colors.grayDark,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
fun TrueStaySliderPreview() {
    TrueStayTheme {
        var value by remember { mutableStateOf(800f..3000f) }
        TrueStaySlider(
            label = "Budget ($/mois)",
            value = value,
            onValueChange = { value = it },
            valueRange = 800f..3000f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            formatValue = { "${it.toInt()}$" }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStaySliderWithStepsPreview() {
    TrueStayTheme {
        var value by remember { mutableStateOf(1f..5f) }
        TrueStaySlider(
            label = "Nombre de chambres",
            value = value,
            onValueChange = { value = it },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStaySliderDisabledPreview() {
    TrueStayTheme {
        TrueStaySlider(
            label = "Budget ($/mois)",
            value = 1000f..2500f,
            onValueChange = {},
            valueRange = 800f..3000f,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            formatValue = { "${it.toInt()}$" }
        )
    }
}

