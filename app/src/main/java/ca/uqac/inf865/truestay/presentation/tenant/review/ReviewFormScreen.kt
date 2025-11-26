package ca.uqac.inf865.truestay.presentation.tenant.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.ReviewType
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.PhotoGrid
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayRatingInput
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.common.utils.rememberOptimizedCameraLauncher
import ca.uqac.inf865.truestay.presentation.navigation.TrueStayTopAppBar
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewFormScreen(
    rentalId: String,
    reviewType: ReviewType,
    onBackClick: () -> Unit,
    onReviewSubmitted: () -> Unit,
    viewModel: ReviewFormViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val colors = LocalAppColors.current

    // Camera launcher for taking photos
    val cameraLauncher = rememberOptimizedCameraLauncher { optimizedUri ->
        viewModel.addPhoto(optimizedUri)
    }

    // Multi-photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addPhotos(uris)
        }
    }

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
        if (uiState.isLoading) {
            // Show loading indicator while loading existing review
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                            ReviewType.PROPERTY -> stringResource(R.string.review_form_title_property)
                            ReviewType.BUILDING -> stringResource(R.string.review_form_title_building)
                            ReviewType.NEIGHBORHOOD -> stringResource(R.string.review_form_title_neighborhood)
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

                    Spacer(modifier = Modifier.height(AppSpacing.small))

                    // Comment section
                    TrueStayTextField(
                        value = uiState.comment,
                        onValueChange = viewModel::setComment,
                        label = stringResource(R.string.review_form_comment_label),
                        placeholder = stringResource(R.string.review_form_comment_placeholder),
                        minLines = 5,
                        maxLines = 10,
                        modifier = Modifier.fillMaxWidth()
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

                        // Display uploaded photos
                        if (uiState.uploadedPhotoUrls.isNotEmpty()) {
                            PhotoGrid(
                                photos = uiState.uploadedPhotoUrls,
                                onPhotoClick = { /* Optionnel: afficher en plein écran */ },
                                onDeletePhoto = { photoUrl -> viewModel.removePhoto(photoUrl) }
                            )
                        }

                        // Photo upload error
                        if (uiState.photoUploadError != null) {
                            Text(
                                text = stringResource(R.string.review_form_photo_upload_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Buttons row for adding photos
                        val canAddMorePhotos = uiState.uploadedPhotoUrls.size < 5
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
                                iconTint = colors.black,
                                modifier = Modifier.weight(1f),
                                enabled = canAddMorePhotos && !uiState.isUploadingPhoto
                            )

                            // Pick from gallery button
                            TrueStayButton(
                                text = stringResource(R.string.review_form_pick_photo),
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                variant = ButtonVariant.SECONDARY,
                                leadingIcon = TrueStayIcons.Image,
                                iconTint = colors.black,
                                modifier = Modifier.weight(1f),
                                enabled = canAddMorePhotos && !uiState.isUploadingPhoto
                            )
                        }

                        // Photo count and loading indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.uploadedPhotoUrls.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.review_form_photos_count, uiState.uploadedPhotoUrls.size, 5),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.grayDark
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            if (uiState.isUploadingPhoto) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(4.dp),
                                        color = colors.primary,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = stringResource(R.string.review_form_uploading_photo),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.grayDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Disclaimer card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
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
                            tint = colors.info,
                            size = 20.dp
                        )

                        Text(
                            text = stringResource(R.string.review_form_disclaimer_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.info
                        )
                    }

                    Text(
                        text = stringResource(R.string.review_form_disclaimer_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.info
                    )

                    Text(
                        text = stringResource(R.string.review_form_disclaimer_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.info
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.medium))

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

                TrueStayButton(
                    text = stringResource(R.string.review_form_submit_button),
                    onClick = {
                        viewModel.submitReview(onReviewSubmitted)
                    },
                    variant = ButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f),
                    enabled = isFormValid,
                    isLoading = uiState.isSubmitting
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.large))
            }
        }
    }
}