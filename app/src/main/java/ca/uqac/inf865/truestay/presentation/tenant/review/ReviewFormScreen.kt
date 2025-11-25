package ca.uqac.inf865.truestay.presentation.tenant.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.ReviewType
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayRatingInput
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.navigation.TrueStayTopAppBar
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import coil3.compose.AsyncImage

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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                    ) {
                        Text(
                            text = stringResource(R.string.review_form_comment_label),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.black
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(AppShapes.medium)
                                .background(colors.grayLight)
                                .padding(AppSpacing.medium)
                        ) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = uiState.comment,
                                onValueChange = viewModel::setComment,
                                modifier = Modifier.fillMaxSize(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = colors.black
                                ),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (uiState.comment.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.review_form_comment_placeholder),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.grayMedium
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }

                    // Photos section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                    ) {
                        Text(
                            text = stringResource(R.string.review_form_photos_label),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.black
                        )

                        TrueStayButton(
                            text = stringResource(R.string.review_form_add_photos),
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            variant = ButtonVariant.SECONDARY,
                            leadingIcon = TrueStayIcons.Camera,
                            iconTint = colors.black,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Display selected photos
                        if (uiState.photoUris.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                            ) {
                                uiState.photoUris.take(3).forEach { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(AppShapes.small)
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(colors.error)
                                                .clickable { viewModel.removePhoto(uri) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            TrueStayIcon(
                                                iconRes = TrueStayIcons.X,
                                                contentDescriptionRes = null,
                                                tint = colors.white,
                                                size = 12.dp
                                            )
                                        }
                                    }
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