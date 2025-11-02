package ca.uqac.inf865.truestay.presentation.tenant.review

import androidx.lifecycle.ViewModel
import ca.uqac.inf865.truestay.domain.repository.ReviewRepository
import ca.uqac.inf865.truestay.domain.usecase.review.SubmitReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReviewFormViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val submitReviewUseCase: SubmitReviewUseCase,
) : ViewModel() {
    // TODO: Implement add review logic
}