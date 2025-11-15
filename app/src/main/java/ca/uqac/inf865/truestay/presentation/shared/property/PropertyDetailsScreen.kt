package ca.uqac.inf865.truestay.presentation.shared.property

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.Rental
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
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.navigation.TrueStayTopAppBar
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import androidx.compose.ui.tooling.preview.Preview
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailsScreen(
    @Suppress("UNUSED_PARAMETER") propertyId: String,
    onBackClick: () -> Unit,
    onAddReviewClick: () -> Unit = {},
    viewModel: PropertyDetailsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TrueStayTopAppBar(
                titleRes = R.string.screen_title_property_details,
                onNavigateBack = onBackClick,
                hasActions = true,
                windowInsets = WindowInsets(0.dp),
                actions = {
                    // Favorite button
                    TrueStayIcon(
                        iconRes = if (uiState.isFavorite) TrueStayIcons.HeartFilled else TrueStayIcons.Heart,
                        contentDescriptionRes = null,
                        tint = if (uiState.isFavorite)
                            LocalAppColors.current.error
                        else
                            LocalAppColors.current.grayDark,
                        size = 24.dp,
                        modifier = Modifier.clickable { viewModel.toggleFavorite() }
                    )
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LocalAppColors.current.primary)
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalAppColors.current.error
                    )
                }
            }
            uiState.property != null -> {
                PropertyDetailsContent(
                    property = uiState.property,
                    rental = uiState.rental,
                    reviews = uiState.reviews,
                    selectedReviewFilter = uiState.selectedReviewFilter,
                    onReviewFilterChange = viewModel::setReviewFilter,
                    onAddReviewClick = onAddReviewClick,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun PropertyDetailsContent(
    property: Property,
    rental: Rental?,
    reviews: List<ReviewWithUser>,
    selectedReviewFilter: ReviewFilterType,
    onReviewFilterChange: (ReviewFilterType) -> Unit,
    onAddReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        // Main image with availability badge
        PropertyImageSection(property = property)

        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // Title and address
            PropertyHeaderSection(property = property)

            // Features
            PropertyFeaturesSection(property = property)

            // Contact button
            TrueStayButton(
                text = stringResource(R.string.property_details_contact_button),
                onClick = { /* TODO: Implement contact */ },
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            if (property.description.isNotBlank()) {
                DescriptionSection(description = property.description)
            }

            // Reviews and ratings
            if (reviews.isNotEmpty()) {
                ReviewsListSection(
                    reviews = reviews,
                    selectedFilter = selectedReviewFilter,
                    onFilterChange = onReviewFilterChange
                )
            }

            // Add review button (only if active rental)
            if (rental != null) {
                TrueStayButton(
                    text = stringResource(R.string.property_details_add_review_button),
                    onClick = onAddReviewClick,
                    variant = ButtonVariant.PRIMARY
                )
            }
        }
    }
}

@Composable
private fun PropertyImageSection(property: Property) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(LocalAppColors.current.grayLight)
    ) {
        if (property.photos.isNotEmpty()) {
            AsyncImage(
                model = property.photos.first(),
                contentDescription = property.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.img_placeholder),
                contentDescription = stringResource(R.string.property_image_placeholder),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Availability badge
        TrueStayBadge(
            text = if (property.isAvailable) {
                stringResource(R.string.property_available)
            } else {
                stringResource(R.string.property_unavailable)
            },
            variant = if (property.isAvailable) BadgeVariant.SUCCESS else BadgeVariant.ERROR,
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
private fun PropertyHeaderSection(property: Property) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        Text(
            text = property.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
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
private fun PropertyFeaturesSection(property: Property) {
    val bedroomCount = property.rooms.count { it.type == RoomType.BEDROOM }
    val bathroomCount = property.rooms.count { it.type == RoomType.BATHROOM }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        FeatureItem(
            icon = TrueStayIcons.House,
            text = stringResource(R.string.property_details_bedrooms_count, bedroomCount)
        )
        FeatureItem(
            icon = TrueStayIcons.Bath,
            text = stringResource(R.string.property_details_bathrooms_count, bathroomCount)
        )
        FeatureItem(
            icon = TrueStayIcons.Square,
            text = "${property.surface} m²"
        )
    }
}

@Composable
private fun FeatureItem(
    icon: Int,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrueStayIcon(
            iconRes = icon,
            contentDescriptionRes = null,
            tint = LocalAppColors.current.grayDark,
            size = 20.dp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalAppColors.current.black
        )
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Text(
            text = stringResource(R.string.property_details_description_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
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
private fun ReviewsListSection(
    reviews: List<ReviewWithUser>,
    selectedFilter: ReviewFilterType,
    onFilterChange: (ReviewFilterType) -> Unit
) {
    // Calculate actual averages from reviews
    val propertyAverageRating = reviews
        .mapNotNull { it.review.propertyReview?.overallRating }
        .takeIf { it.isNotEmpty() }
        ?.average()?.toFloat() ?: 0f

    val buildingAverageRating = reviews
        .mapNotNull { it.review.buildingReview }
        .takeIf { it.isNotEmpty() }
        ?.map { (it.maintenance + it.neighborhood + it.security + it.services) / 4f }
        ?.average()?.toFloat() ?: 0f

    val neighborhoodAverageRating = reviews
        .mapNotNull { it.review.neighborhoodReview }
        .takeIf { it.isNotEmpty() }
        ?.map { (it.transport + it.amenities + it.calm + it.safety + it.atmosphere) / 5f }
        ?.average()?.toFloat() ?: 0f

    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Text(
            text = stringResource(R.string.property_details_reviews_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = LocalAppColors.current.black
        )

        // Filter buttons with ratings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            FilterButton(
                text = stringResource(R.string.property_details_filter_property),
                rating = propertyAverageRating,
                isSelected = selectedFilter == ReviewFilterType.PROPERTY,
                onClick = { onFilterChange(ReviewFilterType.PROPERTY) },
                modifier = Modifier.weight(1f)
            )
            FilterButton(
                text = stringResource(R.string.property_details_filter_building),
                rating = buildingAverageRating,
                isSelected = selectedFilter == ReviewFilterType.BUILDING,
                onClick = { onFilterChange(ReviewFilterType.BUILDING) },
                modifier = Modifier.weight(1f)
            )
            FilterButton(
                text = stringResource(R.string.property_details_filter_neighborhood),
                rating = neighborhoodAverageRating,
                isSelected = selectedFilter == ReviewFilterType.NEIGHBORHOOD,
                onClick = { onFilterChange(ReviewFilterType.NEIGHBORHOOD) },
                modifier = Modifier.weight(1f)
            )
        }

        // Filtered user reviews list
        reviews.forEach { reviewWithUser ->
            when (selectedFilter) {
                ReviewFilterType.PROPERTY -> {
                    reviewWithUser.review.propertyReview?.let { propertyReview ->
                        UserReviewCard(
                            user = reviewWithUser.user,
                            review = reviewWithUser.review,
                            comment = propertyReview.comment,
                            rating = propertyReview.overallRating,
                            photos = propertyReview.photos
                        )
                    }
                }
                ReviewFilterType.BUILDING -> {
                    reviewWithUser.review.buildingReview?.let { buildingReview ->
                        UserReviewCard(
                            user = reviewWithUser.user,
                            review = reviewWithUser.review,
                            comment = buildingReview.comment,
                            rating = (buildingReview.maintenance + buildingReview.neighborhood +
                                     buildingReview.security + buildingReview.services) / 4f,
                            photos = buildingReview.photos
                        )
                    }
                }
                ReviewFilterType.NEIGHBORHOOD -> {
                    reviewWithUser.review.neighborhoodReview?.let { neighborhoodReview ->
                        UserReviewCard(
                            user = reviewWithUser.user,
                            review = reviewWithUser.review,
                            comment = neighborhoodReview.comment,
                            rating = (neighborhoodReview.transport + neighborhoodReview.amenities +
                                     neighborhoodReview.calm + neighborhoodReview.safety +
                                     neighborhoodReview.atmosphere) / 5f,
                            photos = neighborhoodReview.photos
                        )
                    }
                }
            }
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
        modifier = modifier.height(56.dp),
        centerContent = true
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal
            )

            if (rating > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrueStayIcon(
                        iconRes = TrueStayIcons.StarFilled,
                        contentDescriptionRes = null,
                        tint = LocalAppColors.current.warning,
                        size = 16.dp
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", rating),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
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
    photos: List<String>
) {
    TrueStayCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            // Header with user info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture
                if (user?.profilePictureUrl?.isNotEmpty() == true) {
                    AsyncImage(
                        model = user.profilePictureUrl,
                        contentDescription = user.firstName,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.img_placeholder),
                        error = painterResource(R.drawable.img_placeholder)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LocalAppColors.current.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.firstName?.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LocalAppColors.current.white
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
                ) {
                    Text(
                        text = user?.let { "${it.firstName} ${it.lastName.firstOrNull()}." } ?: "Utilisateur",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.black
                    )
                    Text(
                        text = formatDate(review.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalAppColors.current.grayDark
                    )
                }
            }

            // Stars
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
            ) {
                repeat(5) { index ->
                    TrueStayIcon(
                        iconRes = if (index < rating.toInt())
                            TrueStayIcons.StarFilled
                        else
                            TrueStayIcons.Star,
                        contentDescriptionRes = null,
                        tint = LocalAppColors.current.warning,
                        size = 20.dp
                    )
                }
            }

            // Comment
            if (comment.isNotEmpty()) {
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.black
                )
            }

            // Photos
            if (photos.isNotEmpty()) {
                AsyncImage(
                    model = photos.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.img_placeholder),
                    error = painterResource(R.drawable.img_placeholder)
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
    return formatter.format(date)
}

// ==========================================
// Previews
// ==========================================
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
            isAvailable = true
        )
        val rental = Rental(
            id = "rent-1",
            propertyId = property.id,
            tenantId = "tenant-1",
            landlordId = "landlord-1",
            startDate = System.currentTimeMillis() - 10L * 24L * 60L * 60L * 1000L,
            endDate = System.currentTimeMillis() + 20L * 24L * 60L * 60L * 1000L
        )
        val reviews = listOf(
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
            rental = rental,
            reviews = reviews,
            selectedReviewFilter = ReviewFilterType.PROPERTY,
            onReviewFilterChange = {},
            onAddReviewClick = {}
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
            rental = null,
            reviews = emptyList(),
            selectedReviewFilter = ReviewFilterType.PROPERTY,
            onReviewFilterChange = {},
            onAddReviewClick = {}
        )
    }
}
