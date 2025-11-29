package ca.uqac.inf865.truestay.presentation.landlord.rental

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import androidx.compose.ui.Alignment
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.ui.tooling.preview.Preview
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalFocusManager


@Composable
fun CreateRentalScreen(
    propertyId: String,
    onBackClick: () -> Unit,
    onRentalCreated: () -> Unit,
    viewModel: CreateRentalViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.value

    LaunchedEffect(propertyId) {
        viewModel.loadProperty(propertyId)
    }

    CreateRentalContent(
        uiState = state,
        onEmailChange = viewModel::updateTenantEmail,
        onStartDateChange = viewModel::updateStartDate,
        onEndDateChange = viewModel::updateEndDate,
        onDismissError = viewModel::dismissError,
        onRetry = { viewModel.loadProperty(propertyId) },
        onCreateRental = {
            viewModel.createRental(
                propertyId = propertyId,
                onSuccess = onRentalCreated
            )
        }
    )
}

@Composable
private fun CreateRentalContent(
    uiState: CreateRentalUiState,
    onEmailChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onDismissError: () -> Unit,
    onRetry: () -> Unit,
    onCreateRental: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Prepare localized error messages
    val tenantNotFoundMsg = stringResource(R.string.create_rental_error_tenant_not_found)
    val tenantHasActiveRentalMsg = stringResource(R.string.create_rental_error_tenant_already_has_active_rental)
    val landlordNotIdMsg = stringResource(R.string.create_rental_error_landlord_not_identified)
    val createErrorTemplate = stringResource(R.string.create_rental_error)

    val displayError = when {
        uiState.errorMessage == "TENANT_NOT_FOUND" -> tenantNotFoundMsg
        uiState.errorMessage == "TENANT_ALREADY_HAS_ACTIVE_RENTAL" -> tenantHasActiveRentalMsg
        uiState.errorMessage == "LANDLORD_NOT_IDENTIFIED" -> landlordNotIdMsg
        uiState.errorMessage?.startsWith("CREATE_RENTAL_ERROR|") == true -> {
            val error = uiState.errorMessage.substringAfter("|")
            createErrorTemplate.format(error)
        }
        else -> uiState.errorMessage
    }

    when {
        // Loading state
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }

        // Error state
        uiState.error != null && uiState.property == null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.create_rental_load_error),
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalAppColors.current.error,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(AppSpacing.medium))
                TrueStayButton(
                    text = stringResource(R.string.common_retry),
                    onClick = onRetry
                )
            }
        }

        // Content state
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                    }
            ) {

                // Scrollable content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(AppSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
                ) {

                    uiState.property?.let { property ->
                        TrueStayCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    property.name,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(AppSpacing.small))
                                Row {
                                    TrueStayIcon(
                                        iconRes = TrueStayIcons.MapPin,
                                        contentDescriptionRes = null,
                                        tint = LocalAppColors.current.grayDark,
                                        size = 16.dp
                                    )
                                    Text(
                                        " ${property.address.street}, ${property.address.postalCode} ${property.address.city}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    RentalInfoCard(
                        email = uiState.tenantEmail,
                        onEmailChange = onEmailChange,
                        startDate = uiState.startDate,
                        onStartDateChange = onStartDateChange,
                        endDate = uiState.endDate,
                        onEndDateChange = onEndDateChange
                    )

                    ReviewGuidelinesCard()

                    // Error message display
                    displayError?.let { error ->
                        TrueStayCard(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LocalAppColors.current.error,
                                    modifier = Modifier.weight(1f)
                                )
                                androidx.compose.material3.IconButton(onClick = onDismissError) {
                                    TrueStayIcon(
                                        iconRes = TrueStayIcons.X,
                                        contentDescriptionRes = null,
                                        tint = LocalAppColors.current.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Fixed button at bottom
                TrueStayButton(
                    text = stringResource(R.string.rental_button_create),
                    isLoading = uiState.isSubmitting,
                    enabled = !uiState.isSubmitting &&
                              uiState.tenantEmail.isNotBlank() &&
                              uiState.endDate.isNotBlank(),
                    onClick = onCreateRental,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.large)
                )
            }
        }
    }
}

@Composable
fun RentalInfoCard(
    email: String,
    onEmailChange: (String) -> Unit,
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit
) {
    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }

    // Parse start date to get minimum date for end date picker
    val startDateMillis = remember(startDate) {
        if (startDate.isNotBlank()) {
            try {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatter.parse(startDate)?.time
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startDateMillis,
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Only allow dates >= start date
                return startDateMillis?.let { utcTimeMillis >= it } ?: true
            }
        }
    )

    if (showStartDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = startDatePickerState.selectedDateMillis
                        if (millis != null) {
                            val formatted = formatDate(millis)
                            onStartDateChange(formatted)
                        }
                        showStartDatePicker.value = false
                    }
                ) {
                    Text("OK")
                }
            },
            shape = AppShapes.large,
            colors = DatePickerDefaults.colors(
                containerColor = LocalAppColors.current.white,
                headlineContentColor = LocalAppColors.current.black,
                weekdayContentColor = LocalAppColors.current.grayDark,
                subheadContentColor = LocalAppColors.current.black,
                yearContentColor = LocalAppColors.current.black,
                currentYearContentColor = LocalAppColors.current.primary,
                selectedYearContentColor = LocalAppColors.current.white,
                selectedYearContainerColor = LocalAppColors.current.primary,
                dayContentColor = LocalAppColors.current.black,
                selectedDayContentColor = LocalAppColors.current.white,
                selectedDayContainerColor = LocalAppColors.current.primary,
                todayContentColor = LocalAppColors.current.primary,
                todayDateBorderColor = LocalAppColors.current.primary
            )
        ) {
            DatePicker(
                state = startDatePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = LocalAppColors.current.white
                )
            )
        }
    }

    if (showEndDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = endDatePickerState.selectedDateMillis
                        if (millis != null) {
                            // Validate that end date is not before start date
                            if (startDateMillis != null && millis < startDateMillis) {
                                // Don't accept the date, show error or just do nothing
                                // For now, we just prevent setting it
                            } else {
                                val formatted = formatDate(millis)
                                onEndDateChange(formatted)
                                showEndDatePicker.value = false
                            }
                        } else {
                            showEndDatePicker.value = false
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            shape = AppShapes.large,
            colors = DatePickerDefaults.colors(
                containerColor = LocalAppColors.current.white,
                headlineContentColor = LocalAppColors.current.black,
                weekdayContentColor = LocalAppColors.current.grayDark,
                subheadContentColor = LocalAppColors.current.black,
                yearContentColor = LocalAppColors.current.black,
                currentYearContentColor = LocalAppColors.current.primary,
                selectedYearContentColor = LocalAppColors.current.white,
                selectedYearContainerColor = LocalAppColors.current.primary,
                dayContentColor = LocalAppColors.current.black,
                selectedDayContentColor = LocalAppColors.current.white,
                selectedDayContainerColor = LocalAppColors.current.primary,
                todayContentColor = LocalAppColors.current.primary,
                todayDateBorderColor = LocalAppColors.current.primary
            )
        ) {
            DatePicker(
                state = endDatePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = LocalAppColors.current.white
                )
            )
        }
    }
    TrueStayCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {

            // Title
            Text(
                text = stringResource(R.string.rental_info_title),
                style = MaterialTheme.typography.headlineSmall,
                color = LocalAppColors.current.black
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                // Email
                TrueStayTextField(
                    label = stringResource(R.string.rental_email_label),
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = stringResource(R.string.rental_email_placeholder),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(R.string.rental_info_subtext),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalAppColors.current.grayDark
                )
            }

            // Dates
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.large)
                ) {
                    // Start date
                    Column(modifier = Modifier.weight(1f)) {
                        TrueStayTextField(
                            label = stringResource(R.string.rental_start_date_label),
                            value = startDate,
                            onValueChange = { },
                            placeholder = stringResource(R.string.rental_date_placeholder),
                            trailingIcon = TrueStayIcons.Calendar,
                            readOnly = true,
                            enabled = false,
                            onTrailingIconClick = { }
                        )
                    }

                    // End date
                    Column(modifier = Modifier.weight(1f)) {
                        TrueStayTextField(
                            label = stringResource(R.string.rental_end_date_label),
                            value = endDate,
                            onValueChange = { },
                            placeholder = stringResource(R.string.rental_date_placeholder),
                            trailingIcon = TrueStayIcons.Calendar,
                            readOnly = true,
                            onTrailingIconClick = {
                                showEndDatePicker.value = true
                            }
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.rental_start_date_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalAppColors.current.grayDark
                )
            }
        }
    }
}

@Composable
fun ReviewGuidelinesCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.large)
            .background(LocalAppColors.current.infoSurface)
            .border(
                width = 1.dp,
                color = LocalAppColors.current.info,
                shape = AppShapes.large
            )
            .padding(AppSpacing.large)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().background(LocalAppColors.current.infoSurface),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TrueStayIcon(
                    iconRes = TrueStayIcons.CircleAlert,
                    contentDescriptionRes = null,
                    tint = LocalAppColors.current.onInfoSurface,
                    size = 20.dp
                )
                Spacer(modifier = Modifier.width(AppSpacing.small))
                Text(
                    text = stringResource(R.string.rental_guideline_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalAppColors.current.onInfoSurface,
                )
            }
            Text(
                text = stringResource(R.string.rental_guideline_text1),
                color = LocalAppColors.current.onInfoSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true)
@Composable
private fun CreateRentalLoadingPreview() {
    TrueStayTheme {
        CreateRentalContent(
            uiState = CreateRentalUiState(
                isLoading = true,
                property = null,
                error = null
            ),
            onEmailChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onDismissError = {},
            onRetry = {},
            onCreateRental = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateRentalErrorPreview() {
    TrueStayTheme {
        CreateRentalContent(
            uiState = CreateRentalUiState(
                isLoading = false,
                property = null,
                error = "Failed to load property"
            ),
            onEmailChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onDismissError = {},
            onRetry = {},
            onCreateRental = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateRentalEmptyFormPreview() {
    TrueStayTheme {
        CreateRentalContent(
            uiState = CreateRentalUiState(
                isLoading = false,
                property = Property(
                    id = "1",
                    name = "Appartement Moderne Centre-Ville",
                    description = "Superbe appartement rénové",
                    address = Address(
                        street = "123 Rue Principale",
                        city = "Montréal",
                        postalCode = "H1A 1A1",
                        country = "Canada"
                    ),
                    photos = emptyList(),
                    landlordId = "landlord1",
                    monthlyRent = 1500,
                    surface = 75
                ),
                error = null
            ),
            onEmailChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onDismissError = {},
            onRetry = {},
            onCreateRental = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun CreateRentalWithErrorPreview() {
    TrueStayTheme {
        CreateRentalContent(
            uiState = CreateRentalUiState(
                isLoading = false,
                property = Property(
                    id = "1",
                    name = "Appartement Moderne Centre-Ville",
                    description = "Superbe appartement rénové",
                    address = Address(
                        street = "123 Rue Principale",
                        city = "Montréal",
                        postalCode = "H1A 1A1",
                        country = "Canada"
                    ),
                    photos = emptyList(),
                    landlordId = "landlord1",
                    monthlyRent = 1500,
                    surface = 75
                ),
                error = null,
                tenantEmail = "",
                startDate = "",
                endDate = "",
                errorMessage = "TENANT_NOT_FOUND",
                isSubmitting = false
            ),
            onEmailChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onDismissError = {},
            onRetry = {},
            onCreateRental = {}
        )
    }
}

