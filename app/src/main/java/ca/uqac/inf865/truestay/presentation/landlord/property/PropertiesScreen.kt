package ca.uqac.inf865.truestay.presentation.landlord.property

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

/**
 * Screen displaying the landlord's properties
 *
 * Displays the list of properties owned by the landlord with the option to add or edit properties.
 * Manages loading, error, and empty list states.
 */
@Composable
fun PropertiesScreen(
    onAddProperty: () -> Unit,
    onEditProperty: (String) -> Unit,
    viewModel: PropertiesViewModel = hiltViewModel()
) {
    // Reload properties when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadProperties()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Determines the type of error to display: blocking (full screen) or inline (list header).
    val blockingErrorRes = when {
        uiState.error == PropertiesError.NOT_AUTHENTICATED -> R.string.favorites_error_not_authenticated
        uiState.error == PropertiesError.LOAD_FAILED && uiState.properties.isEmpty() -> R.string.favorites_error_loading
        else -> null
    }
    val showBlockingLoader = uiState.isLoading && uiState.properties.isEmpty() && blockingErrorRes == null

    Column(modifier = Modifier.fillMaxSize()) {
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
                    text = stringResource(R.string.bottom_nav_properties),
                    style = MaterialTheme.typography.headlineMedium,
                    color = LocalAppColors.current.white
                )

                Spacer(modifier = Modifier.height(AppSpacing.small))

                Text(
                    text = stringResource(R.string.properties_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.white.copy(alpha = 0.9f)
                )
            }
        }

        HeaderSection(
            resultsCount = uiState.properties.size,
            onAddProperty = onAddProperty
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
                        onRetry = viewModel::loadProperties
                    )
                }

                uiState.properties.isEmpty() -> {
                    EmptyState(
                        onAddProperty = onAddProperty
                    )
                }

                else -> {
                    PropertiesList(
                        uiState = uiState,
                        onPropertyClick = onEditProperty,
                        onEditProperty = { propertyId ->
                            onEditProperty(propertyId)
                        },
                        onDeleteProperty = { propertyId ->
                            viewModel.deleteProperty(propertyId)
                        },
                        onTogglePropertyStatus = { propertyId ->
                            viewModel.togglePropertyStatus(propertyId)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Screen header with results counter and add button
 */
@Composable
private fun HeaderSection(
    resultsCount: Int,
    onAddProperty: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalAppColors.current.white)
            .padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Text(
            text = pluralStringResource(R.plurals.properties_count, resultsCount, resultsCount),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            color = LocalAppColors.current.grayDark
        )

        TrueStayButton(
            text = stringResource(R.string.properties_add_button),
            onClick = onAddProperty,
            variant = ButtonVariant.PRIMARY,
            leadingIcon = TrueStayIcons.Plus,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * List of properties with detailed cards
 */
@Composable
private fun PropertiesList(
    uiState: PropertiesUiState,
    onPropertyClick: (String) -> Unit,
    onEditProperty: (String) -> Unit,
    onDeleteProperty: (String) -> Unit,
    onTogglePropertyStatus: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        items(
            items = uiState.properties,
            key = { property -> property.id }
        ) { property ->
            PropertyCard(
                property = property,
                variant = PropertyCardVariant.DETAILED,
                onClick = { onPropertyClick(property.id) },
                isFavoriteActionEnabled = false,
                showLandlordMenu = true,
                onEditClick = { onEditProperty(property.id) },
                onDeleteClick = { onDeleteProperty(property.id) },
                onToggleStatusClick = { onTogglePropertyStatus(property.id) }
            )
        }
    }
}

/**
 * Error state display
 */
@Composable
private fun ErrorState(
    messageRes: Int,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large),
        modifier = Modifier.padding(AppSpacing.large)
    ) {
        TrueStayIcon(
            iconRes = TrueStayIcons.CircleAlert,
            contentDescriptionRes = null,
            tint = LocalAppColors.current.error,
            size = 48.dp
        )
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            color = LocalAppColors.current.grayDark,
            textAlign = TextAlign.Center
        )
        TrueStayButton(
            text = stringResource(R.string.common_retry),
            onClick = onRetry,
            variant = ButtonVariant.PRIMARY
        )
    }
}

/**
 * Empty state display when no properties exist
 */
@Composable
private fun EmptyState(
    onAddProperty: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large),
        modifier = Modifier.padding(AppSpacing.large)
    ) {
        TrueStayIcon(
            iconRes = TrueStayIcons.House,
            contentDescriptionRes = null,
            tint = LocalAppColors.current.grayMedium,
            size = 64.dp
        )
        Text(
            text = stringResource(R.string.properties_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = LocalAppColors.current.black,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.properties_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalAppColors.current.grayDark,
            textAlign = TextAlign.Center
        )
        TrueStayButton(
            text = stringResource(R.string.properties_add_button),
            onClick = onAddProperty,
            variant = ButtonVariant.PRIMARY,
            leadingIcon = TrueStayIcons.Plus
        )
    }
}

@Preview
@Composable
private fun PropertiesScreenPreview() {
    TrueStayTheme {
        PropertiesScreen(
            onAddProperty = {},
            onEditProperty = {}
        )
    }
}