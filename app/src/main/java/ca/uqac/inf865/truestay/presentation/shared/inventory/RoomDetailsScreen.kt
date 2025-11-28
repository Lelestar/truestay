package ca.uqac.inf865.truestay.presentation.shared.inventory

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.ElementCondition
import ca.uqac.inf865.truestay.domain.model.InventoryElement
import ca.uqac.inf865.truestay.domain.model.InventoryRoom
import ca.uqac.inf865.truestay.presentation.common.components.BadgeVariant
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.FullscreenImageCarouselOverlay
import ca.uqac.inf865.truestay.presentation.common.components.PhotoGrid
import ca.uqac.inf865.truestay.presentation.common.components.TextSelectableButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayBadge
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayProgressBar
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.navigation.TrueStayTopAppBar
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import ca.uqac.inf865.truestay.presentation.common.utils.rememberOptimizedCameraLauncher

private const val MAX_PHOTOS = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailsScreen(
    inventoryId: String,
    roomId: String,
    onNavigateBack: () -> Unit,
    viewModel: RoomDetailsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    LaunchedEffect(inventoryId, roomId) {
        viewModel.loadRoomDetails(inventoryId, roomId)
    }

    // Photo capture launcher for room photos
    val cameraLauncher = rememberOptimizedCameraLauncher { optimizedUri ->
        viewModel.addPhoto(optimizedUri)
    }

    // Photo capture launcher for element photos
    var currentElementForPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    val elementCameraLauncher = rememberOptimizedCameraLauncher { optimizedUri ->
        currentElementForPhoto?.let { elementId ->
            viewModel.addElementPhoto(elementId, optimizedUri)
            currentElementForPhoto = null
        }
    }

    // Fullscreen photo overlay state
    var showFullscreenCarousel by rememberSaveable { mutableStateOf(false) }
    var fullscreenStartIndex by remember { mutableIntStateOf(0) }
    var fullscreenPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var fullscreenTitle by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TrueStayTopAppBar(
                    title = uiState.room?.roomName ?: stringResource(R.string.screen_title_room_details),
                    onNavigateBack = onNavigateBack,
                    windowInsets = WindowInsets(0.dp)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                RoomDetailsScreenContent(
                    uiState = uiState,
                    onRetry = { viewModel.retry(inventoryId, roomId) },
                    onTakePhoto = { cameraLauncher.launch() },
                    onDeletePhoto = { photoUrl -> viewModel.deletePhoto(photoUrl) },
                    onPhotoClick = { index ->
                        fullscreenPhotos = uiState.photoUrls
                        fullscreenTitle = uiState.room?.roomName.orEmpty()
                        fullscreenStartIndex = index
                        showFullscreenCarousel = true
                    },
                    onToggleElement = { elementId -> viewModel.toggleElementExpanded(elementId) },
                    onUpdateCondition = { elementId, condition -> viewModel.updateElementCondition(elementId, condition) },
                    onUpdateComment = { elementId, comment -> viewModel.updateElementComment(elementId, comment) },
                    onTakeElementPhoto = { elementId ->
                        currentElementForPhoto = elementId
                        elementCameraLauncher.launch()
                    },
                    onDeleteElementPhoto = { elementId, photoUrl -> viewModel.deleteElementPhoto(elementId, photoUrl) },
                    onElementPhotoClick = { element, index ->
                        fullscreenPhotos = element.photoUrls
                        fullscreenTitle = element.elementName
                        fullscreenStartIndex = index
                        showFullscreenCarousel = true
                    }
                )
            }
        }

        // Fullscreen photo carousel overlay
        if (showFullscreenCarousel && fullscreenPhotos.isNotEmpty()) {
            FullscreenImageCarouselOverlay(
                photos = fullscreenPhotos,
                title = fullscreenTitle,
                initialPage = fullscreenStartIndex,
                onClose = { showFullscreenCarousel = false }
            )
        }
    }
}

@Composable
private fun RoomDetailsScreenContent(
    uiState: RoomDetailsUiState,
    onRetry: () -> Unit,
    onTakePhoto: () -> Unit,
    onDeletePhoto: (String) -> Unit,
    onPhotoClick: (Int) -> Unit,
    onToggleElement: (String) -> Unit,
    onUpdateCondition: (String, ElementCondition) -> Unit,
    onUpdateComment: (String, String) -> Unit,
    onTakeElementPhoto: (String) -> Unit,
    onDeleteElementPhoto: (String, String) -> Unit,
    onElementPhotoClick: (InventoryElement, Int) -> Unit
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LocalAppColors.current.primary)
            }
        }

        uiState.error != null || uiState.room == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.large),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.room_details_error_loading),
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
        }

        else -> {
            RoomDetailsContent(
                uiState = uiState,
                onTakePhoto = onTakePhoto,
                onDeletePhoto = onDeletePhoto,
                onPhotoClick = onPhotoClick,
                onToggleElement = onToggleElement,
                onUpdateCondition = onUpdateCondition,
                onUpdateComment = onUpdateComment,
                onTakeElementPhoto = onTakeElementPhoto,
                onDeleteElementPhoto = onDeleteElementPhoto,
                onElementPhotoClick = onElementPhotoClick
            )
        }
    }
}

@Composable
private fun RoomDetailsContent(
    uiState: RoomDetailsUiState,
    onTakePhoto: () -> Unit,
    onDeletePhoto: (String) -> Unit,
    onPhotoClick: (Int) -> Unit,
    onToggleElement: (String) -> Unit,
    onUpdateCondition: (String, ElementCondition) -> Unit,
    onUpdateComment: (String, String) -> Unit,
    onTakeElementPhoto: (String) -> Unit,
    onDeleteElementPhoto: (String, String) -> Unit,
    onElementPhotoClick: (InventoryElement, Int) -> Unit
) {
    val colors = LocalAppColors.current
    val isTenant = uiState.currentUserId == uiState.tenantId
    val isLandlord = uiState.currentUserId == uiState.landlordId
    val landlordHasSigned = uiState.inventory?.landlordSignature != null

    // Read-only mode: tenant OR landlord who has already signed
    val isReadOnly = isTenant || (isLandlord && landlordHasSigned)
    val canAddPhotos = !isReadOnly && uiState.photoUrls.size < MAX_PHOTOS
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        // Progress bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.white
        ) {
            TrueStayProgressBar(
                label = stringResource(R.string.inventory_progress_label),
                currentStep = uiState.completedElements,
                totalSteps = uiState.totalElements,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val stroke = with(density) { 1.dp.toPx() }
                        val y = size.height - stroke / 2
                        drawLine(
                            color = colors.grayBorder,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = stroke
                        )
                    }
                    .padding(AppSpacing.large)
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // Photos card - hide in read-only mode if no photos
            if (!isReadOnly || uiState.photoUrls.isNotEmpty()) {
                TrueStayCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.large)) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)) {
                            Text(
                                text = stringResource(
                                    if (isReadOnly) R.string.room_details_photos_title
                                    else R.string.room_details_photos_title_optional
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.black
                            )

                            Text(
                                text = stringResource(R.string.room_details_photos_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.grayDark
                            )
                        }

                        // Photo grid
                        if (uiState.photoUrls.isNotEmpty()) {
                            PhotoGrid(
                                photos = uiState.photoUrls,
                                onPhotoClick = onPhotoClick,
                                onDeletePhoto = if (!isReadOnly) onDeletePhoto else null
                            )
                        }

                        // Photo upload error
                        if (uiState.photoUploadError != null) {
                            Text(
                                text = stringResource(R.string.room_details_photo_upload_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Photo delete error
                        if (uiState.photoDeleteError != null) {
                            Text(
                                text = stringResource(R.string.room_details_photo_delete_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Add photo button
                        if (!isReadOnly) {
                            TrueStayButton(
                                text = stringResource(R.string.room_details_add_photo),
                                onClick = onTakePhoto,
                                leadingIcon = TrueStayIcons.Camera,
                                enabled = canAddPhotos && !uiState.isSavingPhoto,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (uiState.photoUrls.size >= MAX_PHOTOS) {
                                Text(
                                    text = stringResource(R.string.room_details_photo_limit_reached),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.grayDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Elements card
            if (uiState.room != null && uiState.room.elements.isNotEmpty()) {
                TrueStayCard(modifier = Modifier.fillMaxWidth(), padding = 0.dp) {
                    Column {
                        // Card title
                        Text(
                            text = stringResource(R.string.room_details_elements_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.black,
                            modifier = Modifier.padding(AppSpacing.large, AppSpacing.large, AppSpacing.large, AppSpacing.medium)
                        )

                        // Elements list
                        uiState.room.elements.forEachIndexed { index, element ->
                            ExpandableElementCard(
                                element = element,
                                isExpanded = uiState.expandedElementId == element.elementId,
                                isLastItem = index == uiState.room.elements.lastIndex,
                                isSavingPhoto = uiState.isSavingPhoto,
                                isTenant = isReadOnly,
                                onToggle = { onToggleElement(element.elementId) },
                                onConditionChange = { condition ->
                                    onUpdateCondition(element.elementId, condition)
                                },
                                onCommentChange = { comment ->
                                    onUpdateComment(element.elementId, comment)
                                },
                                onTakePhoto = {
                                    onTakeElementPhoto(element.elementId)
                                },
                                onDeletePhoto = { photoUrl ->
                                    onDeleteElementPhoto(element.elementId, photoUrl)
                                },
                                onPhotoClick = { index ->
                                    onElementPhotoClick(element, index)
                                },
                                error = uiState.elementErrors[element.elementId]
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Expandable card for an inventory element
 *
 * Displays element information with condition badge and expandable details section.
 * Landlord can modify conditions, add comments and photos. Tenant has read-only access
 * and can only expand elements that have comments or photos.
 *
 * For DAMAGED elements, both comment and photo are required for completion.
 * Elements with TO_CHECK condition cannot have comments or photos added until a condition is set.
 *
 * @param isTenant If true, shows read-only view without edit capabilities
 * @param isLastItem If true, bottom border is not drawn
 */
@Composable
private fun ExpandableElementCard(
    element: InventoryElement,
    isExpanded: Boolean,
    isLastItem: Boolean = false,
    isSavingPhoto: Boolean = false,
    isTenant: Boolean = false,
    onToggle: () -> Unit,
    onConditionChange: (ElementCondition) -> Unit,
    onCommentChange: (String) -> Unit,
    onTakePhoto: () -> Unit,
    onDeletePhoto: (String) -> Unit,
    onPhotoClick: (Int) -> Unit,
    error: String? = null
) {
    val colors = LocalAppColors.current
    var commentText by rememberSaveable { mutableStateOf(element.comment) }

    // Determine if tenant has content to view - tenant can only expand if there's something to see
    val tenantHasContent = element.comment.isNotBlank() || element.photoUrls.isNotEmpty()
    val canExpand = !isTenant || tenantHasContent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isLastItem) {
                    Modifier.drawBehind {
                        val stroke = with(density) { 1.dp.toPx() }
                        val y = size.height - stroke / 2
                        drawLine(
                            color = colors.grayBorder,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = stroke
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        // Clickable header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (canExpand) {
                        Modifier.clickable { onToggle() }
                    } else {
                        Modifier
                    }
                )
                .padding(AppSpacing.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
            ) {
                Text(
                    text = element.elementName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.black
                )

                TrueStayBadge(
                    text = stringResource(
                        when (element.condition) {
                            ElementCondition.GOOD ->
                                R.string.room_details_condition_good
                            ElementCondition.TO_CHECK ->
                                R.string.room_details_condition_to_check
                            ElementCondition.DAMAGED -> {
                                val hasMissingRequirements = element.comment.isBlank() || element.photoUrls.isEmpty()
                                if (hasMissingRequirements) {
                                    R.string.room_details_condition_damaged_incomplete
                                } else {
                                    R.string.room_details_condition_damaged
                                }
                            }
                            ElementCondition.NOT_APPLICABLE ->
                                R.string.room_details_condition_not_applicable
                        }
                    ),
                    variant = when (element.condition) {
                        ElementCondition.GOOD -> BadgeVariant.SUCCESS
                        ElementCondition.TO_CHECK -> BadgeVariant.WARNING
                        ElementCondition.DAMAGED -> BadgeVariant.ERROR
                        ElementCondition.NOT_APPLICABLE -> BadgeVariant.NEUTRAL
                    }
                )
            }

            // Only show chevron if can expand
            if (canExpand) {
                TrueStayIcon(
                    iconRes = if (isExpanded) TrueStayIcons.ChevronUp else TrueStayIcons.ChevronDown,
                    contentDescriptionRes = if (isExpanded)
                        R.string.room_details_collapse_element else
                        R.string.room_details_expand_element,
                    tint = colors.grayDark,
                )
            }
        }

        // Expanded content
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.large, 0.dp, AppSpacing.large, AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
            ) {
                // Condition buttons section - only for landlord
                if (!isTenant) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                    ) {
                        TextSelectableButton(
                            text = stringResource(R.string.room_details_condition_good),
                            selected = element.condition == ElementCondition.GOOD,
                            onClick = { onConditionChange(ElementCondition.GOOD) },
                            modifier = Modifier.weight(1f),
                            centerContent = true
                        )

                        TextSelectableButton(
                            text = stringResource(R.string.room_details_condition_to_check),
                            selected = element.condition == ElementCondition.TO_CHECK,
                            onClick = { onConditionChange(ElementCondition.TO_CHECK) },
                            modifier = Modifier.weight(1f),
                            centerContent = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                    ) {
                        TextSelectableButton(
                            text = stringResource(R.string.room_details_condition_damaged),
                            selected = element.condition == ElementCondition.DAMAGED,
                            onClick = { onConditionChange(ElementCondition.DAMAGED) },
                            modifier = Modifier.weight(1f),
                            centerContent = true
                        )

                        TextSelectableButton(
                            text = stringResource(R.string.room_details_condition_not_applicable),
                            selected = element.condition == ElementCondition.NOT_APPLICABLE,
                            onClick = { onConditionChange(ElementCondition.NOT_APPLICABLE) },
                            modifier = Modifier.weight(1f),
                            centerContent = true
                        )
                    }

                        // Warning message for damaged condition
                        if (element.condition == ElementCondition.DAMAGED) {
                            val hasMissingRequirements = element.comment.isBlank() || element.photoUrls.isEmpty()
                            if (hasMissingRequirements) {
                                Text(
                                    text = stringResource(R.string.room_details_damaged_requirement),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.warning,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Comment section - show only if not empty for tenant
                if (!isTenant || element.comment.isNotBlank()) {
                    TrueStayTextField(
                        value = commentText,
                        onValueChange = {
                            commentText = it
                            onCommentChange(it)
                        },
                        label = stringResource(R.string.room_details_comment_label),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        imeAction = ImeAction.None,
                        enabled = !isTenant && element.condition != ElementCondition.TO_CHECK
                    )
                }

                // Photos section - show only if not empty for tenant or always for landlord
                if (!isTenant || element.photoUrls.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                    ) {
                        Text(
                            text = stringResource(R.string.room_details_element_photos_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.black
                        )

                        if (element.photoUrls.isNotEmpty()) {
                            PhotoGrid(
                                photos = element.photoUrls,
                                onPhotoClick = onPhotoClick,
                                onDeletePhoto = if (!isTenant) { photoUrl -> onDeletePhoto(photoUrl) } else null
                            )
                        }

                        // Add photo button - only for landlord
                        if (!isTenant) {
                            TrueStayButton(
                                text = stringResource(R.string.room_details_add_photo),
                                onClick = onTakePhoto,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = TrueStayIcons.Camera,
                                enabled = element.condition != ElementCondition.TO_CHECK && element.photoUrls.size < MAX_PHOTOS && !isSavingPhoto
                            )

                            if (element.photoUrls.size >= MAX_PHOTOS) {
                                Text(
                                    text = stringResource(R.string.room_details_photo_limit_reached),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.grayDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Error message
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ==========================================
// Previews
// ==========================================

// Preview stub callbacks
private val previewElementCallbacks = object {
    val onToggleElement: (String) -> Unit = {}
    val onUpdateCondition: (String, ElementCondition) -> Unit = { _, _ -> }
    val onUpdateComment: (String, String) -> Unit = { _, _ -> }
    val onTakeElementPhoto: (String) -> Unit = {}
    val onDeleteElementPhoto: (String, String) -> Unit = { _, _ -> }
    val onElementPhotoClick: (InventoryElement, Int) -> Unit = { _, _ -> }
}

@Preview(name = "Landlord View - Mixed Elements", showBackground = true)
@Composable
private fun PreviewRoomDetailsLandlordView() {
    TrueStayTheme {
        RoomDetailsContent(
            uiState = RoomDetailsUiState(
                room = InventoryRoom(
                    roomId = "room1",
                    roomName = "Salon",
                    photoUrls = listOf(
                        "https://picsum.photos/400/400?random=1",
                        "https://picsum.photos/400/400?random=2"
                    ),
                    elements = listOf(
                        InventoryElement(
                            elementId = "elem1",
                            elementName = "Sol",
                            condition = ElementCondition.GOOD,
                            comment = "En bon état",
                            photoUrls = listOf("https://picsum.photos/400/400?random=3")
                        ),
                        InventoryElement(
                            elementId = "elem2",
                            elementName = "Murs",
                            condition = ElementCondition.DAMAGED,
                            comment = "Trous dans le mur près de la porte",
                            photoUrls = listOf(
                                "https://picsum.photos/400/400?random=4",
                                "https://picsum.photos/400/400?random=5"
                            )
                        ),
                        InventoryElement(
                            elementId = "elem3",
                            elementName = "Plafond",
                            condition = ElementCondition.TO_CHECK,
                            comment = "",
                            photoUrls = emptyList()
                        )
                    )
                ),
                currentRoomIndex = 0,
                totalRooms = 5,
                currentUserId = "landlord1",
                landlordId = "landlord1",
                tenantId = "tenant1",
                photoUrls = listOf(
                    "https://picsum.photos/400/400?random=1",
                    "https://picsum.photos/400/400?random=2"
                ),
                completedElements = 1,
                totalElements = 3,
                isLoading = false
            ),
            onTakePhoto = {},
            onDeletePhoto = {},
            onPhotoClick = {},
            onToggleElement = previewElementCallbacks.onToggleElement,
            onUpdateCondition = previewElementCallbacks.onUpdateCondition,
            onUpdateComment = previewElementCallbacks.onUpdateComment,
            onTakeElementPhoto = previewElementCallbacks.onTakeElementPhoto,
            onDeleteElementPhoto = previewElementCallbacks.onDeleteElementPhoto,
            onElementPhotoClick = previewElementCallbacks.onElementPhotoClick
        )
    }
}

@Preview(name = "Tenant View - With Content", showBackground = true)
@Composable
private fun PreviewRoomDetailsTenantView() {
    TrueStayTheme {
        RoomDetailsContent(
            uiState = RoomDetailsUiState(
                room = InventoryRoom(
                    roomId = "room1",
                    roomName = "Chambre principale",
                    photoUrls = listOf(
                        "https://picsum.photos/400/400?random=1",
                        "https://picsum.photos/400/400?random=2"
                    ),
                    elements = listOf(
                        InventoryElement(
                            elementId = "elem1",
                            elementName = "Sol",
                            condition = ElementCondition.GOOD,
                            comment = "En bon état, aucune remarque",
                            photoUrls = listOf("https://picsum.photos/400/400?random=3")
                        ),
                        InventoryElement(
                            elementId = "elem2",
                            elementName = "Murs",
                            condition = ElementCondition.DAMAGED,
                            comment = "Plusieurs trous à réparer",
                            photoUrls = listOf(
                                "https://picsum.photos/400/400?random=4",
                                "https://picsum.photos/400/400?random=5"
                            )
                        ),
                        InventoryElement(
                            elementId = "elem3",
                            elementName = "Fenêtre",
                            condition = ElementCondition.GOOD,
                            comment = "",
                            photoUrls = emptyList()
                        )
                    )
                ),
                currentRoomIndex = 2,
                totalRooms = 5,
                currentUserId = "tenant1",
                landlordId = "landlord1",
                tenantId = "tenant1",
                photoUrls = listOf(
                    "https://picsum.photos/400/400?random=1",
                    "https://picsum.photos/400/400?random=2"
                ),
                completedElements = 2,
                totalElements = 3,
                isLoading = false
            ),
            onTakePhoto = {},
            onDeletePhoto = {},
            onPhotoClick = {},
            onToggleElement = previewElementCallbacks.onToggleElement,
            onUpdateCondition = previewElementCallbacks.onUpdateCondition,
            onUpdateComment = previewElementCallbacks.onUpdateComment,
            onTakeElementPhoto = previewElementCallbacks.onTakeElementPhoto,
            onDeleteElementPhoto = previewElementCallbacks.onDeleteElementPhoto,
            onElementPhotoClick = previewElementCallbacks.onElementPhotoClick
        )
    }
}

@Preview(name = "Max 5 Photos (+n indicator)", showBackground = true)
@Composable
private fun PreviewRoomDetailsMaxPhotos() {
    TrueStayTheme {
        RoomDetailsContent(
            uiState = RoomDetailsUiState(
                room = InventoryRoom(
                    roomId = "room1",
                    roomName = "Cuisine",
                    photoUrls = List(5) { "https://picsum.photos/400/400?random=$it" },
                    elements = listOf(
                        InventoryElement(
                            elementId = "elem1",
                            elementName = "Sol",
                            condition = ElementCondition.GOOD,
                            comment = "Carrelage en bon état",
                            photoUrls = List(5) { "https://picsum.photos/400/400?random=${it + 10}" }
                        ),
                        InventoryElement(
                            elementId = "elem2",
                            elementName = "Électroménager",
                            condition = ElementCondition.DAMAGED,
                            comment = "Four rayé, plaques de cuisson fonctionnelles",
                            photoUrls = List(5) { "https://picsum.photos/400/400?random=${it + 20}" }
                        )
                    )
                ),
                currentRoomIndex = 1,
                totalRooms = 5,
                currentUserId = "landlord1",
                landlordId = "landlord1",
                tenantId = "tenant1",
                photoUrls = List(5) { "https://picsum.photos/400/400?random=$it" },
                isLoading = false,
                completedElements = 2,
                totalElements = 2
            ),
            onTakePhoto = {},
            onDeletePhoto = {},
            onPhotoClick = {},
            onToggleElement = previewElementCallbacks.onToggleElement,
            onUpdateCondition = previewElementCallbacks.onUpdateCondition,
            onUpdateComment = previewElementCallbacks.onUpdateComment,
            onTakeElementPhoto = previewElementCallbacks.onTakeElementPhoto,
            onDeleteElementPhoto = previewElementCallbacks.onDeleteElementPhoto,
            onElementPhotoClick = previewElementCallbacks.onElementPhotoClick
        )
    }
}

@Preview(name = "Tenant View - With Content", showBackground = true)
@Composable
private fun PreviewRoomDetailsTenantWithContent() {
    TrueStayTheme {
        RoomDetailsContent(
            uiState = RoomDetailsUiState(
                room = InventoryRoom(
                    roomId = "room1",
                    roomName = "Salle de bain",
                    photoUrls = listOf(
                        "https://picsum.photos/400/400?random=1",
                        "https://picsum.photos/400/400?random=2"
                    ),
                    elements = listOf(
                        InventoryElement(
                            elementId = "elem1",
                            elementName = "Lavabo",
                            condition = ElementCondition.GOOD,
                            comment = "Propre et fonctionnel",
                            photoUrls = listOf("https://picsum.photos/400/400?random=10")
                        ),
                        InventoryElement(
                            elementId = "elem2",
                            elementName = "Douche",
                            condition = ElementCondition.DAMAGED,
                            comment = "Joint moisi dans le coin",
                            photoUrls = listOf(
                                "https://picsum.photos/400/400?random=11",
                                "https://picsum.photos/400/400?random=12"
                            )
                        )
                    )
                ),
                currentRoomIndex = 3,
                totalRooms = 5,
                currentUserId = "tenant1",
                landlordId = "landlord1",
                tenantId = "tenant1",
                photoUrls = listOf(
                    "https://picsum.photos/400/400?random=1",
                    "https://picsum.photos/400/400?random=2"
                ),
                isLoading = false,
                completedElements = 2,
                totalElements = 2
            ),
            onTakePhoto = {},
            onDeletePhoto = {},
            onPhotoClick = {},
            onToggleElement = previewElementCallbacks.onToggleElement,
            onUpdateCondition = previewElementCallbacks.onUpdateCondition,
            onUpdateComment = previewElementCallbacks.onUpdateComment,
            onTakeElementPhoto = previewElementCallbacks.onTakeElementPhoto,
            onDeleteElementPhoto = previewElementCallbacks.onDeleteElementPhoto,
            onElementPhotoClick = previewElementCallbacks.onElementPhotoClick
        )
    }
}

@Preview(name = "Tenant View - No Content (Collapsed Elements)", showBackground = true)
@Composable
private fun PreviewRoomDetailsTenantNoContent() {
    TrueStayTheme {
        RoomDetailsContent(
            uiState = RoomDetailsUiState(
                room = InventoryRoom(
                    roomId = "room1",
                    roomName = "Couloir",
                    photoUrls = emptyList(),
                    elements = listOf(
                        InventoryElement(
                            elementId = "elem1",
                            elementName = "Sol",
                            condition = ElementCondition.GOOD,
                            comment = "",
                            photoUrls = emptyList()
                        ),
                        InventoryElement(
                            elementId = "elem2",
                            elementName = "Murs",
                            condition = ElementCondition.TO_CHECK,
                            comment = "",
                            photoUrls = emptyList()
                        ),
                        InventoryElement(
                            elementId = "elem3",
                            elementName = "Éclairage",
                            condition = ElementCondition.NOT_APPLICABLE,
                            comment = "",
                            photoUrls = emptyList()
                        )
                    )
                ),
                currentRoomIndex = 4,
                totalRooms = 5,
                currentUserId = "tenant1",
                landlordId = "landlord1",
                tenantId = "tenant1",
                photoUrls = emptyList(),
                isLoading = false,
                completedElements = 2,
                totalElements = 3
            ),
            onTakePhoto = {},
            onDeletePhoto = {},
            onPhotoClick = {},
            onToggleElement = previewElementCallbacks.onToggleElement,
            onUpdateCondition = previewElementCallbacks.onUpdateCondition,
            onUpdateComment = previewElementCallbacks.onUpdateComment,
            onTakeElementPhoto = previewElementCallbacks.onTakeElementPhoto,
            onDeleteElementPhoto = previewElementCallbacks.onDeleteElementPhoto,
            onElementPhotoClick = previewElementCallbacks.onElementPhotoClick
        )
    }
}

@Preview(name = "Upload Error", showBackground = true)
@Composable
private fun PreviewRoomDetailsUploadError() {
    TrueStayTheme {
        RoomDetailsContent(
            uiState = RoomDetailsUiState(
                room = InventoryRoom(
                    roomId = "room1",
                    roomName = "Bureau",
                    photoUrls = listOf("https://picsum.photos/400/400?random=1")
                ),
                currentRoomIndex = 1,
                totalRooms = 5,
                currentUserId = "landlord1",
                landlordId = "landlord1",
                tenantId = "tenant1",
                photoUrls = listOf("https://picsum.photos/400/400?random=1"),
                isLoading = false,
                photoUploadError = "Erreur lors de l'ajout de la photo"
            ),
            onTakePhoto = {},
            onDeletePhoto = {},
            onPhotoClick = {},
            onToggleElement = previewElementCallbacks.onToggleElement,
            onUpdateCondition = previewElementCallbacks.onUpdateCondition,
            onUpdateComment = previewElementCallbacks.onUpdateComment,
            onTakeElementPhoto = previewElementCallbacks.onTakeElementPhoto,
            onDeleteElementPhoto = previewElementCallbacks.onDeleteElementPhoto,
            onElementPhotoClick = previewElementCallbacks.onElementPhotoClick
        )
    }
}

@Preview(name = "Delete Error", showBackground = true)
@Composable
private fun PreviewRoomDetailsDeleteError() {
    TrueStayTheme {
        RoomDetailsContent(
            uiState = RoomDetailsUiState(
                room = InventoryRoom(
                    roomId = "room1",
                    roomName = "Terrasse",
                    photoUrls = listOf(
                        "https://picsum.photos/400/400?random=1",
                        "https://picsum.photos/400/400?random=2"
                    )
                ),
                currentRoomIndex = 0,
                totalRooms = 5,
                currentUserId = "landlord1",
                landlordId = "landlord1",
                tenantId = "tenant1",
                photoUrls = listOf(
                    "https://picsum.photos/400/400?random=1",
                    "https://picsum.photos/400/400?random=2"
                ),
                isLoading = false,
                photoDeleteError = "Erreur lors de la suppression de la photo"
            ),
            onTakePhoto = {},
            onDeletePhoto = {},
            onPhotoClick = {},
            onToggleElement = previewElementCallbacks.onToggleElement,
            onUpdateCondition = previewElementCallbacks.onUpdateCondition,
            onUpdateComment = previewElementCallbacks.onUpdateComment,
            onTakeElementPhoto = previewElementCallbacks.onTakeElementPhoto,
            onDeleteElementPhoto = previewElementCallbacks.onDeleteElementPhoto,
            onElementPhotoClick = previewElementCallbacks.onElementPhotoClick
        )
    }
}

@Preview(name = "Saving Photo State", showBackground = true)
@Composable
private fun PreviewRoomDetailsSaving() {
    TrueStayTheme {
        RoomDetailsContent(
            uiState = RoomDetailsUiState(
                room = InventoryRoom(
                    roomId = "room1",
                    roomName = "Salon",
                    photoUrls = listOf("https://picsum.photos/400/400?random=1")
                ),
                currentRoomIndex = 2,
                totalRooms = 5,
                currentUserId = "landlord1",
                landlordId = "landlord1",
                tenantId = "tenant1",
                photoUrls = listOf("https://picsum.photos/400/400?random=1"),
                isLoading = false,
                isSavingPhoto = true
            ),
            onTakePhoto = {},
            onDeletePhoto = {},
            onPhotoClick = {},
            onToggleElement = previewElementCallbacks.onToggleElement,
            onUpdateCondition = previewElementCallbacks.onUpdateCondition,
            onUpdateComment = previewElementCallbacks.onUpdateComment,
            onTakeElementPhoto = previewElementCallbacks.onTakeElementPhoto,
            onDeleteElementPhoto = previewElementCallbacks.onDeleteElementPhoto,
            onElementPhotoClick = previewElementCallbacks.onElementPhotoClick
        )
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
private fun PreviewRoomDetailsLoading() {
    TrueStayTheme {
        RoomDetailsScreenContent(
            uiState = RoomDetailsUiState(
                isLoading = true
            ),
            onRetry = {},
            onTakePhoto = {},
            onDeletePhoto = {},
            onPhotoClick = {},
            onToggleElement = previewElementCallbacks.onToggleElement,
            onUpdateCondition = previewElementCallbacks.onUpdateCondition,
            onUpdateComment = previewElementCallbacks.onUpdateComment,
            onTakeElementPhoto = previewElementCallbacks.onTakeElementPhoto,
            onDeleteElementPhoto = previewElementCallbacks.onDeleteElementPhoto,
            onElementPhotoClick = previewElementCallbacks.onElementPhotoClick
        )
    }
}

@Preview(name = "Error State", showBackground = true)
@Composable
private fun PreviewRoomDetailsError() {
    TrueStayTheme {
        RoomDetailsScreenContent(
            uiState = RoomDetailsUiState(
                isLoading = false,
                error = RoomDetailsError.LOAD_FAILED
            ),
            onRetry = {},
            onTakePhoto = {},
            onDeletePhoto = {},
            onPhotoClick = {},
            onToggleElement = previewElementCallbacks.onToggleElement,
            onUpdateCondition = previewElementCallbacks.onUpdateCondition,
            onUpdateComment = previewElementCallbacks.onUpdateComment,
            onTakeElementPhoto = previewElementCallbacks.onTakeElementPhoto,
            onDeleteElementPhoto = previewElementCallbacks.onDeleteElementPhoto,
            onElementPhotoClick = previewElementCallbacks.onElementPhotoClick
        )
    }
}
