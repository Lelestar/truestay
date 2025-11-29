package ca.uqac.inf865.truestay.domain.model

/**
 * Lightweight suggestion for address autocomplete dropdown
 */
data class AddressAutocompleteSuggestion(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String?
)
