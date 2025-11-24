package ca.uqac.inf865.truestay.presentation.shared.rental

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.presentation.common.components.BadgeVariant
import ca.uqac.inf865.truestay.presentation.common.components.FullscreenImageCarouselOverlay
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayBadge
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.ImageCarousel
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.common.utils.DateUtils
import ca.uqac.inf865.truestay.presentation.shared.property.PropertyFeaturesSection
import ca.uqac.inf865.truestay.presentation.shared.property.PropertyHeaderSection
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.domain.model.Inventory
import ca.uqac.inf865.truestay.domain.model.InventoryStatus
import ca.uqac.inf865.truestay.domain.model.RentalStatus
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.compose.ui.tooling.preview.Preview
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.PropertyStatus
import ca.uqac.inf865.truestay.domain.model.Room
import ca.uqac.inf865.truestay.domain.model.RoomType
import ca.uqac.inf865.truestay.domain.model.InventoryType
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

enum class RentalDetailsSection {
    INFO,
    PROGRESS,
    INVENTORIES,
    REVIEWS
}

@Composable
fun RentalDetailsScreen(
    rentalId: String,
    onBackClick: () -> Unit,
    initialSection: RentalDetailsSection? = null,
    onAddOrEditReviewClick: (String) -> Unit, // Tenant only
    onInventoryClick: (String) -> Unit,
    onEndLease: (() -> Unit)? = null, // Landlord only
    viewModel: RentalDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFullscreenCarousel by remember { mutableStateOf(false) }
    var fullscreenStartIndex by remember { mutableIntStateOf(0) }
    var fullscreenPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var fullscreenTitle by remember { mutableStateOf("") }

    LaunchedEffect(rentalId) {
        viewModel.loadRental(rentalId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!showFullscreenCarousel) {
            ca.uqac.inf865.truestay.presentation.navigation.TrueStayTopAppBar(
                titleRes = R.string.screen_title_rental_details,
                onNavigateBack = onBackClick,
                windowInsets = WindowInsets(0.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading && uiState.property == null -> {
                    CircularProgressIndicator(color = LocalAppColors.current.primary)
                }

                uiState.errorRes != null && uiState.property == null -> {
                    RentalDetailsErrorState(
                        messageRes = uiState.errorRes!!,
                        onRetry = { viewModel.loadRental(rentalId) }
                    )
                }

                uiState.property != null && uiState.rental != null -> {
                    val isLandlord = uiState.currentUser?.id == uiState.rental!!.landlordId
                    RentalDetailsContent(
                        property = uiState.property!!,
                        rental = uiState.rental!!,
                        review = uiState.review,
                        isLandlord = isLandlord,
                        tenantFirstName = uiState.tenant?.firstName,
                        tenant = uiState.tenant,
                        landlord = uiState.landlord,
                        entryInventory = uiState.entryInventory,
                        exitInventory = uiState.exitInventory,
                        initialSection = initialSection,
                        onImageClick = { index ->
                            fullscreenPhotos = uiState.property!!.photos
                            fullscreenTitle = uiState.property!!.name
                            fullscreenStartIndex = index
                            showFullscreenCarousel = true
                        },
                        onInventoryClick = { inventoryId ->
                            onInventoryClick(inventoryId)
                        },
                        onAddOrEditReviewClick = onAddOrEditReviewClick
                    )
                }
            }

            if (showFullscreenCarousel && fullscreenPhotos.isNotEmpty()) {
                FullscreenImageCarouselOverlay(
                    photos = fullscreenPhotos,
                    title = fullscreenTitle,
                    initialPage = fullscreenStartIndex,
                    onClose = { showFullscreenCarousel = false }
                )
            }
        }
    }
}

@Composable
private fun RentalDetailsErrorState(
    messageRes: Int,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.large)
    ) {
        Text(
            text = stringResource(id = messageRes),
            style = MaterialTheme.typography.titleMedium,
            color = LocalAppColors.current.error
        )
        Spacer(modifier = Modifier.height(AppSpacing.large))
        TrueStayButton(
            text = stringResource(R.string.common_retry),
            onClick = onRetry
        )
    }
}

@Composable
private fun RentalDetailsContent(
    property: Property,
    rental: Rental,
    review: ca.uqac.inf865.truestay.domain.model.Review?,
    isLandlord: Boolean,
    tenantFirstName: String?,
    tenant: User?,
    landlord: User?,
    entryInventory: Inventory?,
    exitInventory: Inventory?,
    initialSection: RentalDetailsSection?,
    onImageClick: ((Int) -> Unit)? = null,
    onInventoryClick: (String) -> Unit,
    onAddOrEditReviewClick: (String) -> Unit
) {
    val hasInventories = entryInventory != null || exitInventory != null
    val listState = rememberLazyListState()
    var hasScrolledToInitial by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            bottom = AppSpacing.large
        )
    ) {
        // Image carousel with status & price badges
        item {
            RentalImageSection(
                property = property,
                rental = rental,
                onImageClick = onImageClick
            )
        }

        // Main info block: title, address, features, rental period
        item {
            Column(
                modifier = Modifier
                    .background(LocalAppColors.current.white)
                    .padding(AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
            ) {
                PropertyHeaderSection(property = property)
                PropertyFeaturesSection(property = property)
                RentalContactSection(
                    isLandlord = isLandlord,
                    tenant = tenant,
                    landlord = landlord
                )
                RentalPeriodSection(
                    startDate = rental.startDate.takeIf { it > 0L },
                    endDate = rental.endDate.takeIf { it > 0L }
                )
            }
        }

        item {
            HorizontalDivider(color = LocalAppColors.current.grayBorder)
        }

        item {
            RentalProgressSection(
                rental = rental,
                entryInventory = entryInventory,
                exitInventory = exitInventory
            )
        }

        if (entryInventory != null || exitInventory != null) {
            item {
                RentalInventoriesSection(
                    entryInventory = entryInventory,
                    exitInventory = exitInventory,
                    onInventoryClick = onInventoryClick
                )
            }
        }

        item {
            RentalReviewsSection(
                property = property,
                review = review,
                isLandlord = isLandlord,
                tenantFirstName = tenantFirstName,
                onAddOrEditReviewClick = onAddOrEditReviewClick
            )
        }

        // Empty spacer at the end to allow full scrolling to reviews section
        item {
            Spacer(modifier = Modifier.height(1.dp))
        }
    }

    LaunchedEffect(initialSection, hasInventories) {
        if (!hasScrolledToInitial && initialSection != null) {
            // Calculate the total number of items (including the final spacer)
            val totalItems = 4 + (if (hasInventories) 1 else 0) + 1 + 1 // base items + inventories section + reviews section + final spacer

            val targetIndex = when (initialSection) {
                RentalDetailsSection.INFO -> 1 // main info block
                RentalDetailsSection.PROGRESS -> 3
                RentalDetailsSection.INVENTORIES -> if (hasInventories) 4 else 3
                RentalDetailsSection.REVIEWS -> totalItems - 1 // Last item (final spacer after reviews) to show reviews fully
            }

            // Wait a bit for the list to be laid out
            kotlinx.coroutines.delay(100)
            listState.animateScrollToItem(targetIndex, scrollOffset = 0)
            hasScrolledToInitial = true
        }
    }
}


@Composable
private fun RentalImageSection(
    property: Property,
    rental: Rental,
    onImageClick: ((Int) -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(256.dp)
            .background(LocalAppColors.current.grayLight)
    ) {
        ImageCarousel(
            photos = property.photos,
            contentDescription = property.name,
            modifier = Modifier.fillMaxSize(),
            onImageClick = onImageClick
        )

        val statusText = when (rental.status) {
            RentalStatus.PENDING -> stringResource(R.string.rental_status_pending)
            RentalStatus.ACTIVE -> stringResource(R.string.rental_status_active)
            RentalStatus.ENDED -> stringResource(R.string.rental_status_ended)
            RentalStatus.CANCELLED -> stringResource(R.string.rental_status_cancelled)
        }
        val statusVariant = when (rental.status) {
            RentalStatus.PENDING -> BadgeVariant.INFO
            RentalStatus.ACTIVE -> BadgeVariant.SUCCESS
            RentalStatus.ENDED -> BadgeVariant.WARNING
            RentalStatus.CANCELLED -> BadgeVariant.ERROR
        }

        TrueStayBadge(
            text = statusText,
            variant = statusVariant,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(AppSpacing.medium)
        )

        TrueStayBadge(
            text = stringResource(R.string.property_monthly_rent, property.monthlyRent),
            variant = BadgeVariant.INFO,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AppSpacing.medium)
        )
    }
}

@Composable
private fun RentalContactSection(
    isLandlord: Boolean,
    tenant: User?,
    landlord: User?
) {
    val context = LocalContext.current

    val contactUser = if (isLandlord) tenant else landlord
    if (contactUser == null) return

    val roleLabel = if (isLandlord) {
        stringResource(R.string.rental_contact_role_tenant)
    } else {
        stringResource(R.string.rental_contact_role_landlord)
    }

    val fullName = listOfNotNull(contactUser.firstName, contactUser.lastName)
        .joinToString(" ")
        .ifBlank { contactUser.email }

    val email = contactUser.email
    val phone = contactUser.phoneNumber

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalAppColors.current.graySurface, shape = AppShapes.medium)
            .padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        // Role + name
        Text(
            text = "$roleLabel – $fullName",
            style = MaterialTheme.typography.titleMedium,
            color = LocalAppColors.current.black
        )

        // Email row
        if (email.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val uri = "mailto:$email".toUri()
                        val intent = Intent(Intent.ACTION_SENDTO, uri)
                        context.startActivity(intent)
                    },
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrueStayIcon(
                    iconRes = TrueStayIcons.Mail,
                    contentDescriptionRes = null,
                    tint = LocalAppColors.current.grayDark,
                    size = 16.dp
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.grayDark
                )
            }
        }

        // Phone row
        if (phone.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val uri = "tel:$phone".toUri()
                        val intent = Intent(Intent.ACTION_DIAL, uri)
                        context.startActivity(intent)
                    },
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrueStayIcon(
                    iconRes = TrueStayIcons.Phone,
                    contentDescriptionRes = null,
                    tint = LocalAppColors.current.grayDark,
                    size = 16.dp
                )
                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.grayDark
                )
            }
        }
    }
}

@Composable
private fun RentalPeriodSection(
    startDate: Long?,
    endDate: Long?
) {
    val startText = startDate?.let { DateUtils.formatLocalDate(it) }
    val endText = endDate?.let { DateUtils.formatLocalDate(it) }

    if (startText == null && endText == null) return

    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalAppColors.current.graySurface, shape = AppShapes.medium)
            .padding(AppSpacing.large)
    ) {
        if (startText != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrueStayIcon(
                        iconRes = TrueStayIcons.Calendar,
                        contentDescriptionRes = null,
                        tint = LocalAppColors.current.grayDark,
                        size = 16.dp
                    )
                    Text(
                        text = stringResource(R.string.property_rental_start_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalAppColors.current.grayDark
                    )
                }
                Text(
                    text = startText,
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalAppColors.current.black
                )
            }
        }

        if (endText != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrueStayIcon(
                        iconRes = TrueStayIcons.Calendar,
                        contentDescriptionRes = null,
                        tint = LocalAppColors.current.grayDark,
                        size = 16.dp
                    )
                    Text(
                        text = stringResource(R.string.property_rental_end_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalAppColors.current.grayDark
                    )
                }
                Text(
                    text = endText,
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalAppColors.current.black
                )
            }
        }

        val daysLeft = endDate?.let { computeDaysLeft(it) }
        if (daysLeft != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrueStayIcon(
                        iconRes = TrueStayIcons.History,
                        contentDescriptionRes = null,
                        tint = LocalAppColors.current.grayDark,
                        size = 16.dp
                    )
                    Text(
                        text = stringResource(R.string.property_rental_days_left_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalAppColors.current.grayDark
                    )
                }
                Text(
                    text = stringResource(
                        R.string.property_rental_days_left,
                        daysLeft.coerceAtLeast(0)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalAppColors.current.primary
                )
            }
        }
    }
}

private fun computeDaysLeft(endTimestamp: Long): Int {
    val now = System.currentTimeMillis()
    val millisInDay = 24L * 60L * 60L * 1000L
    val diff = endTimestamp - now
    return (diff / millisInDay).toInt()
}

@Composable
private fun RentalProgressSection(
    rental: Rental,
    entryInventory: Inventory?,
    exitInventory: Inventory?
) {
    val entryCompleted = isInventoryCompleted(entryInventory)
    val exitCompleted = isInventoryCompleted(exitInventory)
    val rentalEnded = rental.status == RentalStatus.ENDED || rental.status == RentalStatus.CANCELLED

    val currentStepIndex: Int? = when {
        !entryCompleted -> 1
        rental.status == RentalStatus.ACTIVE -> 2
        rentalEnded && !exitCompleted -> 3
        else -> null
    }

    val entryState = when {
        entryCompleted -> StepState.COMPLETED
        currentStepIndex == 1 -> StepState.ACTIVE
        else -> StepState.PENDING
    }

    val ongoingState = when {
        rentalEnded -> StepState.COMPLETED
        currentStepIndex == 2 -> StepState.ACTIVE
        else -> StepState.PENDING
    }

    val exitState = when {
        exitCompleted -> StepState.COMPLETED
        currentStepIndex == 3 -> StepState.ACTIVE
        else -> StepState.PENDING
    }

    val entrySubtitle = if (entryCompleted && entryInventory?.completedAt != null) {
        val dateText = DateUtils.formatLocalDate(entryInventory.completedAt) ?: ""
        if (dateText.isNotEmpty()) {
            stringResource(R.string.rental_progress_step_done_on, dateText)
        } else {
            stringResource(R.string.rental_progress_step_todo)
        }
    } else {
        stringResource(R.string.rental_progress_step_todo)
    }

    val ongoingSubtitle = rental.startDate.takeIf { it > 0L }?.let { start ->
        val days = if (rentalEnded && rental.endDate > 0L) {
            computeDaysBetween(start, rental.endDate)
        } else {
            computeDaysSince(start)
        }
        if (days >= 0) {
            if (rentalEnded && rental.endDate > 0L) {
                stringResource(R.string.rental_progress_step_during_days, days)
            } else {
                stringResource(R.string.rental_progress_step_since_days, days)
            }
        } else ""
    } ?: ""

    val exitSubtitle = if (exitCompleted && exitInventory?.completedAt != null) {
        val dateText = DateUtils.formatLocalDate(exitInventory.completedAt) ?: ""
        if (dateText.isNotEmpty()) {
            stringResource(R.string.rental_progress_step_done_on, dateText)
        } else {
            stringResource(R.string.rental_progress_step_todo)
        }
    } else {
        stringResource(R.string.rental_progress_step_todo)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalAppColors.current.white)
            .padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Text(
            text = stringResource(R.string.rental_progress_title),
            style = MaterialTheme.typography.headlineSmall,
            color = LocalAppColors.current.black
        )

        Column {
            RentalProgressStep(
                title = stringResource(R.string.rental_progress_step_entry),
                subtitle = entrySubtitle,
                state = entryState,
                isLast = false
            )
            RentalProgressStep(
                title = stringResource(R.string.rental_progress_step_ongoing),
                subtitle = ongoingSubtitle,
                state = ongoingState,
                isLast = false
            )
            RentalProgressStep(
                title = stringResource(R.string.rental_progress_step_exit),
                subtitle = exitSubtitle,
                state = exitState,
                isLast = true
            )
        }
    }
}

@Composable
private fun RentalProgressStep(
    title: String,
    subtitle: String,
    state: StepState,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val indicatorSize = 20.dp

            Box(
                modifier = Modifier
                    .size(indicatorSize)
                    .let { base ->
                        when (state) {
                            StepState.ACTIVE -> base
                                .background(LocalAppColors.current.white, shape = CircleShape)
                                .border(
                                    width = 4.dp,
                                    color = LocalAppColors.current.primary,
                                    shape = CircleShape
                                )
                            StepState.COMPLETED -> base
                                .background(LocalAppColors.current.success, shape = CircleShape)
                            StepState.PENDING -> base
                                .background(LocalAppColors.current.grayLight, shape = CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = LocalAppColors.current.grayDefault,
                                    shape = CircleShape
                                )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state == StepState.COMPLETED) {
                    TrueStayIcon(
                        iconRes = TrueStayIcons.Check,
                        contentDescriptionRes = null,
                        tint = LocalAppColors.current.white,
                        size = 12.dp
                    )
                }
            }

            if (!isLast) {
                Spacer(modifier = Modifier.height(AppSpacing.xsmall))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(LocalAppColors.current.grayBorder)
                )
                Spacer(modifier = Modifier.height(AppSpacing.xsmall))
            }
        }

        Column(
            modifier = Modifier
                .padding(start = AppSpacing.small)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
        ) {
            val titleColor = when (state) {
                StepState.ACTIVE -> LocalAppColors.current.primary
                else -> LocalAppColors.current.black
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor
            )

            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.grayDark
                )
            }
        }
    }
}

private enum class StepState {
    COMPLETED,
    ACTIVE,
    PENDING
}

private fun isInventoryCompleted(inventory: Inventory?): Boolean {
    return inventory?.completedAt != null
}

private fun computeDaysSince(startTimestamp: Long): Int {
    val now = System.currentTimeMillis()
    val millisInDay = 24L * 60L * 60L * 1000L
    val diff = now - startTimestamp
    return (diff / millisInDay).toInt()
}

private fun computeDaysBetween(startTimestamp: Long, endTimestamp: Long): Int {
    val millisInDay = 24L * 60L * 60L * 1000L
    val diff = endTimestamp - startTimestamp
    return (diff / millisInDay).toInt()
}

@Composable
private fun RentalInventoriesSection(
    entryInventory: Inventory?,
    exitInventory: Inventory?,
    onInventoryClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Text(
            text = stringResource(R.string.rental_inventories_title),
            style = MaterialTheme.typography.headlineSmall,
            color = LocalAppColors.current.black
        )

        entryInventory?.let {
            InventorySummaryCard(
                inventory = it,
                title = stringResource(R.string.rental_inventory_entry_title),
                onClick = { onInventoryClick(it.id) }
            )
        }

        exitInventory?.let {
            InventorySummaryCard(
                inventory = it,
                title = stringResource(R.string.rental_inventory_exit_title),
                onClick = { onInventoryClick(it.id) }
            )
        }
    }
}

@Composable
fun InventorySummaryCard(
    inventory: Inventory,
    title: String,
    onClick: () -> Unit
) {
    val completedText = inventory.completedAt?.let { completedAt ->
        DateUtils.formatLocalDate(completedAt)?.let { date ->
            stringResource(R.string.rental_progress_step_done_on, date)
        }
    }

    val subtitle = completedText ?: stringResource(R.string.rental_inventory_not_done)

    val (statusText, statusVariant) = when (inventory.status) {
        InventoryStatus.DRAFT -> stringResource(R.string.rental_inventory_status_draft) to BadgeVariant.NEUTRAL
        InventoryStatus.IN_PROGRESS -> stringResource(R.string.rental_inventory_status_in_progress) to BadgeVariant.INFO
        InventoryStatus.PENDING_SIGNATURE -> stringResource(R.string.rental_inventory_status_pending_signature) to BadgeVariant.WARNING
        InventoryStatus.SIGNED -> stringResource(R.string.rental_inventory_status_signed) to BadgeVariant.SUCCESS
        InventoryStatus.COMPLETED -> stringResource(R.string.rental_inventory_status_completed) to BadgeVariant.SUCCESS
        InventoryStatus.CANCELLED -> stringResource(R.string.rental_inventory_status_cancelled) to BadgeVariant.ERROR
    }

    TrueStayCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TrueStayIcon(
                iconRes = TrueStayIcons.FileText,
                contentDescriptionRes = null,
                tint = LocalAppColors.current.primary,
                size = 24.dp,
                modifier = Modifier
                    .background(LocalAppColors.current.primarySurface, shape = AppShapes.medium)
                    .padding(AppSpacing.small)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = AppSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalAppColors.current.black
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.grayDark
                )
            }

            TrueStayBadge(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = AppSpacing.small),
                text = statusText,
                variant = statusVariant
            )

            TrueStayIcon(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = AppSpacing.small),
                iconRes = TrueStayIcons.ChevronRight,
                contentDescriptionRes = null,
                tint = LocalAppColors.current.grayDark,
                size = 16.dp
            )
        }
    }
}

@Composable
private fun RentalReviewsSection(
    property: Property,
    review: ca.uqac.inf865.truestay.domain.model.Review?,
    isLandlord: Boolean,
    tenantFirstName: String?,
    onAddOrEditReviewClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.large)
            .padding(horizontal = AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Text(
            text = stringResource(R.string.rental_reviews_title),
            style = MaterialTheme.typography.headlineSmall,
            color = LocalAppColors.current.black
        )

        Column (
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            ReviewCategoryCard(
                categoryLabel = stringResource(R.string.rental_reviews_category_property),
                rating = review?.propertyReview?.overallRating,
                comment = review?.propertyReview?.comment,
                createdAt = review?.createdAt,
                hasReview = review?.propertyReview != null,
                isLandlord = isLandlord,
                tenantFirstName = tenantFirstName,
                emptyTextRes = R.string.rental_reviews_none_property,
                emptyTenantTextRes = R.string.rental_reviews_none_property_tenant,
                onEditClick = { onAddOrEditReviewClick("PROPERTY") }
            )

            if (property.isInBuilding) {
                ReviewCategoryCard(
                    categoryLabel = stringResource(R.string.rental_reviews_category_building),
                    rating = review?.buildingReview?.overallRating,
                    comment = review?.buildingReview?.comment,
                    createdAt = review?.createdAt,
                    hasReview = review?.buildingReview != null,
                    isLandlord = isLandlord,
                    tenantFirstName = tenantFirstName,
                    emptyTextRes = R.string.rental_reviews_none_building,
                    emptyTenantTextRes = R.string.rental_reviews_none_building_tenant,
                    onEditClick = { onAddOrEditReviewClick("BUILDING") }
                )
            }

            ReviewCategoryCard(
                categoryLabel = stringResource(R.string.rental_reviews_category_neighborhood),
                rating = review?.neighborhoodReview?.overallRating,
                comment = review?.neighborhoodReview?.comment,
                createdAt = review?.createdAt,
                hasReview = review?.neighborhoodReview != null,
                isLandlord = isLandlord,
                tenantFirstName = tenantFirstName,
                emptyTextRes = R.string.rental_reviews_none_neighborhood,
                emptyTenantTextRes = R.string.rental_reviews_none_neighborhood_tenant,
                onEditClick = { onAddOrEditReviewClick("NEIGHBORHOOD") }
            )
        }
    }
}

@Composable
private fun ReviewCategoryCard(
    categoryLabel: String,
    rating: Float?,
    comment: String?,
    createdAt: Long?,
    hasReview: Boolean,
    isLandlord: Boolean,
    tenantFirstName: String?,
    emptyTextRes: Int,
    emptyTenantTextRes: Int,
    onEditClick: () -> Unit
) {
    TrueStayCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = categoryLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = LocalAppColors.current.black
                    )

                    if (hasReview && rating != null && rating > 0f) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrueStayIcon(
                                iconRes = TrueStayIcons.StarFilled,
                                contentDescriptionRes = null,
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

                if (!isLandlord && hasReview) {
                    TrueStayIcon(
                        iconRes = TrueStayIcons.PenLine,
                        contentDescriptionRes = null,
                        tint = LocalAppColors.current.black,
                        size = 20.dp,
                        modifier = Modifier
                            .clip(AppShapes.small)
                            .clickable(onClick = onEditClick)
                    )
                }
            }

            val dateText = createdAt?.let { DateUtils.formatLocalDate(it) }

            if (hasReview && (dateText != null || !comment.isNullOrBlank())) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
                ) {
                    if (!comment.isNullOrBlank()) {
                        Text(
                            text = comment,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalAppColors.current.grayDark
                        )
                    }
                    if (dateText != null) {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalAppColors.current.grayMedium
                        )
                    }
                }
            } else {
                val message = if (isLandlord) {
                    val name = tenantFirstName ?: ""
                    stringResource(emptyTenantTextRes, name)
                } else {
                    stringResource(emptyTextRes)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalAppColors.current.grayDark
                    )

                    if (!isLandlord && !hasReview) {
                        TrueStayButton(
                            text = stringResource(R.string.rental_reviews_add_button),
                            onClick = onEditClick,
                            variant = ButtonVariant.SECONDARY,
                            leadingIcon = TrueStayIcons.MessageSquare
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true, name = "RentalDetails - Error")
@Composable
private fun RentalDetailsErrorPreview() {
    TrueStayTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                RentalDetailsErrorState(
                    messageRes = R.string.rental_details_error_loading,
                    onRetry = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "RentalDetails - Content (Tenant)")
@Composable
private fun RentalDetailsContentTenantPreview() {
    TrueStayTheme {
        val property = Property(
            id = "prop-1",
            name = "Appartement moderne 2 pièces",
            address = Address(
                street = "15 Rue de la Paix",
                city = "Paris",
                postalCode = "75001"
            ),
            description = "Bel appartement rénové, proche des commodités et transports.",
            monthlyRent = 1800,
            surface = 65,
            rooms = listOf(
                Room(name = "Chambre", type = RoomType.BEDROOM),
                Room(name = "Salle de bain", type = RoomType.BATHROOM)
            ),
            photos = listOf("https://picsum.photos/seed/11/600/400"),
            isInBuilding = true,
            isAvailable = true,
            status = PropertyStatus.PUBLISHED
        )

        val now = System.currentTimeMillis()
        val rental = Rental(
            id = "rental-1",
            propertyId = property.id,
            tenantId = "tenant-1",
            landlordId = "landlord-1",
            startDate = now - 10L * 24L * 60L * 60L * 1000L,
            endDate = now + 20L * 24L * 60L * 60L * 1000L,
            status = RentalStatus.ACTIVE
        )

        val entryInventory = Inventory(
            id = "inv-entry",
            rentalId = rental.id,
            type = InventoryType.ENTRY,
            status = InventoryStatus.COMPLETED,
            completedAt = now - 9L * 24L * 60L * 60L * 1000L
        )

        val exitInventory = Inventory(
            id = "inv-exit",
            rentalId = rental.id,
            type = InventoryType.EXIT,
            status = InventoryStatus.IN_PROGRESS
        )

        val tenant = User(
            id = "tenant-1",
            firstName = "Alice",
            lastName = "Durand",
            email = "alice@example.com",
            phoneNumber = "+33123456789"
        )

        val landlord = User(
            id = "landlord-1",
            firstName = "Bob",
            lastName = "Martin",
            email = "bob@example.com",
            phoneNumber = "+33987654321"
        )

        val review = ca.uqac.inf865.truestay.domain.model.Review(
            id = "review-1",
            rentalId = rental.id,
            propertyId = property.id,
            tenantId = tenant.id,
            propertyReview = ca.uqac.inf865.truestay.domain.model.PropertyReview(
                overallRating = 4.5f,
                comment = "Très bon logement, calme et bien situé."
            ),
            createdAt = now - 3L * 24L * 60L * 60L * 1000L
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2000.dp)
        ) {
            RentalDetailsContent(
                property = property,
                rental = rental,
                review = review,
                isLandlord = false,
                tenantFirstName = tenant.firstName,
                tenant = tenant,
                landlord = landlord,
                entryInventory = entryInventory,
                exitInventory = exitInventory,
                initialSection = null,
                onImageClick = {},
                onInventoryClick = {},
                onAddOrEditReviewClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "RentalDetails - Content (Landlord)")
@Composable
private fun RentalDetailsContentLandlordPreview() {
    TrueStayTheme {
        val property = Property(
            id = "prop-1",
            name = "Appartement moderne 2 pièces",
            address = Address(
                street = "15 Rue de la Paix",
                city = "Paris",
                postalCode = "75001"
            ),
            description = "Bel appartement rénové, proche des commodités et transports.",
            monthlyRent = 1800,
            surface = 65,
            rooms = listOf(
                Room(name = "Chambre", type = RoomType.BEDROOM),
                Room(name = "Salle de bain", type = RoomType.BATHROOM)
            ),
            photos = listOf("https://picsum.photos/seed/11/600/400"),
            isInBuilding = true,
            isAvailable = true,
            status = PropertyStatus.PUBLISHED
        )

        val now = System.currentTimeMillis()
        val rental = Rental(
            id = "rental-1",
            propertyId = property.id,
            tenantId = "tenant-1",
            landlordId = "landlord-1",
            startDate = now - 30L * 24L * 60L * 60L * 1000L,
            endDate = now - 5L * 24L * 60L * 60L * 1000L,
            status = RentalStatus.ENDED
        )

        val tenant = User(
            id = "tenant-1",
            firstName = "Alice",
            lastName = "Durand",
            email = "alice@example.com",
            phoneNumber = "+33123456789"
        )

        val landlord = User(
            id = "landlord-1",
            firstName = "Bob",
            lastName = "Martin",
            email = "bob@example.com",
            phoneNumber = "+33987654321"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2000.dp)
        ) {
            RentalDetailsContent(
                property = property,
                rental = rental,
                review = null,
                isLandlord = true,
                tenantFirstName = tenant.firstName,
                tenant = tenant,
                landlord = landlord,
                entryInventory = null,
                exitInventory = null,
                initialSection = null,
                onImageClick = {},
                onInventoryClick = {},
                onAddOrEditReviewClick = {}
            )
        }
    }
}
