package ca.uqac.inf865.truestay.presentation.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object RoleSelection : Screen("role_selection")
    object Register : Screen("register/{role}") {
        fun createRoute(role: String) = "register/$role"
    }
    object ForgotPassword : Screen("forgot_password")
    object ForgotPasswordEmailSent : Screen("forgot_password_email_sent/{email}") {
        fun createRoute(email: String) = "forgot_password_email_sent/$email"
    }

    // Tenant - Main screens (with bottom bar)
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object TenantRentals : Screen("tenant_rentals")

    // Tenant - Secondary screens (with top bar)
    object ReviewForm : Screen("review_form/{rentalId}/{reviewType}") {
        fun createRoute(rentalId: String, reviewType: String) = "review_form/$rentalId/$reviewType"
    }

    // Landlord - Main screens (with bottom bar)
    object Properties : Screen("properties")
    object LandlordRentals : Screen("landlord_rentals")

    // Landlord - Secondary screens (with top bar)
    object PropertyForm : Screen("property_form?propertyId={propertyId}") {
        fun createRouteForAdd() = "property_form?propertyId=null"
        fun createRouteForEdit(propertyId: String) = "property_form?propertyId=$propertyId"
    }
    object CreateRental : Screen("create_rental/{propertyId}") {
        fun createRoute(propertyId: String) = "create_rental/$propertyId"
    }

    // Shared - Main screens
    object Profile : Screen("profile")

    // Shared - Secondary screens
    object PropertyDetails : Screen("property/{propertyId}") {
        fun createRoute(propertyId: String) = "property/$propertyId"
    }
    object RentalDetails : Screen("rental/{rentalId}?section={section}") {
        fun createRoute(
            rentalId: String,
            section: String? = null
        ): String {
            return if (section != null) {
                "rental/$rentalId?section=$section"
            } else {
                "rental/$rentalId"
            }
        }
    }
    object Inventory : Screen("inventory/{inventoryId}") {
        fun createRoute(inventoryId: String) = "inventory/$inventoryId"
    }
    object RoomDetails : Screen("room/{inventoryId}/{roomId}") {
        fun createRoute(inventoryId: String, roomId: String) = "room/$inventoryId/$roomId"
    }
    object Signature : Screen("signature/{inventoryId}") {
        fun createRoute(inventoryId: String) = "signature/$inventoryId"
    }
    object EditProfile : Screen("edit_profile")
    object ChangeEmail : Screen("change_email")
    object ChangePassword : Screen("change_password")
}