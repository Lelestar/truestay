package ca.uqac.inf865.truestay.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.UserRole
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueStayNavigation(
    userRole: UserRole?,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if we should show the bottom bar
    val showBottomBar = currentDestination?.route in getMainScreenRoutes(userRole)

    // Determine if we should show the top bar
    val showTopBar = currentDestination?.route !in getMainScreenRoutes(userRole)
            && currentDestination?.route !in getAuthScreenRoutes()
            && currentDestination?.route !in getCustomTopBarScreenRoutes()

    Scaffold(
        topBar = {
            if (showTopBar) {
                getScreenTitleRes(currentDestination?.route ?: "")?.let { titleRes ->
                    TrueStayTopAppBar(
                        titleRes = titleRes,
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                TrueStayBottomBar(
                    items = getBottomNavItems(userRole),
                    currentDestination = currentDestination,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            // Pop up to the start destination
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies
                            launchSingleTop = true
                            // Restore state
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (userRole == null) Screen.Login.route else getStartDestination(userRole),
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth screens
            authNavGraph(navController)

            // Tenant screens
            if (userRole == UserRole.TENANT || userRole == null) {
                tenantNavGraph(navController)
            }

            // Landlord screens
            if (userRole == UserRole.LANDLORD || userRole == null) {
                landlordNavGraph(navController)
            }

            // Shared screens
            sharedNavGraph(navController)
        }
    }
}

// Helper functions
private fun getMainScreenRoutes(userRole: UserRole?): List<String> {
    return when (userRole) {
        UserRole.TENANT -> listOf(
            Screen.Search.route,
            Screen.Favorites.route,
            Screen.TenantRentals.route,
            Screen.Profile.route
        )
        UserRole.LANDLORD -> listOf(
            Screen.Properties.route,
            Screen.LandlordRentals.route,
            Screen.Profile.route
        )
        null -> emptyList()
    }
}

private fun getAuthScreenRoutes(): List<String> {
    return listOf(
        Screen.Login.route,
        Screen.RoleSelection.route,
        Screen.Register.route,
        Screen.ForgotPassword.route,
        Screen.ForgotPasswordEmailSent.route
    )
}

private fun getCustomTopBarScreenRoutes(): List<String> {
    return listOf(
        Screen.PropertyDetails.route
    )
}

private fun getBottomNavItems(userRole: UserRole?): List<BottomNavItem> {
    return when (userRole) {
        UserRole.TENANT -> getTenantBottomNavItems()
        UserRole.LANDLORD -> getLandlordBottomNavItems()
        null -> emptyList()
    }
}

private fun getStartDestination(userRole: UserRole): String {
    return when (userRole) {
        UserRole.TENANT -> Screen.Search.route
        UserRole.LANDLORD -> Screen.Properties.route
    }
}

@StringRes
private fun getScreenTitleRes(route: String): Int? {
    return when {
        route.startsWith("property/") -> R.string.screen_title_property_details
        route.startsWith("rental/") -> R.string.screen_title_rental_details
        route.startsWith("add_review/") -> R.string.screen_title_add_review
        route.startsWith("inventory/") -> R.string.screen_title_inventory
        route.startsWith("room/") -> R.string.screen_title_room_details
        route.startsWith("signature/") -> R.string.screen_title_signature
        // Property form (add/edit)
        route.startsWith("property_form") -> {
            if (route.contains("propertyId=null") || !route.contains("propertyId=")) {
                R.string.screen_title_add_property
            } else {
                R.string.screen_title_edit_property
            }
        }
        route.startsWith("create_rental/") -> R.string.screen_title_create_rental
        route == "edit_profile" -> R.string.screen_title_edit_profile
        route == "change_email" -> R.string.screen_title_change_email
        route == "change_password" -> R.string.screen_title_change_password
        else -> null
    }
}
