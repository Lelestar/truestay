package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors

/**
 * Simple and reusable card
 * Provides only the container with shape, padding, and white background
 * The content inside is fully flexible
 */
@Composable
fun TrueStayCard(
    modifier: Modifier = Modifier,
    padding: Dp = AppSpacing.large,
    elevation: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = MaterialTheme.shapes.large,
                clip = false
            )
            .clip(MaterialTheme.shapes.medium)
            .background(LocalAppColors.current.white)
            .border(
                width = 1.dp,
                color = LocalAppColors.current.grayBorder,
                shape = MaterialTheme.shapes.medium
            )
            .padding(padding)
    ) {
        content()
    }
}