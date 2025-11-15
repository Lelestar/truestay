package ca.uqac.inf865.truestay.presentation.shared.property

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.Rental
import ca.uqac.inf865.truestay.domain.model.Review
import ca.uqac.inf865.truestay.domain.model.RoomType
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.presentation.common.components.BadgeVariant
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayBadge
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.navigation.TrueStayTopAppBar
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
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
        topBar = {
            TrueStayTopAppBar(
                titleRes = R.string.screen_title_property_details,
                onNavigateBack = onBackClick,
                hasActions = true,
                actions = {
                    // Bouton favoris
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
        // Image principale avec badge disponibilité
        PropertyImageSection(property = property)

        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // Titre et adresse
            PropertyHeaderSection(property = property)

            // Caractéristiques
            PropertyFeaturesSection(property = property)

            // Bouton Contacter
            TrueStayButton(
                text = stringResource(R.string.property_details_contact_button),
                onClick = { /* TODO: Implémenter contact */ },
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            if (property.description.isNotBlank()) {
                DescriptionSection(description = property.description)
            }

            // Avis et notes
            if (reviews.isNotEmpty()) {
                ReviewsListSection(
                    property = property,
                    reviews = reviews,
                    selectedFilter = selectedReviewFilter,
                    onFilterChange = onReviewFilterChange
                )
            }

            // Déroulement du bail (si location existe)
            if (rental != null) {
                LeaseTimelineSection(rental = rental)
            }

            // État des lieux (si location existe)
            if (rental != null) {
                InventorySection(rental = rental)
            }


            // Bouton Ajouter un avis (seulement si location active)
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

        // Badge disponibilité
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

        // Badge prix
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
    val bedCount = property.rooms.filter { it.type == RoomType.BEDROOM }.sumOf {
        it.elements.count { element -> element.type.name.contains("BED", ignoreCase = true) }
    }.let { if (it == 0) bedroomCount * 2 else it } // Estimation si pas de données

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        FeatureItem(
            icon = TrueStayIcons.House,
            text = stringResource(R.string.property_details_bedrooms_count, bedroomCount)
        )
        FeatureItem(
            icon = TrueStayIcons.Bed,
            text = stringResource(R.string.property_details_beds_count, bedCount)
        )
        FeatureItem(
            icon = TrueStayIcons.Bath,
            text = stringResource(R.string.property_details_bathrooms_count, bathroomCount)
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
private fun LeaseTimelineSection(rental: Rental) {
    TrueStayCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Text(
                text = stringResource(R.string.property_details_lease_timeline),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.black
            )

            // EDL d'entrée
            TimelineItem(
                icon = TrueStayIcons.ClipboardCheck,
                title = stringResource(R.string.property_details_entry_inventory),
                date = formatDate(rental.startDate),
                status = stringResource(R.string.property_details_status_ready),
                statusVariant = BadgeVariant.SUCCESS
            )

            HorizontalDivider(color = LocalAppColors.current.grayBorder)

            // Bail en cours
            TimelineItem(
                icon = TrueStayIcons.Calendar,
                title = stringResource(R.string.property_details_active_lease),
                date = stringResource(
                    R.string.property_details_date_range,
                    formatDate(rental.startDate),
                    formatDate(rental.endDate)
                ),
                status = null,
                statusVariant = null
            )

            HorizontalDivider(color = LocalAppColors.current.grayBorder)

            // EDL de sortie
            TimelineItem(
                icon = TrueStayIcons.ClipboardCheck,
                title = stringResource(R.string.property_details_exit_inventory),
                date = formatDate(rental.endDate),
                status = stringResource(R.string.property_details_status_in_progress),
                statusVariant = BadgeVariant.WARNING
            )
        }
    }
}

@Composable
private fun TimelineItem(
    icon: Int,
    title: String,
    date: String,
    status: String?,
    statusVariant: BadgeVariant?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
        verticalAlignment = Alignment.Top
    ) {
        TrueStayIcon(
            iconRes = icon,
            contentDescriptionRes = null,
            tint = LocalAppColors.current.success,
            size = 24.dp
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = LocalAppColors.current.black
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = LocalAppColors.current.grayDark
            )
        }

        if (status != null && statusVariant != null) {
            TrueStayBadge(
                text = status,
                variant = statusVariant
            )
        }
    }
}

@Composable
private fun InventorySection(@Suppress("UNUSED_PARAMETER") rental: Rental) {
    TrueStayCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Text(
                text = stringResource(R.string.property_details_inventory_section),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.black
            )

            // EDL d'entrée
            InventoryItem(
                title = stringResource(R.string.property_details_entry_inventory),
                status = stringResource(R.string.property_details_status_ready),
                statusVariant = BadgeVariant.SUCCESS
            )

            HorizontalDivider(color = LocalAppColors.current.grayBorder)

            // EDL de sortie
            InventoryItem(
                title = stringResource(R.string.property_details_exit_inventory),
                status = stringResource(R.string.property_details_status_in_progress),
                statusVariant = BadgeVariant.WARNING
            )
        }
    }
}

@Composable
private fun InventoryItem(
    title: String,
    status: String,
    statusVariant: BadgeVariant
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrueStayIcon(
                iconRes = TrueStayIcons.FileText,
                contentDescriptionRes = null,
                tint = LocalAppColors.current.primary,
                size = 20.dp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.black
            )
        }

        TrueStayBadge(
            text = status,
            variant = statusVariant
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
    property: Property,
    reviews: List<ReviewWithUser>,
    selectedFilter: ReviewFilterType,
    onFilterChange: (ReviewFilterType) -> Unit
) {
    // Calculer les vraies moyennes à partir des avis
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

        // Boutons de filtre avec notes
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

        // Liste des avis utilisateurs filtrés
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
    Box(
        modifier = modifier
            .height(56.dp)
            .background(
                color = LocalAppColors.current.black,
                shape = MaterialTheme.shapes.small
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) LocalAppColors.current.primary else LocalAppColors.current.black,
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(AppSpacing.small),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = LocalAppColors.current.white
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
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.white
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
            // Header avec info utilisateur
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo de profil
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

            // Étoiles
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

            // Commentaire
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

