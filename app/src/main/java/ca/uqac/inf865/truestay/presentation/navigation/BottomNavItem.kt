package ca.uqac.inf865.truestay.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons

sealed class BottomNavItem(
    val route: String,
    @param:DrawableRes val icon: Int,
    @param:StringRes val labelRes: Int
) {
    // Tenant Bottom Nav
    object TenantSearch : BottomNavItem(
        route = Screen.Search.route,
        icon = TrueStayIcons.Search,
        labelRes = R.string.bottom_nav_search
    )
    object TenantFavorites : BottomNavItem(
        route = Screen.Favorites.route,
        icon = TrueStayIcons.Heart,
        labelRes = R.string.bottom_nav_favorites
    )
    object TenantRentals : BottomNavItem(
        route = Screen.TenantRentals.route,
        icon = TrueStayIcons.House,
        labelRes = R.string.bottom_nav_tenant_rentals
    )
    object TenantProfile : BottomNavItem(
        route = Screen.Profile.route,
        icon = TrueStayIcons.User,
        labelRes = R.string.bottom_nav_profile
    )

    // Landlord Bottom Nav
    object LandlordProperties : BottomNavItem(
        route = Screen.Properties.route,
        icon = TrueStayIcons.House,
        labelRes = R.string.bottom_nav_properties
    )
    object LandlordRentals : BottomNavItem(
        route = Screen.LandlordRentals.route,
        icon = TrueStayIcons.FolderOpen,
        labelRes = R.string.bottom_nav_landlord_rentals
    )
    object LandlordProfile : BottomNavItem(
        route = Screen.Profile.route,
        icon = TrueStayIcons.User,
        labelRes = R.string.bottom_nav_profile
    )
}

// List of items for each role
fun getTenantBottomNavItems() = listOf(
    BottomNavItem.TenantSearch,
    BottomNavItem.TenantFavorites,
    BottomNavItem.TenantRentals,
    BottomNavItem.TenantProfile
)

fun getLandlordBottomNavItems() = listOf(
    BottomNavItem.LandlordProperties,
    BottomNavItem.LandlordRentals,
    BottomNavItem.LandlordProfile
)