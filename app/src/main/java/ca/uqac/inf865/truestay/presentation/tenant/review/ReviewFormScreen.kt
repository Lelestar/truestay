package ca.uqac.inf865.truestay.presentation.tenant.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.ReviewType
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.FullscreenImageCarouselOverlay
import ca.uqac.inf865.truestay.presentation.common.components.PhotoGrid
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayRatingInput
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.common.utils.CameraLauncher
import ca.uqac.inf865.truestay.presentation.common.utils.PhotoPickerLauncher
import ca.uqac.inf865.truestay.presentation.common.utils.rememberOptimizedCameraLauncher
import ca.uqac.inf865.truestay.presentation.common.utils.rememberOptimizedMultiPhotoPickerLauncher
import ca.uqac.inf865.truestay.presentation.navigation.TrueStayTopAppBar
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewFormScreen(
    onBackClick: () -> Unit,
    onReviewSubmitted: () -> Unit,
    viewModel: ReviewFormViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    // Camera launcher for taking photos
    val cameraLauncher = rememberOptimizedCameraLauncher { optimizedUri ->
        viewModel.addPhoto(optimizedUri)
    }

    // Multi-photo picker
    val photoPickerLauncher = rememberOptimizedMultiPhotoPickerLauncher { uris ->
        viewModel.addPhotos(uris)
    }

    // Fullscreen photo overlay state
    var showFullscreenCarousel by rememberSaveable { mutableStateOf(false) }
    var fullscreenStartIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                TrueStayTopAppBar(
                    titleRes = when (viewModel.reviewType) {
                        ReviewType.PROPERTY -> R.string.review_form_title_property
                        ReviewType.BUILDING -> R.string.review_form_title_building
                        ReviewType.NEIGHBORHOOD -> R.string.review_form_title_neighborhood
                    },
                    onNavigateBack = onBackClick,
                    windowInsets = WindowInsets(0.dp)
                )
            }
        ) { paddingValues ->
            when {
                uiState.isLoading -> {
                    LoadingState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                uiState.loadError != null -> {
                    ErrorState(
                        errorMessage = stringResource(R.string.review_form_error_load),
                        onRetry = { viewModel.retryLoadReview() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                else -> {
                    ReviewFormScreenContent(
                        uiState = uiState,
                        cameraLauncher = cameraLauncher,
                        photoPickerLauncher = photoPickerLauncher,
                        onBackClick = onBackClick,
                        onReviewSubmitted = onReviewSubmitted,
                        onPhotoClick = { index ->
                            fullscreenStartIndex = index
                            showFullscreenCarousel = true
                        },
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
        }

        // Fullscreen photo carousel overlay
        if (showFullscreenCarousel) {
            val allPhotos = uiState.uploadedPhotoUrls + uiState.photoUris.map { it.toString() }
            if (allPhotos.isNotEmpty()) {
                FullscreenImageCarouselOverlay(
                    photos = allPhotos,
                    title = stringResource(R.string.review_form_photos_label),
                    initialPage = fullscreenStartIndex,
                    onClose = { showFullscreenCarousel = false }
                )
            }
        }
    }
}

/**
 * Loading state while fetching existing review
 */
@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = colors.primary)
    }
}

/**
 * Error state when loading existing review fails
 */
@Composable
private fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier.padding(AppSpacing.large),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.titleMedium,
                color = colors.error,
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

/**
 * Main content of the review form screen
 * Separated as its own composable to enable previews
 */
@Composable
fun ReviewFormScreenContent(
    uiState: ReviewFormUiState,
    cameraLauncher: CameraLauncher,
    photoPickerLauncher: PhotoPickerLauncher,
    onBackClick: () -> Unit,
    onReviewSubmitted: () -> Unit,
    onPhotoClick: (Int) -> Unit,
    viewModel: ReviewFormViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        // Main card containing title, ratings, comment and photos
        TrueStayCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
            ) {
                // Title based on review type
                Text(
                    text = when (viewModel.reviewType) {
                        ReviewType.PROPERTY -> stringResource(R.string.review_form_card_title_property)
                        ReviewType.BUILDING -> stringResource(R.string.review_form_card_title_building)
                        ReviewType.NEIGHBORHOOD -> stringResource(R.string.review_form_card_title_neighborhood)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.black
                )

                // Rating criteria based on review type
                when (viewModel.reviewType) {
                    ReviewType.PROPERTY -> {
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_property_general_condition),
                            rating = uiState.generalCondition,
                            onRatingChange = viewModel::setGeneralCondition,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_property_comfort),
                            rating = uiState.comfort,
                            onRatingChange = viewModel::setComfort,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_property_compliance),
                            rating = uiState.compliance,
                            onRatingChange = viewModel::setCompliance,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_property_value_for_money),
                            rating = uiState.valueForMoney,
                            onRatingChange = viewModel::setValueForMoney,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ReviewType.BUILDING -> {
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_building_maintenance),
                            rating = uiState.maintenance,
                            onRatingChange = viewModel::setMaintenance,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_building_neighborhood),
                            rating = uiState.neighborhood,
                            onRatingChange = viewModel::setNeighborhood,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_building_security),
                            rating = uiState.security,
                            onRatingChange = viewModel::setSecurity,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_building_services),
                            rating = uiState.services,
                            onRatingChange = viewModel::setServices,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ReviewType.NEIGHBORHOOD -> {
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_neighborhood_transport),
                            rating = uiState.transport,
                            onRatingChange = viewModel::setTransport,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_neighborhood_amenities),
                            rating = uiState.amenities,
                            onRatingChange = viewModel::setAmenities,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_neighborhood_calm),
                            rating = uiState.calm,
                            onRatingChange = viewModel::setCalm,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_neighborhood_safety),
                            rating = uiState.safety,
                            onRatingChange = viewModel::setSafety,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TrueStayRatingInput(
                            label = stringResource(R.string.review_form_neighborhood_atmosphere),
                            rating = uiState.atmosphere,
                            onRatingChange = viewModel::setAtmosphere,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Comment section
                TrueStayTextField(
                    value = uiState.comment,
                    onValueChange = viewModel::setComment,
                    label = stringResource(R.string.review_form_comment_label),
                    placeholder = stringResource(R.string.review_form_comment_placeholder),
                    minLines = 5,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth(),
                    imeAction = ImeAction.None
                )

                // Photos section
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    Text(
                        text = stringResource(R.string.review_form_photos_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.black
                    )

                    val totalPhotos = uiState.photoUris.size + uiState.uploadedPhotoUrls.size

                    // Display local URIs and uploaded photos
                    if (totalPhotos > 0) {
                        PhotoGrid(
                            localUris = uiState.photoUris,
                            uploadedUrls = uiState.uploadedPhotoUrls,
                            onDeleteLocalPhoto = { uri -> viewModel.removeLocalPhoto(uri) },
                            onDeleteUploadedPhoto = { url -> viewModel.removeUploadedPhoto(url) },
                            onPhotoClick = onPhotoClick
                        )
                    }

                    // Buttons row for adding photos
                    val canAddMorePhotos = totalPhotos < 5
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                    ) {
                        // Take photo button
                        TrueStayButton(
                            text = stringResource(R.string.review_form_take_photo),
                            onClick = { cameraLauncher.launch() },
                            variant = ButtonVariant.SECONDARY,
                            leadingIcon = TrueStayIcons.Camera,
                            modifier = Modifier.weight(1f),
                            enabled = canAddMorePhotos
                        )

                        // Pick from gallery button
                        TrueStayButton(
                            text = stringResource(R.string.review_form_pick_photo),
                            onClick = {
                                photoPickerLauncher.launch(5 - totalPhotos)
                            },
                            variant = ButtonVariant.SECONDARY,
                            leadingIcon = TrueStayIcons.Image,
                            modifier = Modifier.weight(1f),
                            enabled = canAddMorePhotos
                        )
                    }

                    // Photo count indicator
                    if (totalPhotos > 0) {
                        Text(
                            text = stringResource(R.string.review_form_photos_count, totalPhotos, 5),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.grayDark
                        )
                    }
                }
            }
        }

        // Disclaimer card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = colors.infoSurface,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = colors.info
            )
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrueStayIcon(
                        iconRes = TrueStayIcons.CircleAlert,
                        contentDescriptionRes = null,
                        tint = colors.onInfoSurface,
                        size = 20.dp
                    )

                    Text(
                        text = stringResource(R.string.review_form_disclaimer_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onInfoSurface
                    )
                }

                Text(
                    text = stringResource(R.string.review_form_disclaimer_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onInfoSurface
                )

                Text(
                    text = stringResource(R.string.review_form_disclaimer_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onInfoSurface
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Error message
        if (uiState.errorMessage != null) {
            Text(
                text = stringResource(R.string.review_form_error_submit),
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Validate that all required fields are filled based on review type
        val isFormValid = when (viewModel.reviewType) {
            ReviewType.PROPERTY -> {
                uiState.generalCondition > 0 &&
                uiState.comfort > 0 &&
                uiState.compliance > 0 &&
                uiState.valueForMoney > 0 &&
                uiState.comment.isNotBlank()
            }
            ReviewType.BUILDING -> {
                uiState.maintenance > 0 &&
                uiState.neighborhood > 0 &&
                uiState.security > 0 &&
                uiState.services > 0 &&
                uiState.comment.isNotBlank()
            }
            ReviewType.NEIGHBORHOOD -> {
                uiState.transport > 0 &&
                uiState.amenities > 0 &&
                uiState.calm > 0 &&
                uiState.safety > 0 &&
                uiState.atmosphere > 0 &&
                uiState.comment.isNotBlank()
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            TrueStayButton(
                text = stringResource(R.string.review_form_back_button),
                onClick = onBackClick,
                variant = ButtonVariant.SECONDARY,
                modifier = Modifier.weight(1f)
            )

            TrueStayButton(
                text = stringResource(R.string.review_form_submit_button),
                onClick = {
                    viewModel.submitReview(onReviewSubmitted)
                },
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.weight(1f),
                enabled = isFormValid && !uiState.isSubmitting && (uiState.existingReviewId == null || uiState.hasChanges),
                isLoading = uiState.isSubmitting
            )
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true, name = "Loading State")
@Composable
private fun LoadingStatePreview() {
    TrueStayTheme {
        LoadingState(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun ErrorStatePreview() {
    TrueStayTheme {
        ErrorState(
            errorMessage = "Erreur lors du chargement de l'avis",
            onRetry = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}