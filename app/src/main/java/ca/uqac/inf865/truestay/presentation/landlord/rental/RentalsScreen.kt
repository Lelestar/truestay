package ca.uqac.inf865.truestay.presentation.landlord.rental

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyRatings
import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.RentalStatus
import ca.uqac.inf865.truestay.domain.model.Room
import ca.uqac.inf865.truestay.domain.model.RoomType
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.model.UserRole
import ca.uqac.inf865.truestay.presentation.common.components.BadgeVariant
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.PropertyAction
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCard
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCardVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

/**
 * Screen displaying landlord's rentals with tabs for active and past rentals
 */
@Composable
fun RentalsScreen(
    onRentalClick: (String) -> Unit,
    onInventoryClick: (String) -> Unit,
    onInventoriesClick: (String) -> Unit,
    onReviewClick: (String) -> Unit,
    viewModel: RentalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RentalsScreenContent(
        uiState = uiState,
        onRentalClick = onRentalClick,
        onInventoryClick = onInventoryClick,
        onInventoriesClick = onInventoriesClick,
        onReviewClick = onReviewClick,
        onRetry = { viewModel.refreshRentals() }
    )
}

@Composable
private fun RentalsScreenContent(
    uiState: LandlordRentalsUiState,
    onRentalClick: (String) -> Unit,
    onInventoryClick: (String) -> Unit,
    onInventoriesClick: (String) -> Unit,
    onReviewClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with title and subtitle
        RentalsHeader()

        // Content based on state
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppSpacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when (uiState.error) {
                                LandlordRentalsError.NOT_AUTHENTICATED -> stringResource(R.string.rentals_error_not_authenticated)
                                LandlordRentalsError.LOAD_FAILED -> stringResource(R.string.rentals_error_loading)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = LocalAppColors.current.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.large))
                        TrueStayButton(
                            text = stringResource(R.string.common_retry),
                            onClick = onRetry,
                            variant = ButtonVariant.SECONDARY
                        )
                    }
                }
            }

            else -> {
                // Tab row for "En cours" and "Historique"
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = LocalAppColors.current.white,
                    contentColor = LocalAppColors.current.primary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                text = stringResource(R.string.landlord_rentals_tab_in_progress) +
                                      " (${uiState.activeRentals.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (selectedTabIndex == 0)
                                    LocalAppColors.current.primary
                                else
                                    LocalAppColors.current.grayDark
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                text = stringResource(R.string.landlord_rentals_tab_history) +
                                      " (${uiState.pastRentals.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (selectedTabIndex == 1)
                                    LocalAppColors.current.primary
                                else
                                    LocalAppColors.current.grayDark
                            )
                        }
                    )
                }

                // Tab content
                when (selectedTabIndex) {
                    0 -> ActiveRentalsTab(
                        activeRentals = uiState.activeRentals,
                        onRentalClick = onRentalClick,
                        onInventoryClick = onInventoryClick
                    )
                    1 -> HistoryRentalsTab(
                        pastRentals = uiState.pastRentals,
                        onRentalClick = onRentalClick,
                        onInventoriesClick = onInventoriesClick,
                        onReviewClick = onReviewClick
                    )
                }
            }
        }
    }
}

/**
 * Header section with title and subtitle
 */
@Composable
private fun RentalsHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LocalAppColors.current.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.large)
        ) {
            Text(
                text = stringResource(R.string.rentals_title),
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.white
            )
            Spacer(modifier = Modifier.height(AppSpacing.medium))
            Text(
                text = stringResource(R.string.landlord_rentals_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.white
            )
        }
    }
}

/**
 * Tab content for active rentals
 */
@Composable
private fun ActiveRentalsTab(
    activeRentals: List<LandlordRentalItem>,
    onRentalClick: (String) -> Unit,
    onInventoryClick: (String) -> Unit
) {
    if (activeRentals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.large),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.landlord_rentals_no_in_progress),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            items(activeRentals) { rentalItem ->
                ActiveRentalCard(
                    item = rentalItem,
                    onClick = { onRentalClick(rentalItem.rental.id) },
                    onEntryInventoryClick = {
                        rentalItem.rental.entryInventoryId?.let { onInventoryClick(it) }
                    },
                    onExitInventoryClick = {
                        rentalItem.rental.exitInventoryId?.let { onInventoryClick(it) }
                    }
                )
            }
        }
    }
}

/**
 * Tab content for past rentals
 */
@Composable
private fun HistoryRentalsTab(
    pastRentals: List<LandlordRentalItem>,
    onRentalClick: (String) -> Unit,
    onInventoriesClick: (String) -> Unit,
    onReviewClick: (String) -> Unit
) {
    if (pastRentals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.large),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.landlord_rentals_no_history),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            items(pastRentals) { rentalItem ->
                PastRentalCard(
                    item = rentalItem,
                    onClick = { onRentalClick(rentalItem.rental.id) },
                    onInventoriesClick = { onInventoriesClick(rentalItem.rental.id) },
                    onReviewClick = { onReviewClick(rentalItem.rental.id) }
                )
            }
        }
    }
}

/**
 * Card displaying an active rental with property, tenant info, and action buttons
 */
@Composable
private fun ActiveRentalCard(
    item: LandlordRentalItem,
    onClick: () -> Unit,
    onEntryInventoryClick: () -> Unit,
    onExitInventoryClick: () -> Unit
) {
    val rental = item.rental
    val tenant = item.tenant
    val property = item.property
    val actions = buildList {
        if (rental.entryInventoryId != null) {
            add(
                PropertyAction(
                    iconRes = TrueStayIcons.FileText,
                    label = stringResource(R.string.landlord_rentals_entry_inventory_button),
                    onClick = onEntryInventoryClick,
                    variant = ButtonVariant.SECONDARY
                )
            )
        }
        if (rental.exitInventoryId != null) {
            add(
                PropertyAction(
                    iconRes = TrueStayIcons.FileText,
                    label = stringResource(R.string.landlord_rentals_exit_inventory_button),
                    onClick = onExitInventoryClick,
                    variant = ButtonVariant.SECONDARY
                )
            )
        }
    }

    PropertyCard(
        property = property,
        onClick = onClick,
        variant = PropertyCardVariant.INTERACTIVE,
        actions = actions,
        startDate = rental.startDate,
        endDate = rental.endDate,
        tenantName = "${tenant.firstName} ${tenant.lastName}",
        customBadgeText = stringResource(R.string.property_rental_in_progress),
        customBadgeVariant = BadgeVariant.SUCCESS
    )
}

/**
 * Card displaying a past rental with property info and action buttons
 */
@Composable
private fun PastRentalCard(
    item: LandlordRentalItem,
    onClick: () -> Unit,
    onInventoriesClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    val rental = item.rental
    val property = item.property
    val tenant = item.tenant
    val actions = buildList {
        if (rental.entryInventoryId != null || rental.exitInventoryId != null) {
            add(
                PropertyAction(
                    iconRes = TrueStayIcons.FileText,
                    label = stringResource(R.string.landlord_rentals_inventory_button),
                    onClick = onInventoriesClick,
                    variant = ButtonVariant.SECONDARY
                )
            )
        }
        add(
            PropertyAction(
                iconRes = TrueStayIcons.Star,
                label = stringResource(R.string.landlord_rentals_reviews_button),
                onClick = onReviewClick,
                variant = ButtonVariant.SECONDARY
            )
        )
    }

    PropertyCard(
        property = property,
        onClick = onClick,
        variant = PropertyCardVariant.INTERACTIVE,
        actions = actions,
        startDate = rental.startDate,
        endDate = rental.endDate,
        tenantName = "${tenant.firstName} ${tenant.lastName}",
        customBadgeText = if (rental.status == RentalStatus.ENDED)
            stringResource(R.string.property_rental_completed) else stringResource(R.string.property_rental_cancelled),
        customBadgeVariant = if (rental.status == RentalStatus.ENDED) BadgeVariant.INFO else BadgeVariant.ERROR
    )
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
private fun LandlordRentalsLoadingPreview() {
    TrueStayTheme {
        RentalsScreenContent(
            uiState = LandlordRentalsUiState(isLoading = true),
            onRentalClick = {},
            onInventoryClick = {},
            onInventoriesClick = {},
            onReviewClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LandlordRentalsErrorPreview() {
    TrueStayTheme {
        RentalsScreenContent(
            uiState = LandlordRentalsUiState(
                isLoading = false,
                error = LandlordRentalsError.LOAD_FAILED
            ),
            onRentalClick = {},
            onInventoryClick = {},
            onInventoriesClick = {},
            onReviewClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LandlordRentalsActivePreview() {
    val (active, _) = sampleLandlordRentalItems()
    TrueStayTheme {
        RentalsScreenContent(
            uiState = LandlordRentalsUiState(
                activeRentals = active,
                pastRentals = emptyList()
            ),
            onRentalClick = {},
            onInventoryClick = {},
            onInventoriesClick = {},
            onReviewClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LandlordRentalsHistoryPreview() {
    val (_, past) = sampleLandlordRentalItems()
    TrueStayTheme {
        RentalsScreenContent(
            uiState = LandlordRentalsUiState(
                activeRentals = emptyList(),
                pastRentals = past
            ),
            onRentalClick = {},
            onInventoryClick = {},
            onInventoriesClick = {},
            onReviewClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LandlordRentalsMixedPreview() {
    val (active, past) = sampleLandlordRentalItems()
    TrueStayTheme {
        RentalsScreenContent(
            uiState = LandlordRentalsUiState(
                activeRentals = active,
                pastRentals = past
            ),
            onRentalClick = {},
            onInventoryClick = {},
            onInventoriesClick = {},
            onReviewClick = {},
            onRetry = {}
        )
    }
}

private fun sampleLandlordRentalItems(): Pair<List<LandlordRentalItem>, List<LandlordRentalItem>> {
    val now = System.currentTimeMillis()
    val property1 = Property(
        id = "p1",
        name = "Condo lumineux",
        address = Address(
            street = "123 Rue des Pins",
            city = "Saguenay",
            province = "QC",
            postalCode = "G7X 1A1",
            country = "Canada"
        ),
        monthlyRent = 1200,
        rooms = listOf(
            Room(name = "Chambre", type = RoomType.BEDROOM),
            Room(name = "Salon", type = RoomType.LIVING_ROOM)
        ),
        photos = listOf("https://picsum.photos/400/300"),
        landlordId = "landlord1",
        isAvailable = false,
        ratings = PropertyRatings(propertyAverageRating = 4.5f, propertyReviewCount = 8)
    )

    val property2 = property1.copy(
        id = "p2",
        name = "Maison familiale",
        address = property1.address.copy(street = "456 Rue du Parc"),
        monthlyRent = 1800,
        photos = listOf("https://picsum.photos/400/301")
    )

    val tenant1 = User(
        id = "tenant1",
        firstName = "Alice",
        lastName = "Martin",
        role = UserRole.TENANT,
        email = "alice@example.com"
    )

    val tenant2 = tenant1.copy(
        id = "tenant2",
        firstName = "Marc",
        lastName = "Dubois",
        email = "marc@example.com"
    )

    val activeRental = Rental(
        id = "r1",
        propertyId = property1.id,
        tenantId = tenant1.id,
        landlordId = property1.landlordId,
        startDate = now - 5 * 24 * 60 * 60 * 1000L,
        endDate = now + 30 * 24 * 60 * 60 * 1000L,
        status = RentalStatus.ACTIVE,
        entryInventoryId = "inv_entry_r1",
        exitInventoryId = "inv_exit_r1"
    )

    val pastRental = Rental(
        id = "r2",
        propertyId = property2.id,
        tenantId = tenant2.id,
        landlordId = property2.landlordId,
        startDate = now - 400 * 24 * 60 * 60 * 1000L,
        endDate = now - 30 * 24 * 60 * 60 * 1000L,
        status = RentalStatus.ENDED,
        entryInventoryId = "inv_entry_r2",
        exitInventoryId = "inv_exit_r2"
    )

    val active = listOf(LandlordRentalItem(activeRental, property1, tenant1))
    val past = listOf(LandlordRentalItem(pastRental, property2, tenant2))
    return active to past
}
