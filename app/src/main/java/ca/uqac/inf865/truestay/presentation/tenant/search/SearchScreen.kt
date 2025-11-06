package ca.uqac.inf865.truestay.presentation.tenant.search

import android.util.Log
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Address
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.domain.model.PropertyRatings
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCard
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCardVariant
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private const val TAG = "SearchScreen"

@Composable
fun SearchScreen(
    onPropertyClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreenContent(
        properties = uiState.properties,
        onPropertyClick = { propertyId ->
            Log.d(TAG, "Property clicked: $propertyId")
            onPropertyClick(propertyId)
        }
    )
}

@Composable
private fun SearchScreenContent(
    properties: List<Property>,
    onPropertyClick: (String) -> Unit
) {
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    val listState = rememberLazyListState()
    var isMapTouched by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = AppSpacing.small),
        userScrollEnabled = !isMapTouched
    ) {
        // Map
        item {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(horizontal = AppSpacing.large)
                    .clip(AppShapes.large)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(pass = PointerEventPass.Initial)
                            isMapTouched = true

                            // Wait for all pointers to be released
                            do {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            } while (event.changes.any { it.pressed })

                            isMapTouched = false
                        }
                    },
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false
                )
            ) {
                Marker(
                    state = remember { MarkerState(position = singapore) },
                    title = "Singapore",
                    snippet = "Marker in Singapore"
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.large))
        }

        // Results count
        item {
            val resultsText = if (properties.isEmpty()) {
                stringResource(R.string.search_no_results)
            } else {
                stringResource(R.string.search_results_found, properties.size)
            }
            Text(
                text = resultsText,
                style = MaterialTheme.typography.titleMedium,
                color = LocalAppColors.current.black,
                modifier = Modifier.padding(horizontal = AppSpacing.large)
            )
            Spacer(modifier = Modifier.height(AppSpacing.medium))
        }

        // Property list
        items(
            items = properties,
            key = { it.id }
        ) { property ->
            PropertyCard(
                property = property,
                variant = PropertyCardVariant.COMPACT,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.large),
                onClick = { onPropertyClick(property.id) }
            )
            Spacer(modifier = Modifier.height(AppSpacing.small))
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(name = "Search Screen", showBackground = true)
@Composable
private fun SearchScreenPreview() {
    TrueStayTheme {
        SearchScreenContent(
            properties = listOf(
                Property(
                    id = "1",
                    name = "Appartement moderne 2 pièces",
                    address = Address(
                        street = "15 Rue de la Paix",
                        city = "Paris",
                        postalCode = "75001"
                    ),
                    monthlyRent = 1900,
                    ratings = PropertyRatings(
                        propertyAverageRating = 4.5f,
                        propertyReviewCount = 12
                    ),
                    photos = listOf("https://example.com/photo1.jpg")
                ),
                Property(
                    id = "2",
                    name = "Studio lumineux centre-ville",
                    address = Address(
                        street = "42 Avenue des Champs",
                        city = "Lyon",
                        postalCode = "69001"
                    ),
                    monthlyRent = 850,
                    ratings = PropertyRatings(
                        propertyAverageRating = 4.2f,
                        propertyReviewCount = 8
                    ),
                    photos = listOf()
                ),
                Property(
                    id = "3",
                    name = "Grande maison avec jardin",
                    address = Address(
                        street = "8 Rue du Jardin",
                        city = "Marseille",
                        postalCode = "13001"
                    ),
                    monthlyRent = 2500,
                    ratings = PropertyRatings(
                        propertyAverageRating = 4.8f,
                        propertyReviewCount = 24
                    ),
                    photos = listOf("https://example.com/photo3.jpg")
                )
            ),
            onPropertyClick = {}
        )
    }
}
