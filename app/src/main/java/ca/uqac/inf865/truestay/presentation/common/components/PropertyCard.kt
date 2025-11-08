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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyRatings
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import coil3.compose.AsyncImage
import java.util.Locale

enum class PropertyCardVariant {
    COMPACT,     // For SearchScreen and RentalsScreen (tenant)
    DETAILED,    // For FavoritesScree (tenant) and PropertiesScreen (landlord)
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
    actions: List<PropertyAction>? = null, // For INTERACTIVE variant
    // TODO: Complete
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
            onClick = onClick
        )
        PropertyCardVariant.INTERACTIVE -> PropertyCardInteractive(
            property = property,
            modifier = modifier,
            actions = actions ?: emptyList(),
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
    onFavoriteClick: (() -> Unit)? = null
) {
    // TODO: Implement detailed variant
    TrueStayCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text("Detailed variant - TODO")
    }
}

@Composable
private fun PropertyCardInteractive(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<PropertyAction> = emptyList()
) {
    // TODO: Implement interactive variant
    TrueStayCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text("Interactive variant - TODO")
    }
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