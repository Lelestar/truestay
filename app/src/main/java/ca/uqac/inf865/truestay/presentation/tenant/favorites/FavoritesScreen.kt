package ca.uqac.inf865.truestay.presentation.tenant.favorites

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCard
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCardVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayDropdown
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Screen displaying the tenant's favorite properties
 *
 * Displays the list of saved properties with the option to sort by date added, price, or rating.
 * Manages loading, error, and empty list states.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FavoritesScreen(
    onPropertyClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    // Reload favorites when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.refreshFavorites()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sortOptions = remember { FavoritesSortOption.entries }
    val arrowRotation by animateFloatAsState(
        targetValue = if (uiState.isSortDescending) 0f else 180f,
        label = "favoritesSortRotation"
    )
    val optionLabels = sortOptions.associateWith { option ->
        stringResource(id = option.labelRes)
    }
    val selectedSortLabel = optionLabels[uiState.sortOption].orEmpty()

    // Determines the type of error to display: blocking (full screen) or inline (list header).
    val blockingErrorRes = when {
        uiState.error == FavoritesError.NOT_AUTHENTICATED -> R.string.favorites_error_not_authenticated
        uiState.error == FavoritesError.LOAD_FAILED && uiState.favorites.isEmpty() -> R.string.favorites_error_loading
        else -> null
    }
    val inlineErrorRes = when {
        uiState.error == FavoritesError.REMOVE_FAILED -> R.string.favorites_error_remove
        uiState.error == FavoritesError.LOAD_FAILED && uiState.favorites.isNotEmpty() -> R.string.favorites_error_loading
        else -> null
    }
    val showBlockingLoader = uiState.isLoading && uiState.favorites.isEmpty() && blockingErrorRes == null

    Column(modifier = Modifier.fillMaxSize()) {
        Surface (
            modifier = Modifier.fillMaxWidth(),
            color = LocalAppColors.current.primary
        ) {
            Text(
                text = stringResource(R.string.screen_title_favorites),
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.white,
                modifier = Modifier.padding(AppSpacing.large)
            )
        }

        HeaderSection(
            selectedSortLabel = selectedSortLabel,
            sortOptions = sortOptions,
            optionLabels = optionLabels,
            onSortSelected = viewModel::onSortOptionSelected,
            onToggleSort = viewModel::toggleSortOrder,
            dropdownEnabled = uiState.favorites.isNotEmpty(),
            arrowRotation = arrowRotation,
            resultsCount = uiState.favorites.size,
            isSortDescending = uiState.isSortDescending
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = AppSpacing.large),
            contentAlignment = Alignment.Center
        ) {
            when {
                showBlockingLoader -> {
                    CircularProgressIndicator(
                        color = LocalAppColors.current.primary
                    )
                }

                blockingErrorRes != null -> {
                    ErrorState(
                        messageRes = blockingErrorRes,
                        onRetry = viewModel::refreshFavorites
                    )
                }

                uiState.favorites.isEmpty() -> {
                    EmptyState()
                }

                else -> {
                    FavoritesList(
                        uiState = uiState,
                        inlineErrorRes = inlineErrorRes,
                        onPropertyClick = onPropertyClick,
                        onToggleFavorite = viewModel::toggleFavorite
                    )
                }
            }
        }
    }
}

/**
 * Screen header with sorting controls and results counter
 */
@Composable
private fun HeaderSection(
    selectedSortLabel: String,
    sortOptions: List<FavoritesSortOption>,
    optionLabels: Map<FavoritesSortOption, String>,
    onSortSelected: (FavoritesSortOption) -> Unit,
    onToggleSort: () -> Unit,
    dropdownEnabled: Boolean,
    arrowRotation: Float,
    resultsCount: Int,
    isSortDescending: Boolean
) {
    val borderColor = LocalAppColors.current.grayBorder
    val sortOrderText = stringResource(
        if (isSortDescending) R.string.favorites_sort_descending
        else R.string.favorites_sort_ascending
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalAppColors.current.white)
            .drawBehind {
                val stroke = with(density) { 1.dp.toPx() }
                val y = size.height - stroke / 2
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = stroke
                )
            }
            .padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                Text(
                    text = stringResource(R.string.favorites_sort_by),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.grayDark
                )
                TrueStayDropdown(
                    selectedValue = selectedSortLabel,
                    options = sortOptions,
                    onOptionSelected = onSortSelected,
                    enabled = dropdownEnabled,
                    optionLabel = { option -> optionLabels[option].orEmpty() },
                    modifier = Modifier.widthIn(max = 160.dp)
                )
            }

            IconButton(
                onClick = onToggleSort,
                enabled = dropdownEnabled,
                modifier = Modifier.height(33.dp)
            ) {
                TrueStayIcon(
                    iconRes = TrueStayIcons.ArrowDownUp,
                    contentDescriptionRes = R.string.favorites_sort_toggle_content_description,
                    tint = if (dropdownEnabled) LocalAppColors.current.black else LocalAppColors.current.grayMedium,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pluralStringResource(R.plurals.favorites_count, resultsCount, resultsCount),
                style = MaterialTheme.typography.bodySmall,
                color = LocalAppColors.current.grayMedium
            )
            Text(
                text = "•",
                style = MaterialTheme.typography.bodySmall,
                color = LocalAppColors.current.grayMedium
            )
            Text(
                text = sortOrderText,
                style = MaterialTheme.typography.bodySmall,
                color = LocalAppColors.current.grayMedium
            )
        }
    }
}

/**
 * List of favorite properties with detailed cards
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun FavoritesList(
    uiState: FavoritesUiState,
    inlineErrorRes: Int?,
    onPropertyClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (inlineErrorRes != null) {
            Text(
                text = stringResource(id = inlineErrorRes),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.small),
                textAlign = TextAlign.Center
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            items(
                items = uiState.favorites,
                key = { item ->
                    item.favorite.id.ifEmpty { item.property.id }
                }
            ) { favoriteItem ->
                val propertyId = favoriteItem.property.id
                val isRemoving = uiState.pendingRemovalIds.contains(propertyId)
                val addedAtDate = remember(favoriteItem.favorite.addedAt) {
                    FavoriteAddedDateFormatter.format(favoriteItem.favorite.addedAt)
                }
                val metadataText = addedAtDate?.let {
                    stringResource(
                        id = R.string.favorite_added_at,
                        it
                    )
                }
                PropertyCard(
                    property = favoriteItem.property,
                    variant = PropertyCardVariant.DETAILED,
                    onClick = { onPropertyClick(propertyId) },
                    onFavoriteClick = { onToggleFavorite(propertyId) },
                    isFavorite = true,
                    isFavoriteActionEnabled = !isRemoving,
                    metadataText = metadataText
                )
            }
        }
    }
}

/**
 * Empty state displayed when the user has no favorites
 */
@Composable
private fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TrueStayIcon(
            iconRes = TrueStayIcons.Heart,
            contentDescriptionRes = null,
            tint = LocalAppColors.current.grayMedium,
            modifier = Modifier.padding(bottom = AppSpacing.small)
        )
        Text(
            text = stringResource(R.string.favorites_no_favorites),
            style = MaterialTheme.typography.titleMedium,
            color = LocalAppColors.current.grayDark,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Error status displayed in case of loading or authentication issues
 */
@Composable
private fun ErrorState(
    messageRes: Int,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = messageRes),
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

/**
 * Formatter for favorite added date
 * Converts a timestamp to a formatted local date
 */
private object FavoriteAddedDateFormatter {
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
    @RequiresApi(Build.VERSION_CODES.O)
    private val zoneId: ZoneId = ZoneId.systemDefault()

    @RequiresApi(Build.VERSION_CODES.O)
    fun format(timestamp: Long): String? {
        if (timestamp <= 0L) return null
        return Instant.ofEpochMilli(timestamp)
            .atZone(zoneId)
            .format(formatter)
    }
}

// ==========================================
// Previews
// ==========================================
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun FavoritesScreenEmptyPreview() {
    TrueStayTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection(
                selectedSortLabel = "Date ajoutée",
                sortOptions = FavoritesSortOption.entries,
                optionLabels = FavoritesSortOption.entries.associateWith {
                    when(it) {
                        FavoritesSortOption.DATE_ADDED -> "Date ajoutée"
                        FavoritesSortOption.PRICE -> "Prix"
                        FavoritesSortOption.RATING -> "Note"
                    }
                },
                onSortSelected = {},
                onToggleSort = {},
                dropdownEnabled = false,
                arrowRotation = 0f,
                resultsCount = 0,
                isSortDescending = true
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                EmptyState()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun FavoritesScreenLoadingPreview() {
    TrueStayTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection(
                selectedSortLabel = "Date ajoutée",
                sortOptions = FavoritesSortOption.entries,
                optionLabels = FavoritesSortOption.entries.associateWith {
                    when(it) {
                        FavoritesSortOption.DATE_ADDED -> "Date ajoutée"
                        FavoritesSortOption.PRICE -> "Prix"
                        FavoritesSortOption.RATING -> "Note"
                    }
                },
                onSortSelected = {},
                onToggleSort = {},
                dropdownEnabled = false,
                arrowRotation = 0f,
                resultsCount = 0,
                isSortDescending = true
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = LocalAppColors.current.primary
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun FavoritesScreenErrorPreview() {
    TrueStayTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection(
                selectedSortLabel = "Date ajoutée",
                sortOptions = FavoritesSortOption.entries,
                optionLabels = FavoritesSortOption.entries.associateWith {
                    when(it) {
                        FavoritesSortOption.DATE_ADDED -> "Date ajoutée"
                        FavoritesSortOption.PRICE -> "Prix"
                        FavoritesSortOption.RATING -> "Note"
                    }
                },
                onSortSelected = {},
                onToggleSort = {},
                dropdownEnabled = false,
                arrowRotation = 0f,
                resultsCount = 0,
                isSortDescending = true
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                ErrorState(
                    messageRes = R.string.favorites_error_loading,
                    onRetry = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    TrueStayTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.large),
            contentAlignment = Alignment.Center
        ) {
            EmptyState()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    TrueStayTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.large),
            contentAlignment = Alignment.Center
        ) {
            ErrorState(
                messageRes = R.string.favorites_error_loading,
                onRetry = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HeaderSectionPreview() {
    TrueStayTheme {
        HeaderSection(
            selectedSortLabel = "Date ajoutée",
            sortOptions = FavoritesSortOption.entries,
            optionLabels = FavoritesSortOption.entries.associateWith {
                when(it) {
                    FavoritesSortOption.DATE_ADDED -> "Date ajoutée"
                    FavoritesSortOption.PRICE -> "Prix"
                    FavoritesSortOption.RATING -> "Note"
                }
            },
            onSortSelected = {},
            onToggleSort = {},
            dropdownEnabled = true,
            arrowRotation = 0f,
            resultsCount = 5,
            isSortDescending = true
        )
    }
}

