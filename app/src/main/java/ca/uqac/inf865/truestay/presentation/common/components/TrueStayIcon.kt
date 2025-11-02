package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.ui.graphics.Color
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Personalized Icon Composable for TrueStay
 * Allows to easily use icons with size and tint customization
 */
@Composable
fun TrueStayIcon(
    @DrawableRes iconRes: Int,
    @StringRes contentDescriptionRes: Int?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp
) {
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = contentDescriptionRes?.let { stringResource(id = it) },
        modifier = modifier.size(size),
        tint = tint
    )
}