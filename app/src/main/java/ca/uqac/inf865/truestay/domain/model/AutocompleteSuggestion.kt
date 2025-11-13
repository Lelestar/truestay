package ca.uqac.inf865.truestay.domain.model

/** Lightweight suggestion for displaying under the search bar. */
data class AutocompleteSuggestion(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String?
)

