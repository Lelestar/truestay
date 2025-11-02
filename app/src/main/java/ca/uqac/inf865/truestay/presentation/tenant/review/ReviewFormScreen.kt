package ca.uqac.inf865.truestay.presentation.tenant.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.uqac.inf865.truestay.domain.model.ReviewType
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing

@Composable
fun ReviewFormScreen(
    rentalId: String,
    reviewType: ReviewType,
    onBackClick: () -> Unit,
    onReviewSubmitted: () -> Unit,
    viewModel: ReviewFormViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        Text("Add Review Screen - TODO")
    }
}