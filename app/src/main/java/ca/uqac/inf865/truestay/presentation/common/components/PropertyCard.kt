package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyRatings
import ca.uqac.inf865.truestay.domain.model.Room
import ca.uqac.inf865.truestay.domain.model.RoomType
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import coil3.compose.AsyncImage
import ca.uqac.inf865.truestay.presentation.common.utils.DateUtils
import java.util.Locale

enum class PropertyCardVariant {
    COMPACT,     // For SearchScreen and RentalsScreen (tenant)
    DETAILED,    // For FavoritesScreen (tenant) and PropertiesScreen (landlord)
    INTERACTIVE  // For RentalsScreen (tenant+landlord) with action buttons
}

data class PropertyAction(
    val iconRes: Int?,
    val label: String,
    val onClick: () -> Unit,
    val variant: ButtonVariant = ButtonVariant.SECONDARY
)

@Composable
fun PropertyCard(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PropertyCardVariant = PropertyCardVariant.COMPACT,
    // Optional parameters for specific variants
    onFavoriteClick: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    isFavoriteActionEnabled: Boolean = true,
    metadataText: String? = null,
    actions: List<PropertyAction>? = null,
    startDate: Long? = null,
    endDate: Long? = null,
    // Landlord actions menu
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onToggleStatusClick: (() -> Unit)? = null,
    showLandlordMenu: Boolean = false,
) {
    when (variant) {
        PropertyCardVariant.COMPACT -> PropertyCardCompact(
            property = property,
            modifier = modifier,
            onClick = onClick
        )
        PropertyCardVariant.DETAILED -> PropertyCardDetailed(
            property = property,
            modifier = modifier,
            onFavoriteClick = onFavoriteClick,
            isFavorite = isFavorite,
            isFavoriteActionEnabled = isFavoriteActionEnabled,
            onClick = onClick,
            metadataText = metadataText,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onToggleStatusClick = onToggleStatusClick,
            showLandlordMenu = showLandlordMenu
        )
        PropertyCardVariant.INTERACTIVE -> PropertyCardInteractive(
            property = property,
            modifier = modifier,
            actions = actions ?: emptyList(),
            startDate = startDate,
            endDate = endDate,
            onClick = onClick
        )
    }
}

@Composable
private fun PropertyCardCompact(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TrueStayCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        padding = 0.dp
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Property Image
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
                        // Placeholder when no image
                        Image(
                            painter = painterResource(R.drawable.img_placeholder),
                            contentDescription = stringResource(R.string.property_image_placeholder),
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Property Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .padding(AppSpacing.medium)
                ) {
                    // Property Name
                    Text(
                        text = property.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = LocalAppColors.current.black,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xsmall))

                    // Address
                    Text(
                        text = "${property.address.street}, ${property.address.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalAppColors.current.grayDark,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.small))

                    // Bottom Row: Price and Rating
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Monthly Rent
                        Text(
                            text = stringResource(R.string.property_monthly_rent, property.monthlyRent),
                            style = MaterialTheme.typography.titleMedium,
                            color = LocalAppColors.current.primary
                        )

                        // Rating
                        if (property.ratings.propertyAverageRating > 0) {
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
                                    text = String.format(
                                        Locale.getDefault(),
                                        "%.1f",
                                        property.ratings.propertyAverageRating
                                    ),
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

@Composable
private fun PropertyCardDetailed(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteClick: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    isFavoriteActionEnabled: Boolean = true,
    metadataText: String? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onToggleStatusClick: (() -> Unit)? = null,
    showLandlordMenu: Boolean = false
) {
    val bedroomCount = property.rooms.count { it.type == RoomType.BEDROOM }
    val bathroomCount = property.rooms.count { it.type == RoomType.BATHROOM }
    val favoriteTint = when {
        !isFavoriteActionEnabled -> LocalAppColors.current.grayMedium
        isFavorite -> LocalAppColors.current.error
        else -> LocalAppColors.current.grayDark
    }

    var showDropdownMenu by remember { mutableStateOf(false) }

    TrueStayCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        padding = 0.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(LocalAppColors.current.grayLight)
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

                // Availability badge
                val availabilityText = when {
                    property.status == ca.uqac.inf865.truestay.domain.model.PropertyStatus.PAUSED ->
                        stringResource(R.string.property_paused)
                    property.isAvailable ->
                        stringResource(R.string.property_available)
                    else ->
                        stringResource(R.string.property_unavailable)
                }
                val availabilityVariant = when {
                    property.status == ca.uqac.inf865.truestay.domain.model.PropertyStatus.PAUSED ->
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

                // Top right: Favorite button OR Landlord menu
                if (showLandlordMenu) {
                    // Landlord menu with 3 dots
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(AppSpacing.small)
                    ) {
                        IconButton(
                            onClick = { showDropdownMenu = true },
                            shape = AppShapes.medium,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = LocalAppColors.current.white.copy(alpha = 0.5f)
                            )
                        ) {
                            TrueStayIcon(
                                iconRes = TrueStayIcons.EllipsisVertical,
                                contentDescriptionRes = null,
                                tint = LocalAppColors.current.grayDark,
                                size = 24.dp
                            )
                        }

                        androidx.compose.material3.DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false },
                            modifier = androidx.compose.ui.Modifier
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
                            if (onEditClick != null) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.property_action_edit),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = LocalAppColors.current.black
                                        )
                                    },
                                    onClick = {
                                        showDropdownMenu = false
                                        onEditClick()
                                    },
                                    modifier = androidx.compose.ui.Modifier.padding(horizontal = AppSpacing.small),
                                    colors = androidx.compose.material3.MenuDefaults.itemColors(
                                        textColor = LocalAppColors.current.black
                                    )
                                )
                            }
                            if (onToggleStatusClick != null) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (property.status == ca.uqac.inf865.truestay.domain.model.PropertyStatus.PUBLISHED)
                                                stringResource(R.string.property_action_pause)
                                            else
                                                stringResource(R.string.property_action_publish),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = LocalAppColors.current.black
                                        )
                                    },
                                    onClick = {
                                        showDropdownMenu = false
                                        onToggleStatusClick()
                                    },
                                    modifier = androidx.compose.ui.Modifier.padding(horizontal = AppSpacing.small),
                                    colors = androidx.compose.material3.MenuDefaults.itemColors(
                                        textColor = LocalAppColors.current.black
                                    )
                                )
                            }
                            if (onDeleteClick != null) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.property_action_delete),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = LocalAppColors.current.error
                                        )
                                    },
                                    onClick = {
                                        showDropdownMenu = false
                                        onDeleteClick()
                                    },
                                    modifier = androidx.compose.ui.Modifier.padding(horizontal = AppSpacing.small),
                                    colors = androidx.compose.material3.MenuDefaults.itemColors(
                                        textColor = LocalAppColors.current.error
                                    )
                                )
                            }
                        }
                    }
                } else if (onFavoriteClick != null) {
                    // Favorite button (for tenant)
                    IconButton(
                        onClick = onFavoriteClick,
                        enabled = isFavoriteActionEnabled,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(AppSpacing.small),
                        shape = AppShapes.medium,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = LocalAppColors.current.white.copy(alpha = 0.5f),
                            disabledContainerColor = LocalAppColors.current.white.copy(alpha = 0.5f)
                        )
                    ) {
                        TrueStayIcon(
                            iconRes = if (isFavorite) TrueStayIcons.HeartFilled else TrueStayIcons.Heart,
                            contentDescriptionRes = R.string.property_favorite_button_content_description,
                            tint = favoriteTint,
                            size = 24.dp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.large),
            ) {
                Text(
                    text = property.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LocalAppColors.current.black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(AppSpacing.small))

                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
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
                        color = LocalAppColors.current.grayDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.medium))

                if (bedroomCount > 0 || bathroomCount > 0 || property.surface > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.large),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bedroomCount > 0) {
                            val bedroomsLabel = stringResource(
                                id = R.string.property_feature_bedrooms,
                                bedroomCount
                            )
                            FeatureItem(
                                iconRes = TrueStayIcons.Bed,
                                text = bedroomsLabel
                            )
                        }
                        if (bathroomCount > 0) {
                            val bathroomsLabel = stringResource(
                                id = R.string.property_feature_bathrooms,
                                bathroomCount
                            )
                            FeatureItem(
                                iconRes = TrueStayIcons.Bath,
                                text = bathroomsLabel
                            )
                        }
                        if (property.surface > 0) {
                            val surfaceLabel = stringResource(
                                id = R.string.property_feature_surface,
                                property.surface
                            )
                            FeatureItem(
                                iconRes = TrueStayIcons.Square,
                                text = surfaceLabel
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.large))

                val additionalRatingsSummary = buildAdditionalRatingsSummary(
                    buildingLabel = stringResource(id = R.string.property_rating_building_label),
                    buildingRating = property.ratings.buildingAverageRating,
                    neighborhoodLabel = stringResource(id = R.string.property_rating_neighborhood_label),
                    neighborhoodRating = property.ratings.neighborhoodAverageRating
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.large),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (property.ratings.propertyAverageRating > 0f) {
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
                                text = String.format(
                                    Locale.getDefault(),
                                    "%.1f",
                                    property.ratings.propertyAverageRating
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = LocalAppColors.current.black
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.property_rating_review_count,
                                    property.ratings.propertyReviewCount
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalAppColors.current.grayDark
                            )
                        }
                    }
                    if (additionalRatingsSummary != null) {
                        Text(
                            text = additionalRatingsSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalAppColors.current.grayDark,
                            textAlign = TextAlign.End
                        )
                    }
                }

                metadataText?.let {
                    Spacer(modifier = Modifier.height(AppSpacing.large))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalAppColors.current.grayMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(
    iconRes: Int,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrueStayIcon(
            iconRes = iconRes,
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

private fun buildAdditionalRatingsSummary(
    buildingLabel: String,
    buildingRating: Float,
    neighborhoodLabel: String,
    neighborhoodRating: Float
): AnnotatedString? {
    val hasBuildingRating = buildingRating > 0f
    val hasNeighborhoodRating = neighborhoodRating > 0f

    if (!hasBuildingRating && !hasNeighborhoodRating) {
        return null
    }

    return buildAnnotatedString {
        if (hasBuildingRating) {
            appendRatingSegment(buildingLabel, buildingRating)
        }
        if (hasNeighborhoodRating) {
            if (hasBuildingRating) {
                append("  |  ")
            }
            appendRatingSegment(neighborhoodLabel, neighborhoodRating)
        }
    }
}

private fun AnnotatedString.Builder.appendRatingSegment(
    label: String,
    value: Float
) {
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
        append(label)
    }
    append(" : ")
    append(String.format(Locale.getDefault(), "%.1f", value))
}

@Composable
private fun PropertyCardInteractive(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<PropertyAction> = emptyList(),
    startDate: Long? = null,
    endDate: Long? = null
) {
    TrueStayCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        padding = 0.dp
    ) {
        Column {
            // Header image with badges (reuse Detailed style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(LocalAppColors.current.grayLight)
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

                // Availability badge
                TrueStayBadge(
                    text = stringResource(R.string.property_rental_in_progress),
                    variant = BadgeVariant.SUCCESS,
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

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.large)
            ) {
                // Property name
                Text(
                    text = property.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LocalAppColors.current.black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(AppSpacing.small))

                // Address
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
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
                        color = LocalAppColors.current.grayDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacing.medium))

                // Rental period (start / end dates)
                val startText = startDate?.let { DateUtils.formatLocalDate(it) }
                val endText = endDate?.let { DateUtils.formatLocalDate(it) }
                if (startText != null || endText != null) {
                    Column (
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LocalAppColors.current.graySurface)
                            .padding(AppSpacing.large)
                            .clip(AppShapes.medium)
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
                        // Days left until end
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
                                    text = stringResource(R.string.property_rental_days_left, daysLeft.coerceAtLeast(0)),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LocalAppColors.current.primary
                                )
                            }
                        }
                    }
                }

                // Actions
                if (actions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppSpacing.large))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                    ) {
                        actions.forEach { action ->
                            TrueStayButton(
                                text = action.label,
                                onClick = action.onClick,
                                variant = action.variant,
                                leadingIcon = action.iconRes
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun computeDaysLeft(endTimestamp: Long): Int {
    val now = System.currentTimeMillis()
    val millisInDay = 24L * 60L * 60L * 1000L
    val diff = (endTimestamp - now)
    return (diff / millisInDay).toInt()
}


// ==========================================
// Previews
// ==========================================
@Preview(name = "PropertyCard - Compact", showBackground = true)
@Composable
private fun PropertyCardCompactPreview() {
    TrueStayTheme {
        PropertyCard(
            property = Property(
                id = "1",
                name = "Appartement moderne 2 pièces",
                address = Address(
                    street = "15 Rue de la Paix",
                    city = "Paris",
                    postalCode = "75001"
                ),
                monthlyRent = 1900,
                ratings = PropertyRatings(
                    propertyAverageRating = 4.5f,
                    propertyReviewCount = 12
                ),
                photos = listOf("https://picsum.photos/seed/1/400/300")
            ),
            variant = PropertyCardVariant.COMPACT,
            onClick = {}
        )
    }
}

@Preview(name = "PropertyCard - Compact No Rating", showBackground = true)
@Composable
private fun PropertyCardCompactNoRatingPreview() {
    TrueStayTheme {
        PropertyCard(
            property = Property(
                id = "1",
                name = "Studio lumineux centre-ville",
                address = Address(
                    street = "42 Avenue des Champs",
                    city = "Lyon",
                    postalCode = "69001"
                ),
                monthlyRent = 850,
                photos = listOf()
            ),
            variant = PropertyCardVariant.COMPACT,
            onClick = {}
        )
    }
}

@Preview(name = "PropertyCard - Detailed", showBackground = true)
@Composable
private fun PropertyCardDetailedPreview() {
    TrueStayTheme {
        PropertyCard(
            property = Property(
                id = "2",
                name = "Loft lumineux Vieux-Port",
                address = Address(
                    street = "120 Rue de la Commune",
                    city = "Montréal",
                    postalCode = "H2Y 1J3"
                ),
                description = "Superbe loft industriel rénové avec poutres apparentes, cuisine haut de gamme et grande terrasse privée.",
                monthlyRent = 2200,
                surface = 95,
                rooms = listOf(
                    Room(id = "room-1", name = "Chambre 1", type = RoomType.BEDROOM),
                    Room(id = "room-2", name = "Chambre 2", type = RoomType.BEDROOM),
                    Room(id = "room-3", name = "Salle de bain", type = RoomType.BATHROOM)
                ),
                ratings = PropertyRatings(
                    propertyAverageRating = 4.8f,
                    propertyReviewCount = 24,
                    buildingAverageRating = 4.5f,
                    neighborhoodAverageRating = 4.7f
                ),
                photos = listOf("https://picsum.photos/seed/2/600/400")
            ),
            variant = PropertyCardVariant.DETAILED,
            onClick = {},
            onFavoriteClick = {},
            isFavorite = true,
            metadataText = "Ajouté le 12 octobre 2025"
        )
    }
}

@Preview(name = "PropertyCard - Interactive", showBackground = true)
@Composable
private fun PropertyCardInteractivePreview() {
    TrueStayTheme {
        PropertyCard(
            property = Property(
                id = "3",
                name = "Appartement interactif",
                address = Address(
                    street = "10 Rue des Fleurs",
                    city = "Nice",
                    postalCode = "06000"
                ),
                monthlyRent = 1200,
                photos = listOf("https://picsum.photos/seed/3/600/400")
            ),
            variant = PropertyCardVariant.INTERACTIVE,
            onClick = {},
            startDate = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L,
            endDate = System.currentTimeMillis() + 30L * 24L * 60L * 60L * 1000L,
            actions = listOf(
                PropertyAction(
                    iconRes = TrueStayIcons.FileText,
                    label = "État des lieux",
                    onClick = {}
                ),
                PropertyAction(
                    iconRes = TrueStayIcons.MessageSquare,
                    label = "Ajouter un commentaire",
                    onClick = {},
                    variant = ButtonVariant.SECONDARY
                )
            )
        )
    }
}
