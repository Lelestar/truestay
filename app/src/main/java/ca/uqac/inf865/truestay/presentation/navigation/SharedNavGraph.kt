package ca.uqac.inf865.truestay.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ca.uqac.inf865.truestay.presentation.shared.inventory.InventoryScreen
import ca.uqac.inf865.truestay.presentation.shared.inventory.RoomDetailsScreen
import ca.uqac.inf865.truestay.presentation.shared.inventory.SignatureScreen
import ca.uqac.inf865.truestay.presentation.shared.profile.ProfileScreen
import ca.uqac.inf865.truestay.presentation.shared.property.PropertyDetailsScreen
import ca.uqac.inf865.truestay.presentation.shared.rental.RentalDetailsScreen
import ca.uqac.inf865.truestay.presentation.shared.rental.RentalDetailsSection

fun NavGraphBuilder.sharedNavGraph(
    navController: NavHostController
) {
    // Main screens
    composable(Screen.Profile.route) {
        ProfileScreen(
            onLogout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    // Secondary screens
    composable(
        route = Screen.PropertyDetails.route,
        arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
    ) { backStackEntry ->
        val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
        PropertyDetailsScreen(
            propertyId = propertyId,
            onBackClick = { navController.navigateUp() }
        )
    }

    composable(
        route = Screen.RentalDetails.route,
        arguments = listOf(
            navArgument("rentalId") { type = NavType.StringType },
            navArgument("section") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val rentalId = backStackEntry.arguments?.getString("rentalId") ?: ""
        val sectionString = backStackEntry.arguments?.getString("section")

        // Convert String to enum
        val section = sectionString?.let {
            try {
                RentalDetailsSection.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        RentalDetailsScreen(
            rentalId = rentalId,
            initialSection = section,
            onAddOrEditReviewClick = { reviewType ->
                navController.navigate(Screen.ReviewForm.createRoute(rentalId, reviewType))
            },
            onInventoryClick = { inventoryId ->
                navController.navigate(Screen.Inventory.createRoute(inventoryId))
            },
            onEndLease = {
                // TODO
            }
        )
    }

    composable(
        route = Screen.Inventory.route,
        arguments = listOf(navArgument("inventoryId") { type = NavType.StringType })
    ) { backStackEntry ->
        val inventoryId = backStackEntry.arguments?.getString("inventoryId") ?: ""
        InventoryScreen(
            inventoryId = inventoryId,
            onRoomClick = { roomId ->
                navController.navigate(Screen.RoomDetails.createRoute(inventoryId, roomId))
            },
            onSignClick = {
                navController.navigate(Screen.Signature.createRoute(inventoryId))
            }
        )
    }

    composable(
        route = Screen.RoomDetails.route,
        arguments = listOf(
            navArgument("inventoryId") { type = NavType.StringType },
            navArgument("roomId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val inventoryId = backStackEntry.arguments?.getString("inventoryId") ?: ""
        val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
        RoomDetailsScreen(
            inventoryId = inventoryId,
            roomId = roomId,
        )
    }

    composable(
        route = Screen.Signature.route,
        arguments = listOf(navArgument("inventoryId") { type = NavType.StringType })
    ) { backStackEntry ->
        val inventoryId = backStackEntry.arguments?.getString("inventoryId") ?: ""
        SignatureScreen(
            inventoryId = inventoryId,
            onBackClick = { navController.navigateUp() },
            onSignatureSubmitted = { navController.navigateUp() }
        )
    }
}