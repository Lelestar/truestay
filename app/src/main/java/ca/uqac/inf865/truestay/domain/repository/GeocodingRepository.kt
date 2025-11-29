package ca.uqac.inf865.truestay.domain.repository

import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.AddressAutocompleteSuggestion
import ca.uqac.inf865.truestay.domain.model.LocationSuggestion
import ca.uqac.inf865.truestay.domain.model.AutocompleteSuggestion

interface GeocodingRepository {
    /** Returns the best matching place details for a free-text query. */
    suspend fun search(query: String): Result<LocationSuggestion?>

    /** Returns up to a few autocomplete suggestions for the query. */
    suspend fun suggest(query: String, limit: Int = 3): Result<List<AutocompleteSuggestion>>

    /** Fetches a place by its id and maps it to a LocationSuggestion. */
    suspend fun getById(placeId: String): Result<LocationSuggestion?>

    /** Returns address autocomplete suggestions for the query. */
    suspend fun suggestAddresses(
        query: String,
        limit: Int = 5
    ): Result<List<AddressAutocompleteSuggestion>>

    /** Fetches a place by its id and converts it to an Address model. */
    suspend fun getAddressById(placeId: String): Result<Address?>
}
