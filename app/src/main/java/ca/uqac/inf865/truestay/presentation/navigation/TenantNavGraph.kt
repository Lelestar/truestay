package ca.uqac.inf865.truestay.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ca.uqac.inf865.truestay.presentation.tenant.favorites.FavoritesScreen
import ca.uqac.inf865.truestay.presentation.tenant.rentals.RentalsScreen
import ca.uqac.inf865.truestay.presentation.tenant.review.ReviewFormScreen
import ca.uqac.inf865.truestay.presentation.tenant.search.SearchScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.tenantNavGraph(navController: NavHostController) {
    // Main screens
    composable(Screen.Search.route) {
        SearchScreen(
            onPropertyClick = { propertyId ->
                navController.navigate(Screen.PropertyDetails.createRoute(propertyId))
            }
        )
    }

    composable(Screen.Favorites.route) {
        FavoritesScreen(
            onPropertyClick = { propertyId ->
                navController.navigate(Screen.PropertyDetails.createRoute(propertyId))
            }
        )
    }

    composable(Screen.TenantRentals.route) {
        RentalsScreen(
            onRentalClick = { rentalId ->
                navController.navigate(Screen.RentalDetails.createRoute(rentalId))
            },
            onInventoryClick = { rentalId ->
                navController.navigate(Screen.RentalDetails.createRoute(rentalId, "inventories"))
            },
            onReviewClick = { rentalId ->
                navController.navigate(Screen.RentalDetails.createRoute(rentalId, "reviews"))
            }
        )
    }

    // Secondary screens
    composable(
        route = Screen.ReviewForm.route,
        arguments = listOf(
            navArgument("rentalId") { type = NavType.StringType },
            navArgument("reviewType") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        ReviewFormScreen(
            onBackClick = { navController.navigateUp() },
            onReviewSubmitted = { navController.navigateUp() }
        )
    }
}