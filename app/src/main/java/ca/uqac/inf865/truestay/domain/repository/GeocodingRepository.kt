package ca.uqac.inf865.truestay.domain.repository

import ca.uqac.inf865.truestay.domain.model.LocationSuggestion
import ca.uqac.inf865.truestay.domain.model.AutocompleteSuggestion

interface GeocodingRepository {
    /** Returns the best matching place details for a free-text query. */
    suspend fun search(query: String): Result<LocationSuggestion?>

    /** Returns up to a few autocomplete suggestions for the query. */
    suspend fun suggest(query: String, limit: Int = 3): Result<List<AutocompleteSuggestion>>

    /** Fetches a place by its id and maps it to a LocationSuggestion. */
    suspend fun getById(placeId: String): Result<LocationSuggestion?>
}
