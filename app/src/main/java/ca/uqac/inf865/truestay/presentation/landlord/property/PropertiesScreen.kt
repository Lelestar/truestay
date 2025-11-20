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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
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
    onPropertyClick: (String) -> Unit,
    onAddProperty: () -> Unit,
    onEditProperty: (String) -> Unit,
    viewModel: PropertiesViewModel = hiltViewModel()
) {
    // Reload properties when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadProperties()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var propertyIdPendingDeletion by remember { mutableStateOf<String?>(null) }

    // Determines the type of error to display: blocking (full screen) or inline (list header).
    val blockingErrorRes = when {
        uiState.error == PropertiesError.NOT_AUTHENTICATED -> R.string.properties_error_not_authenticated
        uiState.error == PropertiesError.LOAD_FAILED && uiState.properties.isEmpty() -> R.string.properties_error_loading
        else -> null
    }
    val showBlockingLoader = uiState.isLoading && uiState.properties.isEmpty()
    val shouldShowHeader = !showBlockingLoader && blockingErrorRes == null && uiState.properties.isNotEmpty()

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

        if (shouldShowHeader) {
            HeaderSection(
                resultsCount = uiState.properties.size,
                onAddProperty = onAddProperty
            )
        }

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
                        onPropertyClick = onPropertyClick,
                        onEditProperty = { propertyId ->
                            onEditProperty(propertyId)
                        },
                        onDeleteProperty = { propertyId ->
                            propertyIdPendingDeletion = propertyId
                        },
                        onTogglePropertyStatus = { propertyId ->
                            viewModel.togglePropertyStatus(propertyId)
                        }
                    )
                }
            }
        }

        propertyIdPendingDeletion?.let { pendingId ->
            AlertDialog(
                onDismissRequest = { propertyIdPendingDeletion = null },
                title = {
                    Text(
                        text = stringResource(R.string.properties_delete_dialog_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(text = stringResource(R.string.properties_delete_dialog_message))
                },
                confirmButton = {
                    TrueStayButton(
                        text = stringResource(R.string.properties_delete_dialog_confirm),
                        onClick = {
                            propertyIdPendingDeletion = null
                            viewModel.deleteProperty(pendingId)
                        },
                        variant = ButtonVariant.DANGER
                    )
                },
                dismissButton = {
                    TrueStayButton(
                        text = stringResource(R.string.properties_delete_dialog_cancel),
                        onClick = { propertyIdPendingDeletion = null },
                        variant = ButtonVariant.SECONDARY
                    )
                },
                shape = AppShapes.large,
                containerColor = LocalAppColors.current.white
            )
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
                isLandlord = true,
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

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
private fun PropertiesScreenEmptyPreview() {
    TrueStayTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(onAddProperty = {})
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PropertiesScreenLoadingPreview() {
    TrueStayTheme {
        Column(modifier = Modifier.fillMaxSize()) {
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

@Preview(showBackground = true)
@Composable
private fun PropertiesScreenErrorPreview() {
    TrueStayTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                ErrorState(
                    messageRes = R.string.properties_error_loading,
                    onRetry = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PropertiesHeaderSectionPreview() {
    TrueStayTheme {
        HeaderSection(
            resultsCount = 3,
            onAddProperty = {}
        )
    }
}
