package ca.uqac.inf865.truestay.presentation.tenant.rentals

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyRatings
import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.RentalStatus
import ca.uqac.inf865.truestay.domain.model.Room
import ca.uqac.inf865.truestay.domain.model.RoomType
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCard
import ca.uqac.inf865.truestay.presentation.common.components.PropertyAction
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCardVariant
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.common.utils.DateUtils
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import coil3.compose.AsyncImage

/**
 * Screen displaying tenant's rentals
 *
 * Shows current rental (if any) and rental history
 */
@Composable
fun RentalsScreen(
    onRentalClick: (String) -> Unit,
    onPropertyClick: (String) -> Unit,
    onInventoryClick: (String) -> Unit,
    onReviewClick: (String) -> Unit,
    viewModel: RentalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RentalsScreenContent(
        uiState = uiState,
        onRentalClick = onRentalClick,
        onPropertyClick = onPropertyClick,
        onInventoryClick = onInventoryClick,
        onReviewClick = onReviewClick,
        onRetry = { viewModel.refreshRentals() },
        onAccept = { id -> viewModel.acceptRental(id) },
        onDecline = { id -> viewModel.declineRental(id) },
        onDismissSnackbar = { viewModel.clearSnackbarMessage() }
    )
}

@Composable
private fun RentalsScreenContent(
    uiState: RentalsUiState,
    onRentalClick: (String) -> Unit,
    onPropertyClick: (String) -> Unit,
    onInventoryClick: (String) -> Unit,
    onReviewClick: (String) -> Unit,
    onRetry: () -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onDismissSnackbar: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Prepare localized messages
    val acceptedMessage = stringResource(R.string.rental_request_accepted)
    val declinedMessage = stringResource(R.string.rental_request_declined)
    val acceptErrorTemplate = stringResource(R.string.rental_request_accept_error)
    val declineErrorTemplate = stringResource(R.string.rental_request_decline_error)
    val loadErrorTemplate = stringResource(R.string.rental_request_load_error)

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { messageKey ->
            val message = when {
                messageKey == "RENTAL_REQUEST_ACCEPTED" -> acceptedMessage
                messageKey == "RENTAL_REQUEST_DECLINED" -> declinedMessage
                messageKey.startsWith("RENTAL_REQUEST_ACCEPT_ERROR|") -> {
                    val error = messageKey.substringAfter("|")
                    acceptErrorTemplate.format(error)
                }
                messageKey.startsWith("RENTAL_REQUEST_DECLINE_ERROR|") -> {
                    val error = messageKey.substringAfter("|")
                    declineErrorTemplate.format(error)
                }
                messageKey.startsWith("RENTAL_REQUEST_LOAD_ERROR|") -> {
                    val error = messageKey.substringAfter("|")
                    loadErrorTemplate.format(error)
                }
                else -> messageKey
            }
            snackbarHostState.showSnackbar(message)
            onDismissSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
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
                                    RentalsError.NOT_AUTHENTICATED -> stringResource(R.string.rentals_error_not_authenticated)
                                    RentalsError.LOAD_FAILED -> stringResource(R.string.rentals_error_loading)
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

                uiState.currentRental == null && uiState.pendingRentals.isEmpty() && uiState.pastRentals.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AppSpacing.large),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.rentals_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalAppColors.current.grayDark,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(AppSpacing.large)
                    ) {
                        // Pending rentals section
                        if (uiState.pendingRentals.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.rental_request_pending),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = LocalAppColors.current.black,
                                    modifier = Modifier.padding(bottom = AppSpacing.small, top = AppSpacing.xsmall)
                                )
                            }

                            items(uiState.pendingRentals) { item ->
                                PendingRentalCard(
                                    item = item,
                                    onPropertyClick = { onPropertyClick(item.property.id) },
                                    onAccept = { onAccept(item.rental.id) },
                                    onDecline = { onDecline(item.rental.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(AppSpacing.medium))
                            }
                        }

                        // Current rental section
                        item {
                            CurrentRentalSection(
                                currentRental = uiState.currentRental,
                                onRentalClick = onRentalClick,
                                onInventoryClick = onInventoryClick,
                                onReviewClick = onReviewClick
                            )
                        }

                        // History section
                        if (uiState.pastRentals.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.rentals_history),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = LocalAppColors.current.black,
                                    modifier = Modifier.padding(bottom = AppSpacing.small)
                                )
                            }

                            items(uiState.pastRentals) { rentalItem ->
                                PastRentalCard(
                                    item = rentalItem,
                                    onClick = { onRentalClick(rentalItem.rental.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
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
                text = stringResource(R.string.rentals_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.white
            )
        }
    }
}

/**
 * Section displaying current rental or empty state
 */
@Composable
private fun CurrentRentalSection(
    currentRental: RentalPropertyItem?,
    onRentalClick: (String) -> Unit,
    onInventoryClick: (String) -> Unit,
    onReviewClick: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.rentals_current_rental),
            style = MaterialTheme.typography.headlineSmall,
            color = LocalAppColors.current.black
        )
        Spacer(modifier = Modifier.height(AppSpacing.small))

        if (currentRental != null) {
            PropertyCard(
                property = currentRental.property,
                onClick = { onRentalClick(currentRental.rental.id) },
                variant = PropertyCardVariant.INTERACTIVE,
                startDate = currentRental.rental.startDate,
                endDate = currentRental.rental.endDate,
                actions = listOf(
                    PropertyAction(
                        iconRes = TrueStayIcons.FileText,
                        label = stringResource(R.string.rentals_action_inventory),
                        onClick = { onInventoryClick(currentRental.rental.id) },
                        variant = ButtonVariant.SECONDARY
                    ),
                    PropertyAction(
                        iconRes = TrueStayIcons.MessageSquare,
                        label = stringResource(R.string.rentals_action_review),
                        onClick = { onReviewClick(currentRental.rental.id) },
                        variant = ButtonVariant.PRIMARY
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = stringResource(R.string.rentals_no_current),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.large))
    }
}

@Composable
private fun PastRentalCard(
    item: RentalPropertyItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val property = item.property
    val startText = DateUtils.formatLocalDate(item.rental.startDate)
    val endText = DateUtils.formatLocalDate(item.rental.endDate)

    TrueStayCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        padding = 0.dp
    ) {
        Column {
            androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
                // Image
                Box(
                    modifier = Modifier
                        .size(80.dp, 85.dp)
                        .background(LocalAppColors.current.grayLight),
                    contentAlignment = Alignment.Center
                ) {
                    if (property.photos.isNotEmpty()) {
                        AsyncImage(
                            model = property.photos.first(),
                            contentDescription = property.name,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.img_placeholder),
                            error = painterResource(R.drawable.img_placeholder),
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.img_placeholder),
                            contentDescription = stringResource(R.string.property_image_placeholder),
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .padding(AppSpacing.medium)
                ) {
                    Text(
                        text = property.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = LocalAppColors.current.black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xsmall))

                    Text(
                        text = "${property.address.street}, ${property.address.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalAppColors.current.grayDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.small))

                    // Bottom row: dates and tenant rating (if any)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Dates
                        Text(
                            text = listOfNotNull(startText, endText).joinToString(" - "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalAppColors.current.grayDark
                        )

                        val rating = item.tenantRating
                        if (rating != null && rating > 0f) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TrueStayIcon(
                                    iconRes = TrueStayIcons.StarFilled,
                                    contentDescriptionRes = R.string.property_rating,
                                    tint = LocalAppColors.current.warning,
                                    size = 16.dp
                                )
                                Text(
                                    text = String.format(java.util.Locale.getDefault(), "%.1f", rating),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LocalAppColors.current.black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card for pending rental requests
 * Uses PropertyCard layout but with Accept/Decline actions instead
 */
@Composable
private fun PendingRentalCard(
    item: RentalPropertyItem,
    onPropertyClick: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    PropertyCard(
        property = item.property,
        onClick = onPropertyClick,
        variant = PropertyCardVariant.INTERACTIVE,
        startDate = item.rental.startDate,
        endDate = item.rental.endDate,
        showTimeRemaining = false,
        showStatusBadge = false,
        actions = listOf(
            PropertyAction(
                iconRes = TrueStayIcons.X,
                label = stringResource(R.string.rental_request_decline),
                onClick = onDecline,
                variant = ButtonVariant.SECONDARY
            ),
            PropertyAction(
                iconRes = TrueStayIcons.Check,
                label = stringResource(R.string.rental_request_accept),
                onClick = onAccept,
                variant = ButtonVariant.PRIMARY
            )
        ),
        modifier = modifier
    )
}


// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
private fun RentalsScreenLoadingPreview() {
    TrueStayTheme {
        RentalsScreenContent(
            uiState = RentalsUiState(isLoading = true),
            onRentalClick = {},
            onInventoryClick = {},
            onReviewClick = {},
            onPropertyClick = {},
            onAccept = {},
            onDecline = {},
            onRetry = {},
            onDismissSnackbar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RentalsScreenWithCurrentRentalPreview() {
    val property = Property(
        id = "1",
        name = "Beau 3½ avec vue sur le Saguenay",
        address = Address(
            street = "123 Rue des Érables",
            city = "Saguenay",
            province = "QC",
            postalCode = "G7H 1A1",
            country = "Canada",
            latitude = 48.4284,
            longitude = -71.0656
        ),
        monthlyRent = 950,
        surface = 75,
        rooms = listOf(
            Room(name = "Chambre principale", type = RoomType.BEDROOM),
            Room(name = "Salon", type = RoomType.LIVING_ROOM),
            Room(name = "Cuisine", type = RoomType.KITCHEN),
            Room(name = "Salle de bain", type = RoomType.BATHROOM)
        ),
        photos = listOf("https://example.com/photo1.jpg"),
        landlordId = "owner1",
        ratings = PropertyRatings(
            propertyAverageRating = 4.5f,
            propertyReviewCount = 12,
            buildingAverageRating = 4.3f,
            neighborhoodAverageRating = 4.7f
        ),
        isAvailable = false
    )

    val rental = Rental(
        id = "rental1",
        propertyId = "1",
        tenantId = "tenant1",
        landlordId = "owner1",
        startDate = System.currentTimeMillis(),
        endDate = System.currentTimeMillis() + 31536000000L,
        status = RentalStatus.ACTIVE
    )

    TrueStayTheme {
        RentalsScreenContent(
            uiState = RentalsUiState(
                currentRental = RentalPropertyItem(rental, property),
                pastRentals = emptyList(),
                isLoading = false
            ),
            onRentalClick = {},
            onInventoryClick = {},
            onReviewClick = {},
            onPropertyClick = {},
            onAccept = {},
            onDecline = {},
            onRetry = {},
            onDismissSnackbar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RentalsScreenWithHistoryPreview() {
    val currentProperty = Property(
        id = "1",
        name = "Beau 3½ avec vue sur le Saguenay",
        address = Address(
            street = "123 Rue des Érables",
            city = "Saguenay",
            province = "QC",
            postalCode = "G7H 1A1",
            country = "Canada",
            latitude = 48.4284,
            longitude = -71.0656
        ),
        monthlyRent = 950,
        surface = 75,
        rooms = listOf(
            Room(name = "Chambre principale", type = RoomType.BEDROOM)
        ),
        photos = listOf("https://example.com/photo1.jpg"),
        landlordId = "owner1",
        ratings = PropertyRatings(propertyAverageRating = 4.5f),
        isAvailable = false
    )

    val pastProperty1 = Property(
        id = "2",
        name = "Studio moderne centre-ville",
        address = Address(
            street = "456 Boulevard Talbot",
            city = "Chicoutimi",
            province = "QC",
            postalCode = "G7H 4B1",
            country = "Canada",
            latitude = 48.4284,
            longitude = -71.0656
        ),
        monthlyRent = 750,
        surface = 45,
        rooms = listOf(
            Room(name = "Studio", type = RoomType.LIVING_ROOM)
        ),
        photos = listOf("https://example.com/photo2.jpg"),
        landlordId = "owner2",
        ratings = PropertyRatings(propertyAverageRating = 4.2f),
        isAvailable = true
    )

    val currentRental = Rental(
        id = "rental1",
        propertyId = "1",
        tenantId = "tenant1",
        landlordId = "owner1",
        startDate = System.currentTimeMillis(),
        status = RentalStatus.ACTIVE
    )

    val pastRental1 = Rental(
        id = "rental2",
        propertyId = "2",
        tenantId = "tenant1",
        landlordId = "owner2",
        startDate = System.currentTimeMillis() - 63072000000L,
        endDate = System.currentTimeMillis() - 31536000000L,
        status = RentalStatus.ENDED
    )

    TrueStayTheme {
        RentalsScreenContent(
            uiState = RentalsUiState(
                currentRental = RentalPropertyItem(currentRental, currentProperty),
                pastRentals = listOf(
                    RentalPropertyItem(pastRental1, pastProperty1)
                ),
                isLoading = false
            ),
            onRentalClick = {},
            onInventoryClick = {},
            onReviewClick = {},
            onPropertyClick = {},
            onAccept = {},
            onDecline = {},
            onRetry = {},
            onDismissSnackbar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RentalsScreenEmptyPreview() {
    TrueStayTheme {
        RentalsScreenContent(
            uiState = RentalsUiState(
                currentRental = null,
                pastRentals = emptyList(),
                isLoading = false
            ),
            onRentalClick = {},
            onInventoryClick = {},
            onReviewClick = {},
            onPropertyClick = {},
            onAccept = {},
            onDecline = {},
            onRetry = {},
            onDismissSnackbar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RentalsScreenErrorPreview() {
    TrueStayTheme {
        RentalsScreenContent(
            uiState = RentalsUiState(
                currentRental = null,
                pastRentals = emptyList(),
                isLoading = false,
                error = RentalsError.LOAD_FAILED
            ),
            onRentalClick = {},
            onInventoryClick = {},
            onReviewClick = {},
            onPropertyClick = {},
            onAccept = {},
            onDecline = {},
            onRetry = {},
            onDismissSnackbar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RentalsScreenWithPendingRentalPreview() {
    val pendingProperty = Property(
        id = "3",
        name = "Charmant 4½ rénové avec stationnement",
        address = Address(
            street = "789 Rue Racine",
            city = "Chicoutimi",
            province = "QC",
            postalCode = "G7H 2B9",
            country = "Canada",
            latitude = 48.4284,
            longitude = -71.0656
        ),
        monthlyRent = 1150,
        surface = 95,
        rooms = listOf(
            Room(name = "Chambre 1", type = RoomType.BEDROOM),
            Room(name = "Chambre 2", type = RoomType.BEDROOM),
            Room(name = "Salon", type = RoomType.LIVING_ROOM),
            Room(name = "Cuisine", type = RoomType.KITCHEN),
            Room(name = "Salle de bain", type = RoomType.BATHROOM)
        ),
        photos = listOf("https://example.com/photo3.jpg"),
        landlordId = "owner3",
        ratings = PropertyRatings(
            propertyAverageRating = 4.8f,
            propertyReviewCount = 24,
            buildingAverageRating = 4.6f,
            neighborhoodAverageRating = 4.5f
        ),
        isAvailable = true
    )

    val currentProperty = Property(
        id = "1",
        name = "Beau 3½ avec vue sur le Saguenay",
        address = Address(
            street = "123 Rue des Érables",
            city = "Saguenay",
            province = "QC",
            postalCode = "G7H 1A1",
            country = "Canada",
            latitude = 48.4284,
            longitude = -71.0656
        ),
        monthlyRent = 950,
        surface = 75,
        rooms = listOf(
            Room(name = "Chambre principale", type = RoomType.BEDROOM)
        ),
        photos = listOf("https://example.com/photo1.jpg"),
        landlordId = "owner1",
        ratings = PropertyRatings(propertyAverageRating = 4.5f),
        isAvailable = false
    )

    val pendingRental = Rental(
        id = "rental3",
        propertyId = "3",
        tenantId = "tenant1",
        landlordId = "owner3",
        startDate = System.currentTimeMillis() + 2592000000L, // Dans 30 jours
        endDate = System.currentTimeMillis() + 33696000000L, // 1 an après
        status = RentalStatus.PENDING
    )

    val currentRental = Rental(
        id = "rental1",
        propertyId = "1",
        tenantId = "tenant1",
        landlordId = "owner1",
        startDate = System.currentTimeMillis(),
        endDate = System.currentTimeMillis() + 31536000000L,
        status = RentalStatus.ACTIVE
    )

    TrueStayTheme {
        RentalsScreenContent(
            uiState = RentalsUiState(
                pendingRentals = listOf(
                    RentalPropertyItem(pendingRental, pendingProperty)
                ),
                currentRental = RentalPropertyItem(currentRental, currentProperty),
                pastRentals = emptyList(),
                isLoading = false
            ),
            onRentalClick = {},
            onInventoryClick = {},
            onReviewClick = {},
            onPropertyClick = {},
            onAccept = {},
            onDecline = {},
            onRetry = {},
            onDismissSnackbar = {}
        )
    }
}
