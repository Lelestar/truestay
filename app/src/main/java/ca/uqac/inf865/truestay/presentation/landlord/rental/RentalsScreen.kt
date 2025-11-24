package ca.uqac.inf865.truestay.presentation.landlord.rental

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.BadgeVariant
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCard
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCardVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.common.utils.DateUtils
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors

/**
 * Screen displaying landlord's rentals with tabs for active and past rentals
 */
@Composable
fun RentalsScreen(
    onRentalClick: (String) -> Unit,
    onInventoryClick: (String) -> Unit,
    onReviewClick: (String) -> Unit,
    viewModel: RentalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RentalsScreenContent(
        uiState = uiState,
        onRentalClick = onRentalClick,
        onInventoryClick = onInventoryClick,
        onReviewClick = onReviewClick,
        onRetry = { viewModel.refreshRentals() }
    )
}

@Composable
private fun RentalsScreenContent(
    uiState: LandlordRentalsUiState,
    onRentalClick: (String) -> Unit,
    onInventoryClick: (String) -> Unit,
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
                        onInventoryClick = onInventoryClick,
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
    onInventoryClick: (String) -> Unit,
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
                    onInventoryClick = { onInventoryClick(rentalItem.rental.id) },
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
    val property = item.property
    val tenant = item.tenant

    // Calculate days remaining
    val now = System.currentTimeMillis()
    val daysRemaining = ((rental.endDate - now) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)

    // Property card with "En cours" badge and integrated content
    PropertyCard(
        property = property,
        onClick = onClick,
        variant = PropertyCardVariant.DETAILED,
        customBadgeText = stringResource(R.string.property_rental_in_progress),
        customBadgeVariant = BadgeVariant.SUCCESS,
        hideDetails = true,
        bottomContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.large)
                    .padding(bottom = AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
            ) {
                // Rental details card with tenant info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.large)
                        .background(LocalAppColors.current.graySurface)
                        .padding(AppSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
                ) {
                    // Tenant info with icon and name
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrueStayIcons.User.let { iconRes ->
                            ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon(
                                iconRes = iconRes,
                                contentDescriptionRes = null,
                                tint = LocalAppColors.current.black,
                                size = 16.dp
                            )
                        }
                        Text(
                            text = "${tenant.firstName} ${tenant.lastName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = LocalAppColors.current.black
                        )
                    }

                    // Rental period in YYYY/MM - YYYY/MM format
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrueStayIcons.Calendar.let { iconRes ->
                                ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon(
                                    iconRes = iconRes,
                                    contentDescriptionRes = null,
                                    tint = LocalAppColors.current.grayDark,
                                    size = 16.dp
                                )
                            }
                            Text(
                                text = stringResource(R.string.landlord_rentals_rental_period),
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalAppColors.current.grayDark
                            )
                        }
                        Text(
                            text = formatRentalPeriod(rental.startDate, rental.endDate),
                            style = MaterialTheme.typography.titleMedium,
                            color = LocalAppColors.current.black
                        )
                    }

                    // Time remaining
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrueStayIcons.History.let { iconRes ->
                                ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon(
                                    iconRes = iconRes,
                                    contentDescriptionRes = null,
                                    tint = LocalAppColors.current.grayDark,
                                    size = 16.dp
                                )
                            }
                            Text(
                                text = stringResource(R.string.landlord_rentals_time_remaining),
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalAppColors.current.grayDark
                            )
                        }
                        Text(
                            text = stringResource(R.string.property_rental_days_left, daysRemaining),
                            style = MaterialTheme.typography.titleMedium,
                            color = LocalAppColors.current.primary
                        )
                    }
                }

                // Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    TrueStayButton(
                        text = stringResource(R.string.landlord_rentals_entry_inventory_button),
                        onClick = onEntryInventoryClick,
                        variant = ButtonVariant.SECONDARY,
                        leadingIcon = TrueStayIcons.FileText,
                        enabled = rental.entryInventoryId != null
                    )
                    TrueStayButton(
                        text = stringResource(R.string.landlord_rentals_exit_inventory_button),
                        onClick = onExitInventoryClick,
                        variant = ButtonVariant.SECONDARY,
                        leadingIcon = TrueStayIcons.FileText,
                        enabled = rental.exitInventoryId != null
                    )
                }
            }
        }
    )
}

/**
 * Format rental period as YYYY/MM - YYYY/MM
 */
private fun formatRentalPeriod(startDate: Long, endDate: Long): String {
    val startCalendar = java.util.Calendar.getInstance().apply {
        timeInMillis = startDate
    }
    val endCalendar = java.util.Calendar.getInstance().apply {
        timeInMillis = endDate
    }

    val startYear = startCalendar.get(java.util.Calendar.YEAR)
    val startMonth = String.format(java.util.Locale.getDefault(), "%02d", startCalendar.get(java.util.Calendar.MONTH) + 1)
    val endYear = endCalendar.get(java.util.Calendar.YEAR)
    val endMonth = String.format(java.util.Locale.getDefault(), "%02d", endCalendar.get(java.util.Calendar.MONTH) + 1)

    return "$startYear/$startMonth - $endYear/$endMonth"
}

/**
 * Card displaying a past rental with property info and action buttons
 */
@Composable
private fun PastRentalCard(
    item: LandlordRentalItem,
    onClick: () -> Unit,
    onInventoryClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    val rental = item.rental
    val property = item.property
    val tenant = item.tenant

    // Property card with "Terminé" badge and integrated content
    PropertyCard(
        property = property,
        onClick = onClick,
        variant = PropertyCardVariant.DETAILED,
        customBadgeText = stringResource(R.string.property_rental_completed),
        customBadgeVariant = BadgeVariant.INFO,
        hideDetails = true,
        bottomContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.large)
                    .padding(bottom = AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
            ) {
                // Rental details card with tenant info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.large)
                        .background(LocalAppColors.current.graySurface)
                        .padding(AppSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    // Tenant info with icon and name
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrueStayIcons.User.let { iconRes ->
                            ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon(
                                iconRes = iconRes,
                                contentDescriptionRes = null,
                                tint = LocalAppColors.current.black,
                                size = 16.dp
                            )
                        }
                        Text(
                            text = "${tenant.firstName} ${tenant.lastName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = LocalAppColors.current.black
                        )
                    }

                    // Rental period dates below
                    Text(
                        text = "${DateUtils.formatLocalDate(rental.startDate)} - ${DateUtils.formatLocalDate(rental.endDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalAppColors.current.grayDark
                    )
                }

                // Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    TrueStayButton(
                        text = stringResource(R.string.landlord_rentals_inventory_button),
                        onClick = onInventoryClick,
                        variant = ButtonVariant.SECONDARY,
                        leadingIcon = TrueStayIcons.FileText
                    )
                    TrueStayButton(
                        text = stringResource(R.string.landlord_rentals_reviews_button),
                        onClick = onReviewClick,
                        variant = ButtonVariant.SECONDARY,
                        leadingIcon = TrueStayIcons.MessageSquare
                    )
                }
            }
        }
    )
}