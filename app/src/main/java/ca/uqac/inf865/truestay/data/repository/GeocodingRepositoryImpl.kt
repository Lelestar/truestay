package ca.uqac.inf865.truestay.data.repository

import ca.uqac.inf865.truestay.data.source.GeocodingDataSource
import ca.uqac.inf865.truestay.domain.model.GeoBounds
import ca.uqac.inf865.truestay.domain.model.LatLng as DomainLatLng
import ca.uqac.inf865.truestay.domain.model.LocationSuggestion
import ca.uqac.inf865.truestay.domain.model.AutocompleteSuggestion
import ca.uqac.inf865.truestay.domain.repository.GeocodingRepository
import javax.inject.Inject

class GeocodingRepositoryImpl @Inject constructor(
    private val geocodingDataSource: GeocodingDataSource
) : GeocodingRepository {

    override suspend fun search(query: String): Result<LocationSuggestion?> {
        return try {
            val place = geocodingDataSource.findPlace(query) ?: return Result.success(null)
            Result.success(place.toDomain(query))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun suggest(query: String, limit: Int): Result<List<AutocompleteSuggestion>> {
        return try {
            val list = geocodingDataSource.autocomplete(query, limit).map { p ->
                AutocompleteSuggestion(
                    placeId = p.placeId,
                    primaryText = p.getPrimaryText(null).toString(),
                    secondaryText = p.getSecondaryText(null)?.toString()
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getById(placeId: String): Result<LocationSuggestion?> {
        return try {
            val place = geocodingDataSource.fetchPlaceById(placeId) ?: return Result.success(null)
            Result.success(place.toDomain(place.displayName ?: place.formattedAddress ?: ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun com.google.android.libraries.places.api.model.Place.toDomain(fallbackName: String): LocationSuggestion? {
    val loc = this.location ?: return null
    val display = this.displayName ?: this.formattedAddress ?: fallbackName
    val bounds = this.viewport?.let { vp ->
        GeoBounds(
            northEast = DomainLatLng(vp.northeast.latitude, vp.northeast.longitude),
            southWest = DomainLatLng(vp.southwest.latitude, vp.southwest.longitude)
        )
    }
    return LocationSuggestion(
        displayName = display,
        location = DomainLatLng(loc.latitude, loc.longitude),
        bounds = bounds
    )
}
