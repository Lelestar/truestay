package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

/**
 * Base component to display a row of stars
 * Internal component used by TrueStayRatingInput and TrueStayRatingDisplay
 */
@Composable
private fun StarRatingBar(
    rating: Int,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Dp,
    starColor: Color,
    starSpacing: Dp = AppSpacing.small,
    onStarClick: ((Int) -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(starSpacing)
    ) {
        repeat(maxStars) { index ->
            val starIndex = index + 1
            val isFilled = starIndex <= rating

            TrueStayIcon(
                iconRes = if (isFilled) TrueStayIcons.StarFilled else TrueStayIcons.Star,
                contentDescriptionRes = null,
                tint = if (isFilled) starColor else LocalAppColors.current.grayBorder,
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (onStarClick != null) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // Reset rating if the same star is clicked
                                if (starIndex == rating) {
                                    onStarClick(0)
                                } else {
                                    onStarClick(starIndex)
                                }
                            }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

/**
 * Interactive rating component with label for user input
 * Used in filters and rental rating forms
 *
 * @param label Label displayed above the stars
 * @param rating Current rating value (0 to maxStars)
 * @param onRatingChange Callback when the rating changes
 * @param modifier Compose modifier
 * @param maxStars Maximum number of stars (default 5)
 * @param enabled If false, the rating is disabled
 */
@Composable
fun TrueStayRatingInput(
    label: String,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    enabled: Boolean = true
) {
    val colors = LocalAppColors.current

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) colors.black else colors.grayDark,
            modifier = Modifier.padding(bottom = AppSpacing.small)
        )

        StarRatingBar(
            rating = rating.coerceIn(0, maxStars),
            maxStars = maxStars,
            starSize = 32.dp,
            starColor = if (enabled) colors.warning else colors.grayBorder,
            onStarClick = if (enabled) onRatingChange else null
        )
    }
}

/**
 * Compact rating display component without interaction
 * Used to show existing ratings in rental cards, comments, etc.
 *
 * @param rating Rating value to display (0 to maxStars)
 * @param modifier Compose modifier
 * @param maxStars Maximum number of stars (default 5)
 * @param starSize Size of each star icon (default 16.dp)
 * @param starColor Color of the stars
 */
@Composable
fun TrueStayRatingDisplay(
    rating: Float,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Dp = 16.dp,
    starColor: Color = LocalAppColors.current.warning
) {
    StarRatingBar(
        rating = rating.toInt().coerceIn(0, maxStars),
        maxStars = maxStars,
        starSize = starSize,
        starColor = starColor,
        starSpacing = AppSpacing.xsmall,
        onStarClick = null,
        modifier = modifier
    )
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
fun TrueStayRatingInputPreview() {
    TrueStayTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            TrueStayRatingInput(
                label = "Note minimale",
                rating = 0,
                onRatingChange = {}
            )

            TrueStayRatingInput(
                label = "Note minimale",
                rating = 3,
                onRatingChange = {}
            )

            TrueStayRatingInput(
                label = "Note minimale",
                rating = 5,
                onRatingChange = {}
            )

            TrueStayRatingInput(
                label = "Note minimale",
                rating = 3,
                onRatingChange = {},
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrueStayRatingDisplayPreview() {
    TrueStayTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Text("Default size:")
            TrueStayRatingDisplay(rating = 4.5f)

            Text("Larger size:")
            TrueStayRatingDisplay(rating = 3f, starSize = 24.dp)

            Text("Custom color:")
            TrueStayRatingDisplay(
                rating = 5f,
                starSize = 20.dp,
                starColor = LocalAppColors.current.black
            )
        }
    }
}

