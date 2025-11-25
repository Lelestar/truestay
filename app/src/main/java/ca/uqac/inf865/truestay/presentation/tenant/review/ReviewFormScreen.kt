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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.large),
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
                        onRatingChange = viewModel::setGeneralCondition
                    )
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_property_comfort),
                        rating = uiState.comfort,
                        onRatingChange = viewModel::setComfort
                    )
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_property_compliance),
                        rating = uiState.compliance,
                        onRatingChange = viewModel::setCompliance
                    )
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_property_value_for_money),
                        rating = uiState.valueForMoney,
                        onRatingChange = viewModel::setValueForMoney
                    )
                }
                ReviewType.BUILDING -> {
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_building_maintenance),
                        rating = uiState.maintenance,
                        onRatingChange = viewModel::setMaintenance
                    )
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_building_neighborhood),
                        rating = uiState.neighborhood,
                        onRatingChange = viewModel::setNeighborhood
                    )
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_building_security),
                        rating = uiState.security,
                        onRatingChange = viewModel::setSecurity
                    )
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_building_services),
                        rating = uiState.services,
                        onRatingChange = viewModel::setServices
                    )
                }
                ReviewType.NEIGHBORHOOD -> {
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_neighborhood_transport),
                        rating = uiState.transport,
                        onRatingChange = viewModel::setTransport
                    )
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_neighborhood_amenities),
                        rating = uiState.amenities,
                        onRatingChange = viewModel::setAmenities
                    )
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_neighborhood_calm),
                        rating = uiState.calm,
                        onRatingChange = viewModel::setCalm
                    )
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_neighborhood_safety),
                        rating = uiState.safety,
                        onRatingChange = viewModel::setSafety
                    )
                    TrueStayRatingInput(
                        label = stringResource(R.string.review_form_neighborhood_atmosphere),
                        rating = uiState.atmosphere,
                        onRatingChange = viewModel::setAtmosphere
                    )
                }
            }

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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = AppSpacing.small),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrueStayIcon(
                        iconRes = TrueStayIcons.Camera,
                        contentDescriptionRes = null,
                        tint = colors.primary,
                        size = 20.dp
                    )
                    Text(
                        text = stringResource(R.string.review_form_add_photos),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.primary
                    )
                }

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

            // Disclaimer card
            TrueStayCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                        verticalAlignment = Alignment.Top
                    ) {
                        TrueStayIcon(
                            iconRes = TrueStayIcons.CircleAlert,
                            contentDescriptionRes = null,
                            tint = colors.info,
                            size = 20.dp
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
                        ) {
                            Text(
                                text = stringResource(R.string.review_form_disclaimer_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.black
                            )
                            Text(
                                text = stringResource(R.string.review_form_disclaimer_message),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.grayDark
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.review_form_disclaimer_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onWarningSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.small)
                            .background(colors.warningSurface)
                            .padding(AppSpacing.small)
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

                TrueStayButton(
                    text = stringResource(R.string.review_form_submit_button),
                    onClick = {
                        viewModel.submitReview(onReviewSubmitted)
                    },
                    variant = ButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f),
                    isLoading = uiState.isSubmitting
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.large))
        }
    }
}