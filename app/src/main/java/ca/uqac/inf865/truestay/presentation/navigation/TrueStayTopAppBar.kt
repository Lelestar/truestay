package ca.uqac.inf865.truestay.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueStayTopAppBar(
    @StringRes titleRes: Int,
    onNavigateBack: () -> Unit,
    hasActions: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = titleRes),
                style = MaterialTheme.typography.headlineSmall,
                color = LocalAppColors.current.black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                TrueStayIcon(
                    iconRes = TrueStayIcons.ArrowLeft,
                    contentDescriptionRes = R.string.cd_back
                )
            }
        },
        actions = {
            if (hasActions) {
                actions()
            } else {
                // Invisible icon to center the title
                IconButton(onClick = {}, enabled = false) { }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = LocalAppColors.current.white
        ),
        windowInsets = windowInsets
    )
}

@Preview(showBackground = true)
@Composable
private fun TrueStayTopAppBarPreview() {
    TrueStayTheme {
        TrueStayTopAppBar(
            titleRes = R.string.screen_title_property_details,
            onNavigateBack = {},
            windowInsets = WindowInsets(0.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrueStayTopAppBarActionPreview() {
    TrueStayTheme {
        TrueStayTopAppBar(
            titleRes = R.string.screen_title_property_details,
            onNavigateBack = {},
            windowInsets = WindowInsets(0.dp),
            hasActions = true,
            actions = {
                TrueStayIcon(
                    iconRes = TrueStayIcons.Heart,
                    contentDescriptionRes = null,
                    size = 24.dp,
                    modifier = Modifier.padding(end = AppSpacing.large)
                )
            }
        )
    }
}
