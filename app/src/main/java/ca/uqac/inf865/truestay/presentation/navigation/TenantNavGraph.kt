package ca.uqac.inf865.truestay.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ca.uqac.inf865.truestay.domain.model.ReviewType
import ca.uqac.inf865.truestay.presentation.tenant.favorites.FavoritesScreen
import ca.uqac.inf865.truestay.presentation.tenant.rentals.RentalsScreen
import ca.uqac.inf865.truestay.presentation.tenant.review.ReviewFormScreen
import ca.uqac.inf865.truestay.presentation.tenant.search.SearchScreen

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
            },
            onPropertyClick = { propertyId ->
                navController.navigate(Screen.PropertyDetails.createRoute(propertyId))
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
        val rentalId = backStackEntry.arguments?.getString("rentalId") ?: ""
        val reviewTypeString = backStackEntry.arguments?.getString("reviewType") ?: ""

        // Convert String to enum
        val reviewType = reviewTypeString.let {
            try {
                ReviewType.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                ReviewType.PROPERTY
            }
        }

        ReviewFormScreen(
            rentalId = rentalId,
            reviewType = reviewType,
            onBackClick = { navController.navigateUp() },
            onReviewSubmitted = { navController.navigateUp() }
        )
    }
}