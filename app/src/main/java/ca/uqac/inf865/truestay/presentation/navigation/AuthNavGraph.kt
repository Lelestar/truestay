package ca.uqac.inf865.truestay.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ca.uqac.inf865.truestay.domain.model.UserRole
import ca.uqac.inf865.truestay.presentation.shared.auth.ForgotPasswordEmailSentScreen
import ca.uqac.inf865.truestay.presentation.shared.auth.ForgotPasswordScreen
import ca.uqac.inf865.truestay.presentation.shared.auth.LoginScreen
import ca.uqac.inf865.truestay.presentation.shared.auth.RoleSelectionScreen
import ca.uqac.inf865.truestay.presentation.shared.auth.RegisterScreen

fun NavGraphBuilder.authNavGraph(
    navController: NavHostController
) {
    composable(Screen.Login.route) {
        LoginScreen(
            onLoginSuccess = { userRole ->
                val destination = if (userRole == UserRole.TENANT) {
                    Screen.Search.route
                } else {
                    Screen.Properties.route
                }
                navController.navigate(destination) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            onNavigateToRegister = {
                navController.navigate(Screen.RoleSelection.route)
            },
            onNavigateToForgotPassword = {
                navController.navigate(Screen.ForgotPassword.route)
            }
        )
    }

    composable(Screen.RoleSelection.route) {
        RoleSelectionScreen(
            onRoleSelected = { role ->
                navController.navigate(Screen.Register.createRoute(role))
            },
            onNavigateToLogin = {
                navController.navigateUp()
            }
        )
    }

    composable(
        route = Screen.Register.route,
        arguments = listOf(navArgument("role") { type = NavType.StringType })
    ) { backStackEntry ->
        val roleString = backStackEntry.arguments?.getString("role") ?: "tenant"

        // Convert String to enum
        val role = when (roleString.lowercase()) {
            "landlord" -> UserRole.LANDLORD
            else -> UserRole.TENANT
        }

        RegisterScreen(
            userRole = role,
            onRegisterSuccess = {
                val destination = if (role == UserRole.TENANT) {
                    Screen.Search.route
                } else {
                    Screen.Properties.route
                }
                navController.navigate(destination) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            onNavigateToLogin = {
                navController.popBackStack(Screen.Login.route, inclusive = false)
            },
            onNavigateToTerms = {
                // TODO: redirect to terms of service
            },
            onNavigateToPrivacy = {
                // TODO: redirect to privacy policy
            }
        )
    }

    composable(Screen.ForgotPassword.route) {
        ForgotPasswordScreen(
            onEmailSent = { email ->
                navController.navigate(Screen.ForgotPasswordEmailSent.createRoute(email))
            },
            onNavigateToLogin = {
                navController.navigateUp()
            }
        )
    }

    composable(
        route = Screen.ForgotPasswordEmailSent.route,
        arguments = listOf(navArgument("email") { type = NavType.StringType })
    ) { backStackEntry ->
        val email = backStackEntry.arguments?.getString("email") ?: ""

        ForgotPasswordEmailSentScreen(
            email = email,
            onNavigateToLogin = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                }
            }
        )
    }
}