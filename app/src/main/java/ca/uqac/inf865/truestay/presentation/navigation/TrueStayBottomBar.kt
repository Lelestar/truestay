package ca.uqac.inf865.truestay.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

@Composable
fun TrueStayBottomBar(
    items: List<BottomNavItem>,
    currentDestination: NavDestination?,
    onItemClick: (BottomNavItem) -> Unit
) {
    val borderColor = LocalAppColors.current.grayBorder

    NavigationBar(
        modifier = Modifier.drawBehind {
            drawLine(
                color = borderColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        },
        containerColor = LocalAppColors.current.white
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.large),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                val label = stringResource(id = item.labelRes)

                if (index > 0) {
                    Spacer(modifier = Modifier.padding(horizontal = AppSpacing.small / 2))
                }

                NavigationBarItem(
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (selected) {
                                        LocalAppColors.current.primarySurface
                                    } else {
                                        LocalAppColors.current.transparent
                                    },
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(AppSpacing.small, AppSpacing.medium)
                        ) {
                            TrueStayIcon(
                                iconRes = item.icon,
                                contentDescriptionRes = item.labelRes,
                                size = 24.dp,
                                tint = if (selected) {
                                    LocalAppColors.current.primary
                                } else {
                                    LocalAppColors.current.grayDark
                                }
                            )
                            Spacer(modifier = Modifier.padding(top = AppSpacing.xsmall))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selected) {
                                    LocalAppColors.current.primary
                                } else {
                                    LocalAppColors.current.grayDark
                                }
                            )
                        }
                    },
                    label = null,
                    selected = selected,
                    onClick = { onItemClick(item) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = LocalAppColors.current.transparent
                    )
                )
            }
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true, name = "Bottom Bar - Search Selected")
@Composable
private fun TrueStayBottomBarSearchSelectedPreview() {
    TrueStayTheme {
        val navController = rememberNavController()

        // Simulate selection of the Search tab
        NavHost(navController = navController, startDestination = Screen.Search.route) {
            composable(Screen.Search.route) { }
        }

        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStackEntry?.destination

        TrueStayBottomBar(
            items = getTenantBottomNavItems(),
            currentDestination = currentDestination,
            onItemClick = { }
        )
    }
}
