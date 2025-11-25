package ca.uqac.inf865.truestay.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ca.uqac.inf865.truestay.presentation.landlord.property.PropertiesScreen
import ca.uqac.inf865.truestay.presentation.landlord.property.PropertyFormScreen
import ca.uqac.inf865.truestay.presentation.landlord.rental.CreateRentalScreen
import ca.uqac.inf865.truestay.presentation.landlord.rental.RentalsScreen

fun NavGraphBuilder.landlordNavGraph(navController: NavHostController) {
    // Main screens
    composable(Screen.Properties.route) {
        PropertiesScreen(
            onPropertyClick = { propertyId ->
                navController.navigate(Screen.PropertyDetails.createRoute(propertyId))
            },
            onAddProperty = {
                navController.navigate(Screen.PropertyForm.createRouteForAdd())
            },
            onEditProperty = { propertyId ->
                navController.navigate(Screen.PropertyForm.createRouteForEdit(propertyId))
            }
        )
    }

    composable(Screen.LandlordRentals.route) {
        RentalsScreen(
            onRentalClick = { rentalId ->
                navController.navigate(Screen.RentalDetails.createRoute(rentalId))
            },
            onInventoryClick = { inventoryId ->
                navController.navigate(Screen.Inventory.createRoute(inventoryId))
            },
            onInventoriesClick = { rentalId ->
                navController.navigate(Screen.RentalDetails.createRoute(rentalId, "inventories"))
            },
            onReviewClick = { rentalId ->
                navController.navigate(Screen.RentalDetails.createRoute(rentalId, "reviews"))
            }
        )
    }

    // Secondary screens
    composable(
        route = Screen.PropertyForm.route,
        arguments = listOf(
            navArgument("propertyId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val propertyId = backStackEntry.arguments?.getString("propertyId")
            ?.takeIf { it != "null" } // to handle nullable argument

        PropertyFormScreen(
            propertyId = propertyId,
            onBackClick = { navController.navigateUp() },
            onPropertySaved = { navController.navigateUp() }
        )
    }

    composable(
        route = Screen.CreateRental.route,
        arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
    ) { backStackEntry ->
        val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
        CreateRentalScreen(
            propertyId = propertyId,
            onBackClick = { navController.navigateUp() },
            onRentalCreated = { navController.navigateUp() }
        )
    }
}