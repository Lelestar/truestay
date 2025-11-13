package ca.uqac.inf865.truestay.presentation.tenant.rentals

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.BadgeVariant
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.PropertyAction
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCard
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCardVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayBadge
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.shared.auth.rememberCurrentUser
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors

@Composable
fun RentalsScreen(
    onRentalClick: (String) -> Unit,
    onInventoryClick: (String) -> Unit,
    onReviewClick: (String) -> Unit,
    viewModel: RentalsViewModel = hiltViewModel()
) {
    val currentUser = rememberCurrentUser()
    val uiState = viewModel.uiState

    // Charger les locations au démarrage
    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { tenantId ->
            viewModel.loadRentals(tenantId)
        }
    }

    when {
        currentUser == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.favorites_error_not_authenticated),
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalAppColors.current.grayDark
                )
            }
        }
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LocalAppColors.current.primary)
            }
        }
        uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
                ) {
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalAppColors.current.error
                    )
                }
            }
        }
        else -> {
            RentalsContent(
                activeRental = uiState.activeRental,
                historicalRentals = uiState.historicalRentals,
                onRentalClick = onRentalClick,
                onInventoryClick = onInventoryClick,
                onReviewClick = onReviewClick
            )
        }
    }
}

@Composable
private fun RentalsContent(
    activeRental: RentalWithProperty?,
    historicalRentals: List<RentalWithProperty>,
    onRentalClick: (String) -> Unit,
    onInventoryClick: (String) -> Unit,
    onReviewClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppColors.current.white),
        contentPadding = PaddingValues(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalAppColors.current.primary)
                    .padding(AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                Text(
                    text = stringResource(R.string.rentals_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.white
                )
                Text(
                    text = stringResource(R.string.rentals_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.white
                )
            }
        }

        // Section Location actuelle
        if (activeRental != null) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
                ) {
                    Text(
                        text = stringResource(R.string.rentals_current_rental),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.black
                    )

                    // Badge Location actuelle sur la carte
                    Box {
                        PropertyCard(
                            property = activeRental.property,
                            onClick = { onRentalClick(activeRental.rental.id) },
                            variant = PropertyCardVariant.INTERACTIVE,
                            actions = listOf(
                                PropertyAction(
                                    iconRes = TrueStayIcons.ClipboardCheck,
                                    label = stringResource(R.string.rentals_edl_button),
                                    onClick = {
                                        activeRental.rental.entryInventoryId?.let {
                                            onInventoryClick(it)
                                        }
                                    },
                                    variant = ButtonVariant.PRIMARY
                                )
                            )
                        )

                        TrueStayBadge(
                            text = stringResource(R.string.rentals_current_rental),
                            variant = BadgeVariant.SUCCESS,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(AppSpacing.medium)
                        )
                    }
                }
            }
        }

        // Section Historique
        item {
            Spacer(modifier = Modifier.height(AppSpacing.large))
            Text(
                text = stringResource(R.string.rentals_history),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.black
            )
        }

        if (historicalRentals.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.rentals_no_history),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.grayDark,
                    modifier = Modifier.padding(vertical = AppSpacing.medium)
                )
            }
        } else {
            items(historicalRentals) { rentalWithProperty ->
                PropertyCard(
                    property = rentalWithProperty.property,
                    onClick = { onRentalClick(rentalWithProperty.rental.id) },
                    variant = PropertyCardVariant.COMPACT
                )
            }
        }
    }
}