package ca.uqac.inf865.truestay.presentation.shared.property

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.text.format.DateFormat
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.Review
import ca.uqac.inf865.truestay.domain.model.PropertyReview
import ca.uqac.inf865.truestay.domain.model.BuildingReview
import ca.uqac.inf865.truestay.domain.model.NeighborhoodReview
import ca.uqac.inf865.truestay.domain.model.Room
import ca.uqac.inf865.truestay.domain.model.RoomType
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.presentation.common.components.BadgeVariant
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayBadge
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.SelectableButton
import ca.uqac.inf865.truestay.presentation.common.components.ImageCarousel
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.navigation.TrueStayTopAppBar
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import androidx.compose.ui.tooling.preview.Preview
import ca.uqac.inf865.truestay.presentation.common.components.FullscreenImageCarouselOverlay
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayRatingDisplay
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import ca.uqac.inf865.truestay.domain.model.PropertyStatus
import ca.uqac.inf865.truestay.presentation.common.components.PhotoGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailsScreen(
    propertyId: String,
    onBackClick: () -> Unit,
    onEditProperty: ((String) -> Unit)? = null,
    onCreateRental: ((String) -> Unit)? = null,
    viewModel: PropertyDetailsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showFullscreenCarousel by remember { mutableStateOf(false) }
    var fullscreenStartIndex by remember { mutableIntStateOf(0) }
    var fullscreenPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var fullscreenTitle by remember { mutableStateOf("") }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.favoriteMessageRes) {
        val msgRes = uiState.favoriteMessageRes
        if (msgRes != null) {
            snackbarHostState.showSnackbar(context.getString(msgRes))
            viewModel.clearFavoriteMessage()
        }
    }

    // Determine if current user is the landlord of this property
    val isLandlord = uiState.currentUser?.id == uiState.property?.landlordId

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TrueStayTopAppBar(
                    titleRes = R.string.screen_title_property_details,
                    onNavigateBack = onBackClick,
                    hasActions = true,
                    windowInsets = WindowInsets(0.dp),
                    actions = {
                        if (isLandlord) {
                            // Landlord actions: 3-dot menu
                            Box {
                                TrueStayIcon(
                                    iconRes = TrueStayIcons.EllipsisVertical,
                                    contentDescriptionRes = null,
                                    tint = LocalAppColors.current.grayDark,
                                    size = 24.dp,
                                    modifier = Modifier
                                        .padding(end = AppSpacing.large)
                                        .clickable { showDropdownMenu = true }
                                )

                                androidx.compose.material3.DropdownMenu(
                                    expanded = showDropdownMenu,
                                    onDismissRequest = { showDropdownMenu = false },
                                    modifier = Modifier
                                        .background(
                                            color = LocalAppColors.current.white,
                                            shape = AppShapes.medium
                                        )
                                        .padding(vertical = AppSpacing.xsmall),
                                    shape = AppShapes.medium,
                                    containerColor = LocalAppColors.current.white,
                                    tonalElevation = 2.dp,
                                    shadowElevation = 8.dp
                                ) {
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = stringResource(R.string.property_action_edit),
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = LocalAppColors.current.black
                                            )
                                        },
                                        onClick = {
                                            showDropdownMenu = false
                                            onEditProperty?.invoke(propertyId)
                                        },
                                        modifier = Modifier.padding(horizontal = AppSpacing.small),
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = LocalAppColors.current.black
                                        )
                                    )
                                    val canToggleStatus = uiState.property?.let { property ->
                                        property.isAvailable || property.status == PropertyStatus.PAUSED
                                    } == true
                                    if (canToggleStatus) {
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (uiState.property.status == PropertyStatus.PUBLISHED)
                                                        stringResource(R.string.property_action_pause)
                                                    else
                                                        stringResource(R.string.property_action_publish),
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    color = LocalAppColors.current.black
                                                )
                                            },
                                            onClick = {
                                                showDropdownMenu = false
                                                viewModel.togglePropertyStatus {
                                                    // Success callback
                                                }
                                            },
                                            modifier = Modifier.padding(horizontal = AppSpacing.small),
                                            colors = androidx.compose.material3.MenuDefaults.itemColors(
                                                textColor = LocalAppColors.current.black
                                            )
                                        )
                                    }
                                    val canDelete = uiState.property?.isAvailable == true
                                    if (canDelete) {
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.property_action_delete),
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    color = LocalAppColors.current.error
                                                )
                                            },
                                            onClick = {
                                                showDropdownMenu = false
                                                showDropdownMenu = false
                                                showDeleteDialog = true
                                            },
                                            modifier = Modifier.padding(horizontal = AppSpacing.small),
                                            colors = androidx.compose.material3.MenuDefaults.itemColors(
                                                textColor = LocalAppColors.current.error
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            // Tenant actions: favorite button
                            TrueStayIcon(
                                iconRes = if (uiState.isFavorite) TrueStayIcons.HeartFilled else TrueStayIcons.Heart,
                                contentDescriptionRes = null,
                                tint = if (uiState.isFavorite)
                                    LocalAppColors.current.error
                                else
                                    LocalAppColors.current.grayDark,
                                size = 24.dp,
                                modifier = Modifier
                                    .padding(end = AppSpacing.large)
                                    .clickable { viewModel.toggleFavorite() }
                            )
                        }
                    }
                )
            }
        ) { padding ->
            // Blocking loader / error follow the same pattern as FavoritesScreen.
            val blockingErrorRes = if (uiState.error != null && uiState.property == null) {
                R.string.property_details_error_loading
            } else null

            val showBlockingLoader = uiState.isLoading && uiState.property == null && blockingErrorRes == null

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                when {
                    showBlockingLoader -> {
                        CircularProgressIndicator(color = LocalAppColors.current.primary)
                    }

                    blockingErrorRes != null -> {
                        PropertyDetailsErrorState(
                            messageRes = blockingErrorRes,
                            onRetry = viewModel::refresh
                        )
                    }

                    uiState.property != null -> {
                        PropertyDetailsContent(
                            property = uiState.property,
                            reviews = uiState.reviews,
                            selectedReviewFilter = uiState.selectedReviewFilter,
                            onReviewFilterChange = viewModel::setReviewFilter,
                            onImageClick = { index ->
                                fullscreenPhotos = uiState.property.photos
                                fullscreenTitle = uiState.property.name
                                fullscreenStartIndex = index
                                showFullscreenCarousel = true
                            },
                            onReviewPhotoClick = { photos, index ->
                                fullscreenPhotos = photos
                                fullscreenTitle = uiState.property.name
                                fullscreenStartIndex = index
                                showFullscreenCarousel = true
                            },
                            landlordPhone = uiState.landlord?.phoneNumber,
                            isLandlord = isLandlord,
                            onCreateRental = onCreateRental
                        )
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(text = stringResource(R.string.properties_delete_dialog_title))
                },
                text = {
                    Text(text = stringResource(R.string.properties_delete_dialog_message))
                },
                confirmButton = {
                    TrueStayButton(
                        text = stringResource(R.string.properties_delete_dialog_confirm),
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteProperty {
                                onBackClick()
                            }
                        },
                        variant = ButtonVariant.DANGER
                    )
                },
                dismissButton = {
                    TrueStayButton(
                        text = stringResource(R.string.properties_delete_dialog_cancel),
                        onClick = { showDeleteDialog = false },
                        variant = ButtonVariant.SECONDARY
                    )
                },
                shape = AppShapes.large,
                containerColor = LocalAppColors.current.white
            )
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

@Composable
private fun PropertyDetailsErrorState(
    messageRes: Int,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(AppSpacing.large)
    ) {
        Text(
            text = stringResource(id = messageRes),
            style = MaterialTheme.typography.titleMedium,
            color = LocalAppColors.current.error,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(AppSpacing.large))
        TrueStayButton(
            text = stringResource(R.string.common_retry),
            onClick = onRetry,
            variant = ButtonVariant.SECONDARY
        )
    }
}

@Composable
private fun PropertyDetailsContent(
    property: Property,
    reviews: List<ReviewWithUser>,
    selectedReviewFilter: ReviewFilterType,
    onReviewFilterChange: (ReviewFilterType) -> Unit,
    modifier: Modifier = Modifier,
    onImageClick: ((Int) -> Unit)? = null,
    onReviewPhotoClick: ((List<String>, Int) -> Unit)? = null,
    landlordPhone: String? = null,
    isLandlord: Boolean = false,
    onCreateRental: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = AppSpacing.large)
        ) {
            // Main image with availability badge
            item {
                PropertyImageSection(
                    property = property,
                    onImageClick = onImageClick
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .background(LocalAppColors.current.white)
                        .padding(AppSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
                ) {
                    // Title and address
                    PropertyHeaderSection(property = property)

                    // Features
                    PropertyFeaturesSection(property = property)

                    if (!isLandlord) {
                        // Contact button (opens chooser to call or send SMS to landlord, if phone available)
                        val phone = landlordPhone?.takeIf { it.isNotBlank() }
                        if (phone != null) {
                            TrueStayButton(
                                text = stringResource(R.string.property_details_contact_button),
                                onClick = {
                                    val phoneUri = "tel:$phone".toUri()
                                    val callIntent = Intent(Intent.ACTION_DIAL, phoneUri)

                                    val smsUri = "smsto:$phone".toUri()
                                    val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri)

                                    val chooser = Intent.createChooser(
                                        smsIntent,
                                        context.getString(R.string.property_details_contact_chooser_title)
                                    )
                                    chooser.putExtra(
                                        Intent.EXTRA_INITIAL_INTENTS,
                                        arrayOf(callIntent)
                                    )

                                    context.startActivity(chooser)
                                },
                                variant = ButtonVariant.PRIMARY,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = TrueStayIcons.Phone
                            )
                        }
                    } else if (onCreateRental != null) {
                        TrueStayButton(
                            text = stringResource(R.string.property_details_create_location),
                            onClick = { onCreateRental(property.id) },
                            variant = ButtonVariant.PRIMARY,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Description
                    if (property.description.isNotBlank()) {
                        DescriptionSection(description = property.description)
                    }
                }
            }

            item {
                ReviewsListHeader(
                    property = property,
                    reviews = reviews,
                    selectedFilter = selectedReviewFilter,
                    onFilterChange = onReviewFilterChange
                )
            }

            // One lazy item per review for performance (no spacer after last visible item)
            val filteredReviews = when (selectedReviewFilter) {
                ReviewFilterType.PROPERTY -> reviews.filter { it.review.propertyReview != null }
                ReviewFilterType.BUILDING -> reviews.filter { it.review.buildingReview != null }
                ReviewFilterType.NEIGHBORHOOD -> reviews.filter { it.review.neighborhoodReview != null }
            }

            items(filteredReviews.size) { index ->
                val reviewWithUser = filteredReviews[index]
                val isLast = index == filteredReviews.lastIndex

                when (selectedReviewFilter) {
                    ReviewFilterType.PROPERTY -> {
                        val propertyReview = reviewWithUser.review.propertyReview!!
                        UserReviewCard(
                            user = reviewWithUser.user,
                            review = reviewWithUser.review,
                            comment = propertyReview.comment,
                            rating = propertyReview.overallRating,
                            photos = propertyReview.photos,
                            selectedReviewFilter = selectedReviewFilter,
                            onPhotoClick = { i -> onReviewPhotoClick?.invoke(propertyReview.photos, i) }
                        )
                        if (!isLast) Spacer(modifier = Modifier.height(AppSpacing.small))
                    }
                    ReviewFilterType.BUILDING -> {
                        val buildingReview = reviewWithUser.review.buildingReview!!
                        UserReviewCard(
                            user = reviewWithUser.user,
                            review = reviewWithUser.review,
                            comment = buildingReview.comment,
                            rating = buildingReview.overallRating,
                            photos = buildingReview.photos,
                            selectedReviewFilter = selectedReviewFilter,
                            onPhotoClick = { i -> onReviewPhotoClick?.invoke(buildingReview.photos, i) }
                        )
                        if (!isLast) Spacer(modifier = Modifier.height(AppSpacing.small))
                    }
                    ReviewFilterType.NEIGHBORHOOD -> {
                        val neighborhoodReview = reviewWithUser.review.neighborhoodReview!!
                        UserReviewCard(
                            user = reviewWithUser.user,
                            review = reviewWithUser.review,
                            comment = neighborhoodReview.comment,
                            rating = neighborhoodReview.overallRating,
                            photos = neighborhoodReview.photos,
                            selectedReviewFilter = selectedReviewFilter,
                            onPhotoClick = { i -> onReviewPhotoClick?.invoke(neighborhoodReview.photos, i) }
                        )
                        if (!isLast) Spacer(modifier = Modifier.height(AppSpacing.small))
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyImageSection(
    property: Property,
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

        // Availability badge
        val availabilityText = when {
            property.status == PropertyStatus.PAUSED ->
                stringResource(R.string.property_paused)
            !property.isAvailable ->
                stringResource(R.string.property_rented)
            property.isAvailable ->
                stringResource(R.string.property_available)
            else ->
                stringResource(R.string.property_unavailable)
        }
        val availabilityVariant = when {
            property.status == PropertyStatus.PAUSED ->
                BadgeVariant.WARNING  // Orange badge for paused properties
            property.isAvailable ->
                BadgeVariant.SUCCESS  // Green badge for available properties
            else ->
                BadgeVariant.ERROR  // Red badge for unavailable properties
        }

        TrueStayBadge(
            text = availabilityText,
            variant = availabilityVariant,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(AppSpacing.medium)
        )

        // Price badge
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
fun PropertyHeaderSection(property: Property) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        Text(
            text = property.name,
            style = MaterialTheme.typography.headlineMedium,
            color = LocalAppColors.current.black
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrueStayIcon(
                iconRes = TrueStayIcons.MapPin,
                contentDescriptionRes = null,
                tint = LocalAppColors.current.grayDark,
                size = 16.dp
            )
            Text(
                text = "${property.address.street}, ${property.address.city}",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark
            )
        }
    }
}

@Composable
fun PropertyFeaturesSection(property: Property) {
    val bedroomCount = property.rooms.count { it.type == RoomType.BEDROOM }
    val bathroomCount = property.rooms.count { it.type == RoomType.BATHROOM }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        FeatureItem(
            icon = TrueStayIcons.Bed,
            text = pluralStringResource(R.plurals.property_details_bedrooms_count, bedroomCount, bedroomCount)
        )
        FeatureItem(
            icon = TrueStayIcons.Bath,
            text = pluralStringResource(R.plurals.property_details_bathrooms_count, bathroomCount, bathroomCount)
        )
        FeatureItem(
            icon = TrueStayIcons.Square,
            text = "${property.surface}m²"
        )
    }
}

@Composable
private fun FeatureItem(
    icon: Int,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrueStayIcon(
            iconRes = icon,
            contentDescriptionRes = null,
            tint = LocalAppColors.current.grayDark,
            size = 16.dp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalAppColors.current.grayDark
        )
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        Text(
            text = stringResource(R.string.property_details_description_title),
            style = MaterialTheme.typography.headlineSmall,
            color = LocalAppColors.current.black
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalAppColors.current.grayDark
        )
    }
}

@Composable
private fun ReviewsListHeader(
    property: Property,
    reviews: List<ReviewWithUser>,
    selectedFilter: ReviewFilterType,
    onFilterChange: (ReviewFilterType) -> Unit
) {
    val hasPropertyReviews = reviews.any { it.review.propertyReview != null }
    val hasBuildingReviews = reviews.any { it.review.buildingReview != null }
    val hasNeighborhoodReviews = reviews.any { it.review.neighborhoodReview != null }

    Column(modifier = Modifier.padding(AppSpacing.large)) {
        Text(
            text = stringResource(R.string.property_details_reviews_title),
            style = MaterialTheme.typography.headlineSmall,
            color = LocalAppColors.current.black
        )

        Spacer(modifier = Modifier.height(AppSpacing.medium))

        // Filter buttons with ratings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
        ) {
            FilterButton(
                text = stringResource(R.string.property_details_filter_property),
                rating = property.ratings.propertyAverageRating,
                isSelected = selectedFilter == ReviewFilterType.PROPERTY,
                onClick = { onFilterChange(ReviewFilterType.PROPERTY) },
                modifier = Modifier.weight(1f)
            )
            if (property.isInBuilding) {
                FilterButton(
                    text = stringResource(R.string.property_details_filter_building),
                    rating = property.ratings.buildingAverageRating,
                    isSelected = selectedFilter == ReviewFilterType.BUILDING,
                    onClick = { onFilterChange(ReviewFilterType.BUILDING) },
                    modifier = Modifier.weight(1f)
                )
            }
            FilterButton(
                text = stringResource(R.string.property_details_filter_neighborhood),
                rating = property.ratings.neighborhoodAverageRating,
                isSelected = selectedFilter == ReviewFilterType.NEIGHBORHOOD,
                onClick = { onFilterChange(ReviewFilterType.NEIGHBORHOOD) },
                modifier = Modifier.weight(1f)
            )
        }

        val emptyMessageRes = when (selectedFilter) {
            ReviewFilterType.PROPERTY ->
                if (!hasPropertyReviews) R.string.property_details_no_property_reviews else null
            ReviewFilterType.BUILDING ->
                if (!hasBuildingReviews && property.isInBuilding) R.string.property_details_no_building_reviews else null
            ReviewFilterType.NEIGHBORHOOD ->
                if (!hasNeighborhoodReviews) R.string.property_details_no_neighborhood_reviews else null
        }

        emptyMessageRes?.let { resId ->
            Text(
                text = stringResource(resId),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark,
                modifier = Modifier.padding(top = AppSpacing.large)
            )
        }
    }
}

@Composable
private fun FilterButton(
    text: String,
    rating: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SelectableButton(
        selected = isSelected,
        onClick = onClick,
        modifier = modifier.height(43.dp),
        centerContent = true,
        verticalPadding = AppSpacing.xsmall
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
            )

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
                if (rating > 0) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", rating),
                        style = MaterialTheme.typography.titleSmall,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.property_details_no_ratings),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun UserReviewCard(
    user: User?,
    review: Review,
    comment: String,
    rating: Float,
    photos: List<String>,
    selectedReviewFilter: ReviewFilterType? = null,
    onPhotoClick: ((Int) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    val colors = LocalAppColors.current

    TrueStayCard(modifier = Modifier.padding(horizontal=AppSpacing.large)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // Header with user info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture
                if (user?.profilePictureUrl?.isNotEmpty() == true) {
                    AsyncImage(
                        model = user.profilePictureUrl,
                        contentDescription = user.firstName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.img_placeholder),
                        error = painterResource(R.drawable.img_placeholder)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LocalAppColors.current.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.firstName?.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.headlineSmall,
                            color = LocalAppColors.current.white
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
                ) {
                    Text(
                        text = user?.let { "${it.firstName} ${it.lastName.firstOrNull()}." } ?: stringResource(R.string.property_details_anonymous),
                        style = MaterialTheme.typography.titleMedium,
                        color = LocalAppColors.current.black
                    )
                    Text(
                        text = formatDate(review.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalAppColors.current.grayDark
                    )
                }

                // Stars
                TrueStayRatingDisplay(rating = rating)

                // Expand button
                if (selectedReviewFilter != null) {
                    TrueStayIcon(
                        iconRes = if (isExpanded) TrueStayIcons.ChevronUp else TrueStayIcons.ChevronDown,
                        contentDescriptionRes = if (isExpanded) R.string.property_details_collapse_review else R.string.property_details_expand_review,
                        tint = colors.grayDark,
                        modifier = Modifier
                            .clickable { isExpanded = !isExpanded }
                            .padding(start = AppSpacing.small)
                    )
                }
            }

            // Expanded details
            AnimatedVisibility(visible = isExpanded && selectedReviewFilter != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.grayLight.copy(alpha = 0.3f), shape = AppShapes.small)
                        .padding(AppSpacing.medium)
                ) {
                    when (selectedReviewFilter) {
                        ReviewFilterType.PROPERTY -> {
                            review.propertyReview?.let {
                                RatingRow(stringResource(R.string.review_form_property_general_condition), it.generalCondition)
                                RatingRow(stringResource(R.string.review_form_property_comfort), it.comfort)
                                RatingRow(stringResource(R.string.review_form_property_compliance), it.compliance)
                                RatingRow(stringResource(R.string.review_form_property_value_for_money), it.valueForMoney)
                            }
                        }
                        ReviewFilterType.BUILDING -> {
                            review.buildingReview?.let {
                                RatingRow(stringResource(R.string.review_form_building_maintenance), it.maintenance)
                                RatingRow(stringResource(R.string.review_form_building_neighborhood), it.neighborhood)
                                RatingRow(stringResource(R.string.review_form_building_security), it.security)
                                RatingRow(stringResource(R.string.review_form_building_services), it.services)
                            }
                        }
                        ReviewFilterType.NEIGHBORHOOD -> {
                            review.neighborhoodReview?.let {
                                RatingRow(stringResource(R.string.review_form_neighborhood_transport), it.transport)
                                RatingRow(stringResource(R.string.review_form_neighborhood_amenities), it.amenities)
                                RatingRow(stringResource(R.string.review_form_neighborhood_calm), it.calm)
                                RatingRow(stringResource(R.string.review_form_neighborhood_safety), it.safety)
                                RatingRow(stringResource(R.string.review_form_neighborhood_atmosphere), it.atmosphere)
                            }
                        }
                        null -> {}
                    }
                }
            }

            // Comment
            if (comment.isNotEmpty()) {
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.grayDark
                )
            }

            // Photos
            if (photos.isNotEmpty()) {
                PhotoGrid(
                    photos = photos,
                    onPhotoClick = onPhotoClick ?: { _ -> /* No-op if not provided */ },
                    onDeletePhoto = null // Read-only in this view
                )
            }
        }
    }
}

@Composable
private fun RatingRow(label: String, rating: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = LocalAppColors.current.grayDark
        )
        TrueStayRatingDisplay(
            rating = rating.toFloat(),
            starSize = 14.dp
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMdd")
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true, name = "PropertyDetails - Loading")
@Composable
private fun PropertyDetailsLoadingPreview() {
    TrueStayTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = LocalAppColors.current.primary)
        }
    }
}

@Preview(showBackground = true, name = "PropertyDetails - Error")
@Composable
private fun PropertyDetailsErrorPreview() {
    TrueStayTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PropertyDetailsErrorState(
                messageRes = R.string.property_details_error_loading,
                onRetry = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "PropertyDetails - With Rental & Reviews")
@Composable
private fun PropertyDetailsContentPreview() {
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
            rooms = listOf(
                Room(name = "Chambre", type = RoomType.BEDROOM),
                Room(name = "Salle de bain", type = RoomType.BATHROOM)
            ),
            photos = listOf("https://picsum.photos/seed/11/600/400"),
            isAvailable = true,
        )
        val reviews = listOf(
            ReviewWithUser(
                review = Review(
                    propertyId = property.id,
                    tenantId = "tenant-2",
                    propertyReview = PropertyReview(
                        generalCondition = 4,
                        comfort = 5,
                        compliance = 4,
                        valueForMoney = 4,
                        overallRating = 4.25f,
                        comment = "Très bon logement, calme et bien situé.",
                        photos = listOf(
                            "https://picsum.photos/seed/11/600/400",
                            "https://picsum.photos/seed/11/600/400",
                            "https://picsum.photos/seed/11/600/400",
                            "https://picsum.photos/seed/11/600/400",
                            "https://picsum.photos/seed/11/600/400"
                        )
                    )
                ),
                user = User(id = "tenant-1", email = "john@example.com")
            ),
            ReviewWithUser(
                review = Review(
                    propertyId = property.id,
                    tenantId = "tenant-1",
                    propertyReview = PropertyReview(
                        generalCondition = 4,
                        comfort = 5,
                        compliance = 4,
                        valueForMoney = 4,
                        overallRating = 4.25f,
                        comment = "Très bon logement, calme et bien situé."
                    )
                ),
                user = User(id = "tenant-1", email = "john@example.com")
            ),
            ReviewWithUser(
                review = Review(
                    propertyId = property.id,
                    tenantId = "tenant-2",
                    buildingReview = BuildingReview(
                        maintenance = 4,
                        neighborhood = 4,
                        security = 5,
                        services = 4,
                        overallRating = 4.25f,
                        comment = "Immeuble bien entretenu"
                    ),
                    neighborhoodReview = NeighborhoodReview(
                        transport = 5,
                        amenities = 4,
                        calm = 4,
                        safety = 4,
                        atmosphere = 4,
                        overallRating = 4.2f,
                        comment = "Quartier agréable"
                    )
                ),
                user = User(id = "tenant-2", email = "jane@example.com")
            )
        )

        PropertyDetailsContent(
            property = property,
            reviews = reviews,
            selectedReviewFilter = ReviewFilterType.PROPERTY,
            onReviewFilterChange = {},
            onImageClick = {},
            onReviewPhotoClick = { _, _ -> },
            landlordPhone = "+33123456789"
        )
    }
}

@Preview(showBackground = true, name = "PropertyDetails - No Rental, No Reviews")
@Composable
private fun PropertyDetailsContentEmptyPreview() {
    TrueStayTheme {
        val property = Property(
            id = "prop-2",
            name = "Studio lumineux centre-ville",
            address = Address(
                street = "42 Avenue des Champs",
                city = "Lyon",
                postalCode = "69001"
            ),
            description = "Studio fonctionnel, idéal étudiant.",
            monthlyRent = 900,
            rooms = listOf(
                Room(name = "Studio", type = RoomType.LIVING_ROOM)
            ),
            photos = emptyList(),
            isAvailable = false
        )

        PropertyDetailsContent(
            property = property,
            reviews = emptyList(),
            selectedReviewFilter = ReviewFilterType.PROPERTY,
            onReviewFilterChange = {},
            onImageClick = {},
            onReviewPhotoClick = { _, _ -> },
            landlordPhone = null
        )
    }
}
