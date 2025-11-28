package ca.uqac.inf865.truestay.presentation.landlord.property

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.RoomType
import ca.uqac.inf865.truestay.domain.model.getLabelRes
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.PhotoGrid
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayDropdown
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.TrueStaySwitch
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.common.utils.rememberOptimizedMultiPhotoPickerLauncher
import ca.uqac.inf865.truestay.presentation.navigation.TrueStayTopAppBar
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyFormScreen(
    propertyId: String? = null, // null => add mode, not null => edit mode
    onBackClick: () -> Unit,
    onPropertySaved: () -> Unit,
    viewModel: PropertyFormViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val colors = LocalAppColors.current
    val focusManager = LocalFocusManager.current

    // Multi-photo picker
    val photoPickerLauncher = rememberOptimizedMultiPhotoPickerLauncher(maxItems = 10) { uris ->
        viewModel.addPhotos(uris)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TrueStayTopAppBar(
                titleRes = if (propertyId == null) R.string.property_form_title_add else R.string.property_form_title_edit,
                onNavigateBack = onBackClick,
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // Informations générales
            TrueStayCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
                ) {
                    Text(
                        text = stringResource(R.string.property_form_info_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.black
                    )

                    TrueStayTextField(
                        value = uiState.name,
                        onValueChange = viewModel::setName,
                        label = stringResource(R.string.property_form_name_label),
                        placeholder = stringResource(R.string.property_form_name_placeholder),
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Next
                    )

                    TrueStayTextField(
                        value = uiState.address,
                        onValueChange = viewModel::setAddress,
                        label = stringResource(R.string.property_form_address_label),
                        placeholder = stringResource(R.string.property_form_address_placeholder),
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Next
                    )

                    TrueStayTextField(
                        value = uiState.description,
                        onValueChange = viewModel::setDescription,
                        label = stringResource(R.string.property_form_description_label),
                        placeholder = stringResource(R.string.property_form_description_placeholder),
                        minLines = 5,
                        maxLines = 10,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.None
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
                    ) {
                        TrueStayTextField(
                            value = uiState.monthlyRent,
                            onValueChange = viewModel::setMonthlyRent,
                            label = stringResource(R.string.property_form_rent_label),
                            placeholder = stringResource(R.string.property_form_rent_placeholder),
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                            imeAction = ImeAction.Next
                        )

                        TrueStayTextField(
                            value = uiState.surface,
                            onValueChange = viewModel::setSurface,
                            label = stringResource(R.string.property_form_surface_label),
                            placeholder = stringResource(R.string.property_form_surface_placeholder),
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                            imeAction = ImeAction.Done
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
                        ) {
                            Text(
                                text = stringResource(R.string.property_form_in_building_label),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.black
                            )
                            Text(
                                text = stringResource(R.string.property_form_in_building_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.grayDark
                            )
                        }

                        TrueStaySwitch(
                            checked = uiState.isInBuilding,
                            onCheckedChange = viewModel::setIsInBuilding
                        )
                    }
                }
            }

            // Pièces du logement
            TrueStayCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
                    ) {
                        Text(
                            text = stringResource(R.string.property_form_rooms_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.black
                        )
                        Text(
                            text = stringResource(R.string.property_form_rooms_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.grayDark
                        )
                    }

                    // Liste des pièces
                    uiState.rooms.forEachIndexed { index, room ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = colors.grayLight,
                                thickness = 1.dp
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Pièce ${index + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.black
                                )

                                TrueStayIcon(
                                    iconRes = TrueStayIcons.Trash,
                                    contentDescriptionRes = R.string.property_form_delete_room,
                                    tint = colors.error,
                                    size = 20.dp,
                                    modifier = Modifier.clickable {
                                        viewModel.removeRoom(room.id)
                                    }
                                )
                            }

                            TrueStayTextField(
                                value = room.name,
                                onValueChange = { viewModel.updateRoomName(room.id, it) },
                                label = stringResource(R.string.property_form_room_name_label),
                                placeholder = stringResource(R.string.property_form_room_name_placeholder),
                                modifier = Modifier.fillMaxWidth(),
                                imeAction = ImeAction.Next
                            )

                            val roomTypes = RoomType.entries.toList()
                            val roomTypeLabels = roomTypes.map { stringResource(it.getLabelRes()) }

                            TrueStayDropdown(
                                selectedValue = room.type?.let { stringResource(it.getLabelRes()) },
                                options = roomTypeLabels,
                                onOptionSelected = { selectedLabel ->
                                    val index = roomTypeLabels.indexOf(selectedLabel)
                                    if (index >= 0) {
                                        viewModel.updateRoomType(room.id, roomTypes[index])
                                    }
                                },
                                label = stringResource(R.string.property_form_room_type_label),
                                placeholder = stringResource(R.string.property_form_room_type_placeholder),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    TrueStayButton(
                        text = stringResource(R.string.property_form_add_room),
                        onClick = viewModel::addRoom,
                        variant = ButtonVariant.SECONDARY,
                        leadingIcon = TrueStayIcons.Plus,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Photos
            TrueStayCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
                    ) {
                        Text(
                            text = stringResource(R.string.property_form_photos_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.black
                        )
                        Text(
                            text = stringResource(R.string.property_form_photos_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.grayDark
                        )
                    }

                    val totalPhotos = uiState.photoUris.size

                    // Display photos
                    if (totalPhotos > 0) {
                        PhotoGrid(
                            localUris = uiState.photoUris,
                            uploadedUrls = emptyList(),
                            onDeleteLocalPhoto = { uri -> viewModel.removePhoto(uri) },
                            onDeleteUploadedPhoto = {},
                            onPhotoClick = {}
                        )
                    }

                    // Add photos button
                    val canAddMorePhotos = totalPhotos < 10
                    TrueStayButton(
                        text = stringResource(R.string.property_form_add_photos),
                        onClick = {
                            photoPickerLauncher.launch(10 - totalPhotos)
                        },
                        variant = ButtonVariant.SECONDARY,
                        leadingIcon = TrueStayIcons.Image,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canAddMorePhotos
                    )

                    if (totalPhotos > 0) {
                        Text(
                            text = stringResource(R.string.property_form_photo_count, totalPhotos, 10),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.grayDark
                        )
                    }
                }
            }

            // Disponibilité
            TrueStayCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
                ) {
                    Text(
                        text = stringResource(R.string.property_form_availability_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.black
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
                        ) {
                            Text(
                                text = stringResource(R.string.property_form_available_label),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.black
                            )
                            Text(
                                text = stringResource(R.string.property_form_available_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.grayDark
                            )
                        }

                        TrueStaySwitch(
                            checked = uiState.isAvailable,
                            onCheckedChange = viewModel::setIsAvailable
                        )
                    }
                }
            }

            // Avant de publier
            TrueStayCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    Text(
                        text = stringResource(R.string.property_form_before_publish_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.black
                    )

                    RequirementItem(
                        text = stringResource(R.string.property_form_requirement_name),
                        isFulfilled = uiState.name.isNotBlank()
                    )
                    RequirementItem(
                        text = stringResource(R.string.property_form_requirement_address),
                        isFulfilled = uiState.address.isNotBlank()
                    )
                    RequirementItem(
                        text = stringResource(R.string.property_form_requirement_description),
                        isFulfilled = uiState.description.isNotBlank()
                    )
                    RequirementItem(
                        text = stringResource(R.string.property_form_requirement_room),
                        isFulfilled = uiState.rooms.isNotEmpty() && uiState.rooms.all { it.type != null }
                    )
                    RequirementItem(
                        text = stringResource(R.string.property_form_requirement_photos),
                        isFulfilled = uiState.photoUris.size >= 3
                    )
                    RequirementItem(
                        text = stringResource(R.string.property_form_requirement_complete),
                        isFulfilled = uiState.monthlyRent.isNotBlank() && uiState.surface.isNotBlank()
                    )
                }
            }

            // Error message
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Action buttons
            val isFormValid = uiState.name.isNotBlank() &&
                             uiState.address.isNotBlank() &&
                             uiState.description.isNotBlank() &&
                             uiState.monthlyRent.isNotBlank() &&
                             uiState.surface.isNotBlank() &&
                             uiState.rooms.isNotEmpty() &&
                             uiState.rooms.all { it.type != null } &&
                             uiState.photoUris.size >= 3

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
            ) {
                TrueStayButton(
                    text = stringResource(R.string.property_form_cancel),
                    onClick = onBackClick,
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f)
                )

                TrueStayButton(
                    text = stringResource(R.string.property_form_publish),
                    onClick = { viewModel.submitProperty(onPropertySaved) },
                    variant = ButtonVariant.PRIMARY,
                    modifier = Modifier.weight(1f),
                    enabled = isFormValid && !uiState.isSubmitting,
                    isLoading = uiState.isSubmitting
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.large))
        }
    }
}

@Composable
private fun RequirementItem(
    text: String,
    isFulfilled: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        TrueStayIcon(
            iconRes = if (isFulfilled) TrueStayIcons.Check else TrueStayIcons.Square,
            contentDescriptionRes = null,
            tint = if (isFulfilled) colors.success else colors.grayDark,
            size = 16.dp
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isFulfilled) colors.grayDark else colors.black
        )
    }
}