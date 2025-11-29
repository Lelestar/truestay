package ca.uqac.inf865.truestay.presentation.landlord.rental

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Inventory
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.shared.rental.InventorySummaryCard
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun CreateRentalScreen(
    propertyId: String,
    onBackClick: () -> Unit,
    onRentalCreated: () -> Unit,
    viewModel: CreateRentalViewModel = hiltViewModel()
) {

    val state = viewModel.uiState.value
    var tenantEmail = remember { mutableStateOf("") }
    var startDate = remember { mutableStateOf("") }
    var endDate = remember { mutableStateOf("") }

    LaunchedEffect(propertyId) {
        viewModel.loadProperty(propertyId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // 🔹 ZONE SCROLLABLE
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {

            state.property?.let { property ->
                TrueStayCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(property.name, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row {
                            TrueStayIcon(
                                iconRes = TrueStayIcons.MapPin,
                                contentDescriptionRes = null,
                                tint = LocalAppColors.current.grayDark,
                                size = 16.dp
                            )
                            Text(
                                " ${property.address.street}, ${property.address.postalCode} ${property.address.city}",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            RentalInfoCard(
                email = tenantEmail.value,
                onEmailChange = { tenantEmail.value = it },
                startDate = startDate.value,
                onStartDateChange = { startDate.value = it },
                endDate = endDate.value,
                onEndDateChange = { endDate.value = it }
            )

            ReviewGuidelinesCard()
        }

        // 🔹 BOUTON FIXÉ EN BAS
        TrueStayButton(
            text = stringResource(R.string.rental_button_create),
            onClick = {
                viewModel.createRental(
                    propertyId = propertyId,
                    tenantEmail = tenantEmail.value,
                    startDate = startDate.value,
                    endDate = endDate.value,
                    onSuccess = onRentalCreated,
                    onError = { message ->
                        println("Erreur lors de la création : $message")
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.large)
        )
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
    var showStartDatePicker = remember { mutableStateOf(false) }
    var showEndDatePicker = remember { mutableStateOf(false) }


    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

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
            }
        ) {
            DatePicker(state = startDatePickerState)
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
                            val formatted = formatDate(millis)
                            onEndDateChange(formatted)
                        }
                        showEndDatePicker.value = false
                    }
                ) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
    TrueStayCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {

            // Titre
            Text(
                text = stringResource(R.string.rental_info_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            // Label
            Text(
                text = stringResource(R.string.rental_email_label),
                fontWeight = FontWeight.SemiBold
            )

            // Champ Email
            TrueStayTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.rental_email_placeholder),
                modifier = Modifier.fillMaxWidth()
            )

            // Texte d’aide
            Text(
                text = stringResource(R.string.rental_info_subtext),
                fontSize = 12.sp,
                color = LocalAppColors.current.grayDark
            )

            // Dates côte à côte
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
            ) {

                // Début
                Column(modifier = Modifier.weight(1f)) {
                    Text("Date de début", fontWeight = FontWeight.SemiBold)
                    TrueStayTextField(
                        value = startDate,
                        onValueChange = { },
                        placeholder = "jj/mm/aaaa",
                        trailingIcon = TrueStayIcons.Calendar,
                        //keyboardType = KeyboardType.Number,
                        enabled = true,
                        onTrailingIconClick = {
                            showStartDatePicker.value = true
                        }
                    )

                }

                // Fin
                Column(modifier = Modifier.weight(1f)) {
                    Text("Date de fin", fontWeight = FontWeight.SemiBold)
                    TrueStayTextField(
                        value = endDate,
                        onValueChange = { },
                        enabled = true,
                        placeholder = "jj/mm/aaaa",
                        trailingIcon = TrueStayIcons.Calendar,

                        onTrailingIconClick = {
                            showEndDatePicker.value = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewGuidelinesCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LocalAppColors.current.primary.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = LocalAppColors.current.primary,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Ligne du haut : icône info + titre
            Row(verticalAlignment = Alignment.CenterVertically) {
                TrueStayIcon(
                    iconRes = TrueStayIcons.CircleAlert,
                    contentDescriptionRes = null,
                    tint = LocalAppColors.current.primary,
                    size = 15.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.rental_guideline_title),
                    fontWeight = FontWeight.Bold,
                    color = LocalAppColors.current.primary,
                    fontSize = 13.sp
                )
            }


            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Texte descriptif
                Text(
                    text = stringResource(R.string.rental_guideline_text1),
                    color = LocalAppColors.current.primary,
                    fontSize = 12.sp
                )

                Text(
                    text = stringResource(R.string.rental_guideline_mes_locations),
                    fontSize = 13.sp,
                    color = LocalAppColors.current.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

