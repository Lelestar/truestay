package ca.uqac.inf865.truestay.presentation.shared.inventory

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Inventory
import ca.uqac.inf865.truestay.domain.model.InventoryRoom
import ca.uqac.inf865.truestay.domain.model.InventoryStatus
import ca.uqac.inf865.truestay.domain.model.InventoryType
import ca.uqac.inf865.truestay.domain.model.RoomInventoryStatus
import ca.uqac.inf865.truestay.domain.model.Signature
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayBadge
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayProgressBar
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.BadgeVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons.ChevronRight
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import androidx.core.net.toUri
import ca.uqac.inf865.truestay.presentation.navigation.TrueStayTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    inventoryId: String,
    onRoomClick: (String) -> Unit,
    onSignClick: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(inventoryId) {
        viewModel.loadInventory(inventoryId)
    }

    var pdfUrlToView by rememberSaveable { mutableStateOf<String?>(null) }
    var showSignModal by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TrueStayTopAppBar(
                    titleRes = R.string.screen_title_inventory,
                    onNavigateBack = onNavigateBack,
                    windowInsets = WindowInsets(0.dp)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                InventoryScreenContent(
                    uiState = uiState,
                    onRetry = { viewModel.loadInventory(inventoryId) },
                    onRoomClick = onRoomClick,
                    onViewPdf = { url -> pdfUrlToView = url },
                    onShowSignModal = { showSignModal = true }
                )
            }
        }

        if (!pdfUrlToView.isNullOrBlank()) {
            val inventory = uiState.inventory
            val documentTitle = if (inventory != null) {
                stringResource(R.string.inventory_document_download_title, inventory.id)
            } else {
                stringResource(R.string.inventory_document_title)
            }

            PdfOverlay(
                url = pdfUrlToView!!,
                title = documentTitle,
                onClose = { pdfUrlToView = null }
            )
        }

        if (showSignModal) {
            val isLandlord = uiState.currentUserId == uiState.landlordId

            SignConfirmationModal(
                isLandlord = isLandlord,
                onConfirm = {
                    showSignModal = false
                    onSignClick()
                },
                onDismiss = { showSignModal = false }
            )
        }
    }
}

@Composable
private fun InventoryScreenContent(
    uiState: InventoryUiState,
    onRetry: () -> Unit,
    onRoomClick: (String) -> Unit,
    onViewPdf: (String) -> Unit = {},
    onShowSignModal: () -> Unit = {}
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

        uiState.error != null || uiState.inventory == null -> {
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
                        text = stringResource(R.string.inventory_error_loading),
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
            InventoryContent(
                uiState = uiState,
                onRoomClick = onRoomClick,
                onViewPdf = onViewPdf,
                onShowSignModal = onShowSignModal
            )
        }
    }
}

@Composable
private fun InventoryContent(
    uiState: InventoryUiState,
    onRoomClick: (String) -> Unit,
    onViewPdf: (String) -> Unit,
    onShowSignModal: () -> Unit
) {
    val inventory = uiState.inventory ?: return
    val completedRooms = inventory.rooms.count { it.status == RoomInventoryStatus.COMPLETED }
    val totalRooms = inventory.rooms.size.coerceAtLeast(1)
    val colors = LocalAppColors.current
    val context = LocalContext.current

    // Determine if sign button should be shown
    val isLandlord = uiState.currentUserId == uiState.landlordId
    val isTenant = uiState.currentUserId == uiState.tenantId
    val allRoomsCompleted = inventory.rooms.all { it.status == RoomInventoryStatus.COMPLETED }

    val showSignButton = when {
        isLandlord -> allRoomsCompleted && inventory.landlordSignature == null
        isTenant -> inventory.status == InventoryStatus.PENDING_SIGNATURE &&
                    inventory.landlordSignature != null &&
                    inventory.tenantSignature == null
        else -> false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.white
        ) {
            TrueStayProgressBar(
                label = stringResource(R.string.inventory_progress_label),
                currentStep = completedRooms,
                totalSteps = totalRooms,
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            TrueStayCard {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.large)) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                        Text(
                            text = uiState.propertyName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.black
                        )
                        Text(
                            text = uiState.propertyAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.grayDark
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.inventory_type_label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.grayDark
                            )
                            Text(
                                text = stringResource(
                                    when (inventory.type) {
                                        InventoryType.ENTRY -> R.string.inventory_type_entry
                                        InventoryType.EXIT -> R.string.inventory_type_exit
                                    }
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.black
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.inventory_status_label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.grayDark
                            )
                            val (statusText, badgeVariant) = inventory.status.toLabelAndVariant()
                            TrueStayBadge(
                                text = statusText,
                                variant = badgeVariant
                            )
                        }
                    }
                }
            }

            TrueStayCard {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                    Text(
                        text = stringResource(R.string.inventory_signatures_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.black
                        )

                        if (inventory.status == InventoryStatus.DRAFT || inventory.status == InventoryStatus.IN_PROGRESS) {
                            Text(
                                text = stringResource(R.string.inventory_signatures_unavailable),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.grayDark
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                                SignatureRow(
                                    label = stringResource(R.string.inventory_signature_role_landlord),
                                    name = uiState.landlordName,
                                    isSigned = inventory.landlordSignature != null
                                )
                                SignatureRow(
                                    label = stringResource(R.string.inventory_signature_role_tenant),
                                    name = uiState.tenantName,
                                    isSigned = inventory.tenantSignature != null
                                )
                            }
                    }
                }
            }

            TrueStayCard {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                    Text(
                        text = stringResource(R.string.inventory_document_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.black
                    )

                    val pdfUrl = inventory.pdfUrl
                    if (pdfUrl.isNullOrBlank()) {
                        Text(
                            text = stringResource(R.string.inventory_document_unavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.grayDark
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                        ) {
                            TrueStayButton(
                                text = stringResource(R.string.inventory_document_view_pdf),
                                onClick = { onViewPdf(pdfUrl) },
                                modifier = Modifier.weight(1f)
                            )
                            TrueStayButton(
                                text = stringResource(R.string.inventory_document_download),
                                onClick = { downloadPdf(context, pdfUrl, inventory.id) },
                                leadingIcon = TrueStayIcons.Download,
                                variant = ButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            TrueStayCard {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                    Text(
                        text = stringResource(R.string.inventory_rooms_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.black
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                        inventory.rooms.forEach { room ->
                            RoomRow(
                                name = room.roomName,
                                status = room.status,
                                onClick = { onRoomClick(room.roomId) }
                            )
                        }
                    }
                }
            }

            if (showSignButton) {
                TrueStayButton(
                    text = stringResource(R.string.inventory_sign_button),
                    onClick = onShowSignModal,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = TrueStayIcons.PenTool
                )
            }
        }
    }
}

@Composable
private fun SignConfirmationModal(
    isLandlord: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        TrueStayCard(
            modifier = Modifier
                .padding(AppSpacing.large)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    onClick = {}
                )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
            ) {
                Text(
                    text = stringResource(
                        if (isLandlord) R.string.inventory_sign_modal_landlord_title
                        else R.string.inventory_sign_modal_tenant_title
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.black
                )

                Text(
                    text = stringResource(
                        if (isLandlord) R.string.inventory_sign_modal_landlord_message
                        else R.string.inventory_sign_modal_tenant_message
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.grayDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    TrueStayButton(
                        text = stringResource(R.string.inventory_sign_modal_cancel),
                        onClick = onDismiss,
                        variant = ButtonVariant.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )
                    TrueStayButton(
                        text = stringResource(R.string.inventory_sign_modal_confirm),
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun downloadPdf(context: Context, url: String, inventoryId: String) {
    val title = context.getString(R.string.inventory_document_download_title, inventoryId)
    val description = context.getString(R.string.inventory_document_download_description)
    val fileName = context.getString(R.string.inventory_document_filename, inventoryId)

    val request = DownloadManager.Request(url.toUri())
        .setTitle(title)
        .setDescription(description)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            fileName
        )

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
    downloadManager?.enqueue(request)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PdfOverlay(
    url: String,
    title: String,
    onClose: () -> Unit
) {
    val colors = LocalAppColors.current

    var isLoading by rememberSaveable { mutableStateOf(true) }
    var hasError by rememberSaveable { mutableStateOf(false) }
    val webViewHolder = remember { mutableStateOf<WebView?>(null) }

    // Use Google Docs Viewer to display PDF
    val pdfViewerUrl = "https://docs.google.com/gview?embedded=true&url=$url"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.black.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.white)
                    .height(56.dp)
                    .padding(AppSpacing.large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.black
                )

                TrueStayIcon(
                    iconRes = TrueStayIcons.X,
                    contentDescriptionRes = R.string.inventory_document_overlay_close,
                    tint = colors.black,
                    modifier = Modifier.clickable(onClick = onClose)
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.white),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                    hasError = false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    errorCode: Int,
                                    description: String?,
                                    failingUrl: String?
                                ) {
                                    hasError = true
                                    isLoading = false
                                }
                            }
                            loadUrl(pdfViewerUrl)
                            webViewHolder.value = this
                        }
                    }
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.white),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                } else if (hasError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.white)
                            .padding(AppSpacing.large),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
                        ) {
                            Text(
                                text = stringResource(R.string.inventory_document_load_error),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.error,
                                textAlign = TextAlign.Center
                            )
                            TrueStayButton(
                                text = stringResource(R.string.common_retry),
                                onClick = {
                                    isLoading = true
                                    hasError = false
                                    webViewHolder.value?.loadUrl(pdfViewerUrl)
                                },
                                variant = ButtonVariant.SECONDARY
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignatureRow(
    label: String,
    name: String,
    isSigned: Boolean
) {
    val colors = LocalAppColors.current
    val (statusText, badgeVariant) = signatureStatusLabel(isSigned)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.graySurface, AppShapes.medium)
            .padding(AppSpacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = colors.black
            )
            Text(
                text = name.ifBlank { "-" },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grayDark
            )
        }

        TrueStayBadge(
            text = statusText,
            variant = badgeVariant
        )
    }
}

@Composable
private fun RoomRow(
    name: String,
    status: RoomInventoryStatus,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val (statusText, badgeVariant) = roomStatusLabelAndVariant(status)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.medium)
            .background(colors.graySurface)
            .clickable(onClick = onClick)
            .padding(AppSpacing.large),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name.ifBlank { "-" },
            style = MaterialTheme.typography.titleMedium,
            color = colors.black,
            modifier = Modifier.weight(1f)
        )

        TrueStayBadge(
            text = statusText,
            variant = badgeVariant
        )

        TrueStayIcon(
            iconRes = ChevronRight,
            contentDescriptionRes = null,
            tint = colors.grayDefault,
            size = 16.dp
        )
    }
}

@Composable
private fun signatureStatusLabel(isSigned: Boolean): Pair<String, BadgeVariant> {
    return if (isSigned) {
        stringResource(R.string.inventory_signature_status_signed) to BadgeVariant.SUCCESS
    } else {
        stringResource(R.string.inventory_signature_status_missing) to BadgeVariant.NEUTRAL
    }
}

@Composable
private fun InventoryStatus.toLabelAndVariant(): Pair<String, BadgeVariant> {
    return when (this) {
        InventoryStatus.DRAFT -> R.string.rental_inventory_status_draft to BadgeVariant.NEUTRAL
        InventoryStatus.IN_PROGRESS -> R.string.rental_inventory_status_in_progress to BadgeVariant.INFO
        InventoryStatus.PENDING_SIGNATURE -> R.string.rental_inventory_status_pending_signature to BadgeVariant.WARNING
        InventoryStatus.SIGNED -> R.string.rental_inventory_status_signed to BadgeVariant.SUCCESS
        InventoryStatus.COMPLETED -> R.string.rental_inventory_status_completed to BadgeVariant.SUCCESS
        InventoryStatus.CANCELLED -> R.string.rental_inventory_status_cancelled to BadgeVariant.ERROR
    }.let { (textRes, variant) ->
        stringResource(textRes) to variant
    }
}

@Composable
private fun roomStatusLabelAndVariant(status: RoomInventoryStatus): Pair<String, BadgeVariant> {
    val (textRes, variant) = when (status) {
        RoomInventoryStatus.TODO -> R.string.inventory_room_status_todo to BadgeVariant.NEUTRAL
        RoomInventoryStatus.IN_PROGRESS -> R.string.inventory_room_status_in_progress to BadgeVariant.INFO
        RoomInventoryStatus.COMPLETED -> R.string.inventory_room_status_completed to BadgeVariant.SUCCESS
    }
    return stringResource(textRes) to variant
}

// ==========================================
// Previews
// ==========================================
@Preview(name = "Draft - Landlord View", showBackground = true)
@Composable
private fun InventoryDraftLandlordPreview() {
    val sampleInventory = Inventory(
        id = "inv1",
        type = InventoryType.ENTRY,
        rooms = listOf(
            InventoryRoom(roomId = "r1", roomName = "Salon", status = RoomInventoryStatus.TODO),
            InventoryRoom(roomId = "r2", roomName = "Chambre", status = RoomInventoryStatus.TODO),
            InventoryRoom(roomId = "r3", roomName = "Cuisine", status = RoomInventoryStatus.TODO)
        ),
        status = InventoryStatus.DRAFT,
        landlordSignature = null,
        tenantSignature = null
    )

    TrueStayTheme {
        InventoryContent(
            uiState = InventoryUiState(
                inventory = sampleInventory,
                propertyName = "Appartement 3½ moderne",
                propertyAddress = "123 Rue des Pins, Saguenay",
                landlordName = "Paul Tremblay",
                tenantName = "Alice Martin",
                currentUserId = "landlord1",
                landlordId = "landlord1",
                tenantId = "tenant1"
            ),
            onRoomClick = {},
            onViewPdf = {},
            onShowSignModal = {}
        )
    }
}

@Preview(name = "In Progress - Landlord View", showBackground = true)
@Composable
private fun InventoryInProgressLandlordPreview() {
    val sampleInventory = Inventory(
        id = "inv2",
        type = InventoryType.ENTRY,
        rooms = listOf(
            InventoryRoom(roomId = "r1", roomName = "Salon", status = RoomInventoryStatus.COMPLETED),
            InventoryRoom(roomId = "r2", roomName = "Chambre", status = RoomInventoryStatus.IN_PROGRESS),
            InventoryRoom(roomId = "r3", roomName = "Cuisine", status = RoomInventoryStatus.TODO)
        ),
        status = InventoryStatus.IN_PROGRESS,
        landlordSignature = null,
        tenantSignature = null
    )

    TrueStayTheme {
        InventoryContent(
            uiState = InventoryUiState(
                inventory = sampleInventory,
                propertyName = "Beau 3 pièces lumineux",
                propertyAddress = "456 Boulevard Saint-Jean, Chicoutimi",
                landlordName = "Marie Gagnon",
                tenantName = "Jean Dupont",
                currentUserId = "landlord2",
                landlordId = "landlord2",
                tenantId = "tenant2"
            ),
            onRoomClick = {},
            onViewPdf = {},
            onShowSignModal = {}
        )
    }
}

@Preview(name = "All Completed - Landlord Can Sign", showBackground = true)
@Composable
private fun InventoryReadyToSignLandlordPreview() {
    val sampleInventory = Inventory(
        id = "inv3",
        type = InventoryType.ENTRY,
        rooms = listOf(
            InventoryRoom(roomId = "r1", roomName = "Salon", status = RoomInventoryStatus.COMPLETED),
            InventoryRoom(roomId = "r2", roomName = "Chambre", status = RoomInventoryStatus.COMPLETED),
            InventoryRoom(roomId = "r3", roomName = "Salle de bain", status = RoomInventoryStatus.COMPLETED)
        ),
        status = InventoryStatus.IN_PROGRESS,
        landlordSignature = null,
        tenantSignature = null
    )

    TrueStayTheme {
        InventoryContent(
            uiState = InventoryUiState(
                inventory = sampleInventory,
                propertyName = "Studio charmant centre-ville",
                propertyAddress = "789 Rue Racine, Chicoutimi",
                landlordName = "Robert Bouchard",
                tenantName = "Sophie Lavoie",
                currentUserId = "landlord3",
                landlordId = "landlord3",
                tenantId = "tenant3"
            ),
            onRoomClick = {},
            onViewPdf = {},
            onShowSignModal = {}
        )
    }
}

@Preview(name = "Pending Signature - Tenant View (Can Sign)", showBackground = true)
@Composable
private fun InventoryPendingSignatureTenantPreview() {
    val sampleInventory = Inventory(
        id = "inv4",
        type = InventoryType.ENTRY,
        rooms = listOf(
            InventoryRoom(roomId = "r1", roomName = "Salon", status = RoomInventoryStatus.COMPLETED),
            InventoryRoom(roomId = "r2", roomName = "Chambre", status = RoomInventoryStatus.COMPLETED),
            InventoryRoom(roomId = "r3", roomName = "Cuisine", status = RoomInventoryStatus.COMPLETED)
        ),
        status = InventoryStatus.PENDING_SIGNATURE,
        landlordSignature = Signature(
            userId = "landlord4",
            signatureImageUrl = "https://example.com/signature.png",
            signedAt = System.currentTimeMillis()
        ),
        tenantSignature = null
    )

    TrueStayTheme {
        InventoryContent(
            uiState = InventoryUiState(
                inventory = sampleInventory,
                propertyName = "Grand 4½ avec balcon",
                propertyAddress = "321 Avenue des Chênes, Jonquière",
                landlordName = "Claude Simard",
                tenantName = "Isabelle Roy",
                currentUserId = "tenant4",
                landlordId = "landlord4",
                tenantId = "tenant4"
            ),
            onRoomClick = {},
            onViewPdf = {},
            onShowSignModal = {}
        )
    }
}

@Preview(name = "Pending Signature - Landlord View (Already Signed)", showBackground = true)
@Composable
private fun InventoryPendingSignatureLandlordPreview() {
    val sampleInventory = Inventory(
        id = "inv5",
        type = InventoryType.EXIT,
        rooms = listOf(
            InventoryRoom(roomId = "r1", roomName = "Salon", status = RoomInventoryStatus.COMPLETED),
            InventoryRoom(roomId = "r2", roomName = "Chambre", status = RoomInventoryStatus.COMPLETED)
        ),
        status = InventoryStatus.PENDING_SIGNATURE,
        landlordSignature = Signature(
            userId = "landlord5",
            signatureImageUrl = "https://example.com/signature.png",
            signedAt = System.currentTimeMillis()
        ),
        tenantSignature = null,
        pdfUrl = ""
    )

    TrueStayTheme {
        InventoryContent(
            uiState = InventoryUiState(
                inventory = sampleInventory,
                propertyName = "Petit 2½ économique",
                propertyAddress = "654 Rue Price, Chicoutimi",
                landlordName = "François Tremblay",
                tenantName = "Nathalie Fortin",
                currentUserId = "landlord5",
                landlordId = "landlord5",
                tenantId = "tenant5"
            ),
            onRoomClick = {},
            onViewPdf = {},
            onShowSignModal = {}
        )
    }
}

@Preview(name = "Completed - Both Signed", showBackground = true)
@Composable
private fun InventoryCompletedPreview() {
    val sampleInventory = Inventory(
        id = "inv6",
        type = InventoryType.ENTRY,
        rooms = listOf(
            InventoryRoom(roomId = "r1", roomName = "Salon", status = RoomInventoryStatus.COMPLETED),
            InventoryRoom(roomId = "r2", roomName = "Chambre 1", status = RoomInventoryStatus.COMPLETED),
            InventoryRoom(roomId = "r3", roomName = "Chambre 2", status = RoomInventoryStatus.COMPLETED),
            InventoryRoom(roomId = "r4", roomName = "Salle de bain", status = RoomInventoryStatus.COMPLETED)
        ),
        status = InventoryStatus.COMPLETED,
        landlordSignature = Signature(
            userId = "landlord6",
            signatureImageUrl = "https://example.com/landlord_sig.png",
            signedAt = System.currentTimeMillis() - 3600000
        ),
        tenantSignature = Signature(
            userId = "tenant6",
            signatureImageUrl = "https://example.com/tenant_sig.png",
            signedAt = System.currentTimeMillis()
        ),
        pdfUrl = "https://example.com/final_inventory.pdf"
    )

    TrueStayTheme {
        InventoryContent(
            uiState = InventoryUiState(
                inventory = sampleInventory,
                propertyName = "Maison 5½ avec cour",
                propertyAddress = "987 Chemin Saint-Antoine, La Baie",
                landlordName = "André Gauthier",
                tenantName = "Caroline Beaulieu",
                currentUserId = "tenant6",
                landlordId = "landlord6",
                tenantId = "tenant6"
            ),
            onRoomClick = {},
            onViewPdf = {},
            onShowSignModal = {}
        )
    }
}

@Preview(name = "Sign Confirmation Modal - Landlord", showBackground = true)
@Composable
private fun SignConfirmationLandlordPreview() {
    TrueStayTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SignConfirmationModal(
                isLandlord = true,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Sign Confirmation Modal - Tenant", showBackground = true)
@Composable
private fun SignConfirmationTenantPreview() {
    TrueStayTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SignConfirmationModal(
                isLandlord = false,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
