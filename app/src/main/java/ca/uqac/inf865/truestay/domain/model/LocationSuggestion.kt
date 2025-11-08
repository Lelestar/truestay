package ca.uqac.inf865.truestay.domain.model

data class LocationSuggestion(
    val displayName: String,
    val location: LatLng,
    val bounds: GeoBounds?
)

