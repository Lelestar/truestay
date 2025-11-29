package ca.uqac.inf865.truestay.presentation.shared.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.Inventory
import ca.uqac.inf865.truestay.domain.model.InventoryType
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.User
import ca.uqac.inf865.truestay.domain.model.UserRole
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.SignatureCanvas
import ca.uqac.inf865.truestay.presentation.common.components.SignatureController
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.utils.DateUtils
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureScreen(
    inventoryId: String,
    onBackClick: () -> Unit,
    onSignatureSubmitted: () -> Unit,
    viewModel: SignatureViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val signatureController = remember { SignatureController() }

    LaunchedEffect(inventoryId) {
        viewModel.loadData()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                LoadingState()
            }
            uiState.error != null -> {
                ErrorState(
                    error = stringResource(R.string.signature_load_error),
                    onRetry = { viewModel.loadData() }
                )
            }
            uiState.inventory != null && uiState.property != null -> {
                SignatureContent(
                    uiState = uiState,
                    signatureController = signatureController,
                    onClearSignature = { signatureController.clear() },
                    onSign = {
                        val bitmap = signatureController.capture()
                        if (bitmap != null) {
                            viewModel.submitSignature(bitmap, onSignatureSubmitted)
                        }
                    },
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = LocalAppColors.current.primary)
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
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
                text = error,
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

@Composable
private fun SignatureContent(
    uiState: SignatureUiState,
    signatureController: SignatureController,
    onClearSignature: () -> Unit,
    onSign: () -> Unit,
    onBackClick: () -> Unit
) {
    val property = uiState.property!!
    val inventory = uiState.inventory!!
    val currentUser = uiState.currentUser
    val colors = LocalAppColors.current
    val hasSignature = signatureController.hasSignature

    val isLandlord = currentUser?.role == UserRole.LANDLORD
    val roleTitle = if (isLandlord) stringResource(R.string.role_landlord) else stringResource(R.string.role_tenant)
    val signerName = "${currentUser?.firstName.orEmpty()} ${currentUser?.lastName.orEmpty()}".trim()

    val todayDate = DateUtils.formatLocalDate(System.currentTimeMillis()) ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        // 1. Info Card (Inventory/Property details)
        TrueStayCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.large)) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    Text(
                        text = property.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.black
                    )
                    Text(
                        text = "${property.address.street}, ${property.address.city}",
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
                            text = stringResource(R.string.signature_date_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.grayDark
                        )
                        Text(
                            text = todayDate,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.black
                        )
                    }
                }
            }
        }

        // 2. Signature Card
        TrueStayCard {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.large)) {
                // Header: Role and Name
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    Text(
                        text = roleTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.black
                    )
                    Text(
                        text = signerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.grayDark
                    )
                }

                // Canvas Section
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    Text(
                        text = stringResource(R.string.signature_pad_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.black
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .drawBehind {
                                val strokeWidth = 3.dp.toPx()
                                val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                                drawRoundRect(
                                    color = colors.grayBorder,
                                    size = size,
                                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                    style = Stroke(width = strokeWidth, pathEffect = dash)
                                )
                            }
                            .clip(AppShapes.medium)
                    ) {
                        SignatureCanvas(
                            modifier = Modifier.fillMaxSize(),
                            controller = signatureController
                        )
                    }

                    Text(
                        text = stringResource(R.string.signature_pad_helper),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.grayDark,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                TrueStayButton(
                    text = stringResource(R.string.signature_clear_button),
                    onClick = onClearSignature,
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.align(Alignment.End),
                    leadingIcon = TrueStayIcons.Eraser
                )
            }
        }

        // 3. Disclaimer Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = colors.warningSurface,
            border = BorderStroke(1.dp, colors.warning)
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
                        iconRes = TrueStayIcons.Scale,
                        contentDescriptionRes = null,
                        tint = colors.onWarningSurface,
                        size = 20.dp
                    )

                    Text(
                        text = stringResource(R.string.signature_disclaimer_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onWarningSurface
                    )
                }

                Text(
                    text = stringResource(R.string.signature_disclaimer_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onWarningSurface
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Submit error
        if (uiState.submitError != null) {
            Text(
                text = stringResource(R.string.signature_submit_error),
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // 4. Action Buttons
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
                text = stringResource(R.string.inventory_sign_button),
                onClick = onSign,
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.weight(1f),
                isLoading = uiState.isSubmitting,
                enabled = hasSignature && !uiState.isSubmitting
            )
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true, name = "Signature Content")
@Composable
private fun SignatureContentPreview() {
    TrueStayTheme {
        SignatureContent(
            uiState = SignatureUiState(
                inventory = PreviewInventory,
                property = PreviewProperty,
                currentUser = PreviewUser
            ),
            signatureController = remember { SignatureController() },
            onClearSignature = {},
            onSign = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun SignatureLoadingPreview() {
    TrueStayTheme {
        LoadingState()
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun SignatureErrorPreview() {
    TrueStayTheme {
        ErrorState(
            error = stringResource(R.string.signature_load_error),
            onRetry = {}
        )
    }
}

private val PreviewProperty = Property(
    id = "property1",
    name = "Appartement lumineux",
    address = Address(
        street = "123 Rue Principale",
        city = "Montréal",
        province = "QC",
        postalCode = "H2X 1Y4",
        country = "Canada"
    )
)

private val PreviewUser = User(
    id = "tenant1",
    firstName = "Louis",
    lastName = "Roy",
    role = UserRole.TENANT
)

private val PreviewInventory = Inventory(
    id = "inventory1",
    rentalId = "rental1",
    type = InventoryType.ENTRY
)
