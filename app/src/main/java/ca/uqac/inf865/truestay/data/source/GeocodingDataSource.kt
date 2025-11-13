package ca.uqac.inf865.truestay.data.source

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class GeocodingDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val placesClient: PlacesClient by lazy {
        if (!Places.isInitialized()) {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
            if (!apiKey.isNullOrBlank()) {
                Places.initializeWithNewPlacesApiEnabled(context, apiKey)
            }
        }
        Places.createClient(context)
    }

    suspend fun findPlace(query: String): Place? {
        if (query.isBlank()) return null

        return try {
            // Create a session token for autocompletion
            val token = AutocompleteSessionToken.newInstance()

            // Search for predictions
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                .build()

            val predictions = suspendCancellableCoroutine { continuation ->
                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        continuation.resume(response.autocompletePredictions)
                    }
                    .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
            }

            // Take the first prediction
            val firstPrediction = predictions.firstOrNull() ?: return null

            // Retrieve details about this location
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION,
                Place.Field.VIEWPORT
            )

            val placeRequest = FetchPlaceRequest.builder(firstPrediction.placeId, placeFields)
                .setSessionToken(token)
                .build()

            val place = suspendCancellableCoroutine { continuation ->
                placesClient.fetchPlace(placeRequest)
                    .addOnSuccessListener { response ->
                        continuation.resume(response.place)
                    }
                    .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
            }

            place
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchPlaceById(placeId: String): Place? {
        if (placeId.isBlank()) return null
        return try {
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION,
                Place.Field.VIEWPORT
            )
            val request = FetchPlaceRequest.builder(placeId, placeFields).build()
            suspendCancellableCoroutine { continuation ->
                placesClient.fetchPlace(request)
                    .addOnSuccessListener { response -> continuation.resume(response.place) }
                    .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun autocomplete(query: String, limit: Int = 3): List<com.google.android.libraries.places.api.model.AutocompletePrediction> {
        if (query.isBlank()) return emptyList()
        return try {
            val token = AutocompleteSessionToken.newInstance()
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
                .build()
            val predictions = suspendCancellableCoroutine { continuation ->
                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response -> continuation.resume(response.autocompletePredictions) }
                    .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
            }
            predictions.take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
