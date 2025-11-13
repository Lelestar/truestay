package ca.uqac.inf865.truestay.presentation.tenant.search

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.domain.model.Property
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCard
import ca.uqac.inf865.truestay.presentation.common.components.PropertyCardVariant
import ca.uqac.inf865.truestay.presentation.common.components.IconSelectableButton
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.*
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import ca.uqac.inf865.truestay.domain.model.AutocompleteSuggestion
import ca.uqac.inf865.truestay.domain.model.BedroomCount
import ca.uqac.inf865.truestay.domain.model.PropertyFilters
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TextFieldSize
import ca.uqac.inf865.truestay.presentation.common.components.TextSelectableButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayRatingInput
import ca.uqac.inf865.truestay.utils.computeSliderConfig
import ca.uqac.inf865.truestay.presentation.common.components.TrueStaySlider
import ca.uqac.inf865.truestay.presentation.common.components.TrueStaySwitch

/**
 * Count the number of active filters
 */
private fun countActiveFilters(filters: PropertyFilters): Int {
    var count = 0
    if (filters.minPrice != null || filters.maxPrice != null) count++
    if (filters.minSurface != null || filters.maxSurface != null) count++
    if (filters.bedroomCounts.isNotEmpty()) count++
    if (filters.minRating != null) count++
    if (filters.availableOnly) count++
    return count
}

/**
 * Creates a custom marker icon with rating displayed
 */
private fun createCustomMarkerBitmap(
    context: Context,
    rating: Float,
    primaryColor: Int,
    textColor: Int
): BitmapDescriptor {
    val width = 120
    val height = 150
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    val backgroundPaint = Paint().apply {
        color = primaryColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    val montserratTypeface = ResourcesCompat.getFont(context, R.font.montserrat_semibold)
        ?: Typeface.create("sans-serif-medium", Typeface.NORMAL)

    val textPaint = Paint().apply {
        color = textColor
        isAntiAlias = true
        textSize = 32f
        typeface = montserratTypeface
        textAlign = Paint.Align.CENTER
    }

    val centerX = width / 2f
    val centerY = 50f
    val radius = 45f
    canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

    val ratingText = if (rating > 0f) {
        String.format(Locale.getDefault(), "%.1f", rating)
    } else {
        "N/A"
    }
    val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(ratingText, centerX, textY, textPaint)

    val trianglePath = android.graphics.Path().apply {
        val triangleTop = centerY + radius - 5f
        val triangleHeight = 16f
        val triangleWidth = 30f

        moveTo(centerX, triangleTop + triangleHeight)
        lineTo(centerX - triangleWidth / 2, triangleTop)
        lineTo(centerX + triangleWidth / 2, triangleTop)
        close()
    }

    canvas.drawPath(trianglePath, backgroundPaint)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Tenant search screen: orchestrates search text input, Google Map,
 * filtered property list in a bottom sheet, and a filter modal.
 */
@Composable
fun SearchScreen(
    onPropertyClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreenContent(
        allProperties = uiState.allProperties,
        properties = uiState.properties,
        filters = uiState.filters,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        searchQuery = uiState.searchQuery,
        searchLocation = uiState.searchLocation,
        searchZoomLevel = uiState.searchZoomLevel,
        searchBounds = uiState.searchBounds,
        suggestions = uiState.suggestions,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onSearchSubmit = viewModel::searchLocation,
        onSuggestionClick = viewModel::onSuggestionSelected,
        onClearSearchLocation = viewModel::clearSearchLocation,
        onClearSuggestions = viewModel::clearSuggestions,
        onPriceRangeChange = viewModel::updatePriceRange,
        onSurfaceRangeChange = viewModel::updateSurfaceRange,
        onToggleBedroomCount = viewModel::toggleBedroomCount,
        onMinRatingChange = { ratingInt -> viewModel.setMinRating(if (ratingInt > 0) ratingInt.toFloat() else null) },
        onAvailableOnlyChange = viewModel::setAvailableOnly,
        onResetFilters = viewModel::resetFilters,
        onPropertyClick = { propertyId ->
            onPropertyClick(propertyId)
        },
        onMapBoundsChanged = { northEast, southWest ->
            viewModel.updateGeoBounds(northEast, southWest)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Main Search UI: map + list.
 * - searchBounds: preferred viewport to fit camera when a place is selected.
 * - searchLocation/searchZoomLevel: fallback when bounds are not provided.
 */
private fun SearchScreenContent(
    allProperties: List<Property>,
    properties: List<Property>,
    filters: PropertyFilters,
    isLoading: Boolean,
    errorMessage: String?,
    searchQuery: String,
    searchLocation: LatLng?,
    searchZoomLevel: Float?,
    searchBounds: LatLngBounds?,
    suggestions: List<AutocompleteSuggestion>,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onSuggestionClick: (AutocompleteSuggestion) -> Unit,
    onClearSearchLocation: () -> Unit,
    onClearSuggestions: () -> Unit,
    onPriceRangeChange: (min: Int?, max: Int?) -> Unit,
    onSurfaceRangeChange: (min: Int?, max: Int?) -> Unit,
    onToggleBedroomCount: (BedroomCount) -> Unit,
    onMinRatingChange: (Int) -> Unit,
    onAvailableOnlyChange: (Boolean) -> Unit,
    onResetFilters: () -> Unit,
    onPropertyClick: (String) -> Unit,
    onMapBoundsChanged: (northEast: Pair<Double, Double>, southWest: Pair<Double, Double>) -> Unit
) {
    val context = LocalContext.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val successColor = LocalAppColors.current.success.toArgb()
    val dangerColor = LocalAppColors.current.error.toArgb()
    val whiteColor = LocalAppColors.current.white.toArgb()
    val borderColor = LocalAppColors.current.grayBorder
    val coroutineScope = rememberCoroutineScope()

    var selectedProperty by remember { mutableStateOf<Property?>(null) }
    var showFiltersBottomSheet by remember { mutableStateOf(false) }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false,
            confirmValueChange = { true }
        )
    )

    // Detect expanded state directly from bottomSheetState
    val isSheetExpanded by remember {
        derivedStateOf {
            val state = scaffoldState.bottomSheetState
            state.currentValue == SheetValue.Expanded &&
            state.targetValue == SheetValue.Expanded
        }
    }

    val sheetCorner by animateDpAsState(
        targetValue = if (isSheetExpanded) 0.dp else 16.dp,
        animationSpec = spring()
    )

    // Track if map is ready to prevent CameraUpdateFactory crashes
    var isMapReady by remember { mutableStateOf(false) }

    val customMarkers = remember(properties, successColor, dangerColor, whiteColor, isMapReady) {
        if (!isMapReady) {
            emptyMap()
        } else {
            properties.associateWith { property ->
                val rating = property.ratings.propertyAverageRating
                val markerColor = if (property.isAvailable) successColor else dangerColor
                createCustomMarkerBitmap(context, rating, markerColor, whiteColor)
            }
        }
    }

    val centerPosition = remember(allProperties) {
        val validProperties = allProperties.filter {
            it.address.latitude != 0.0 && it.address.longitude != 0.0
        }
        if (validProperties.isNotEmpty()) {
            val avgLat = validProperties.map { it.address.latitude }.average()
            val avgLng = validProperties.map { it.address.longitude }.average()
            LatLng(avgLat, avgLng)
        } else {
            LatLng(45.5009, -73.5677)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerPosition, 10f)
    }

    val density = LocalDensity.current

    // Anchor for floating suggestions overlay (position and size of search bar)
    var anchorPos by remember { mutableStateOf(IntOffset(0, 0)) }
    var anchorSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(allProperties, isMapReady) {
        if (!isMapReady) return@LaunchedEffect
        val validProperties = allProperties.filter {
            it.address.latitude != 0.0 && it.address.longitude != 0.0
        }

        if (validProperties.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            validProperties.forEach { property ->
                Log.d("SearchScreen", "Property: $property")
                boundsBuilder.include(
                    LatLng(property.address.latitude, property.address.longitude)
                )
            }
            val bounds = boundsBuilder.build()
            val padding = with(density) { 100.dp.roundToPx() }

            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, padding)
                )
            } catch (_: Exception) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(centerPosition, 10f)
                )
            }
        }
    }

    // Fit to bounds when available; otherwise center with zoom
    LaunchedEffect(searchBounds, searchLocation, searchZoomLevel, isMapReady) {
        if (!isMapReady) return@LaunchedEffect

        when {
            searchBounds != null -> {
                val padding = with(density) { 80.dp.roundToPx() }
                try {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(searchBounds, padding)
                    )
                } catch (_: Exception) {
                    // Fallback to center if bounds are invalid for current view size
                    searchLocation?.let { location ->
                        val zoom = searchZoomLevel ?: 13f
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(location, zoom)
                        )
                    }
                }
                onClearSearchLocation()
            }
            searchLocation != null -> {
                val zoom = searchZoomLevel ?: 13f
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(searchLocation, zoom)
                )
                onClearSearchLocation()
            }
        }
    }

    // Report visible map bounds to ViewModel when camera stops moving
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val projection = cameraPositionState.projection
            if (projection != null) {
                val bounds = projection.visibleRegion.latLngBounds
                onMapBoundsChanged(
                    Pair(bounds.northeast.latitude, bounds.northeast.longitude),
                    Pair(bounds.southwest.latitude, bounds.southwest.longitude)
                )
            }
        }
    }

    // Root container
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content: header + map + sheet
        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalAppColors.current.white)
                .drawBehind {
                    val stroke = with(density) { 1.dp.toPx() }
                    val y = size.height - stroke / 2
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = stroke
                    )
                }
                .padding(horizontal = AppSpacing.large, vertical = AppSpacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrueStayTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = null,
                    placeholder = stringResource(R.string.search_placeholder),
                    leadingIcon = TrueStayIcons.Search,
                    showClearButton = true,
                    onClear = {
                        onSearchQueryChange("")
                        onClearSuggestions()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInRoot()
                            anchorPos = IntOffset(pos.x.toInt(), pos.y.toInt())
                            anchorSize = coords.size
                        },
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search,
                    onImeAction = {
                        if (searchQuery.isNotBlank()) {
                            onSearchSubmit(searchQuery)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    },
                    size = TextFieldSize.Large
                )

                BadgedBox(
                    badge = {
                        val activeFiltersCount = countActiveFilters(filters)
                        if (activeFiltersCount > 0) {
                            Badge(
                                containerColor = LocalAppColors.current.primary,
                                contentColor = LocalAppColors.current.white
                            ) {
                                Text(
                                    text = activeFiltersCount.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LocalAppColors.current.white
                                )
                            }
                        }
                    }
                ) {
                    IconSelectableButton(
                        selected = showFiltersBottomSheet,
                        onClick = { showFiltersBottomSheet = true },
                        iconRes = TrueStayIcons.Funnel,
                        contentDescriptionRes = R.string.search_filters
                    )
                }
            }
        }

        // Map and bottom sheet
        Box(modifier = Modifier.fillMaxSize()) {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 48.dp,
                sheetShape = RoundedCornerShape(
                    topStart = sheetCorner,
                    topEnd = sheetCorner
                ),
                sheetContainerColor = LocalAppColors.current.graySurface,
                sheetDragHandle = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.small)
                            .background(LocalAppColors.current.graySurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .fillMaxWidth(0.1f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(LocalAppColors.current.grayLight)
                        )
                    }
                },
                sheetContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 2000.dp)
                            .background(LocalAppColors.current.graySurface)
                            .padding(horizontal = AppSpacing.large)
                    ) {
                        if (errorMessage != null) {
                            Text(
                                text = stringResource(R.string.search_error_generic, errorMessage),
                                style = MaterialTheme.typography.titleMedium,
                                color = LocalAppColors.current.error,
                                modifier = Modifier.padding(bottom = AppSpacing.medium)
                            )
                        } else {
                            val resultsText = if (properties.isEmpty()) {
                                stringResource(R.string.search_no_results)
                            } else {
                                pluralStringResource(R.plurals.search_results_found, properties.size, properties.size)
                            }
                            Text(
                                text = resultsText,
                                style = MaterialTheme.typography.titleMedium,
                                color = LocalAppColors.current.black,
                                modifier = Modifier.padding(bottom = AppSpacing.medium)
                            )

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(bottom = 56.dp)
                            ) {
                                items(
                                    items = properties,
                                    key = { it.id }
                                ) { property ->
                                    PropertyCard(
                                        property = property,
                                        variant = PropertyCardVariant.COMPACT,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .alpha(if (property.isAvailable) 1f else 0.5f),
                                        onClick = { onPropertyClick(property.id) }
                                    )
                                    Spacer(modifier = Modifier.height(AppSpacing.small))
                                }
                            }
                        }
                    }
                }
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        rotationGesturesEnabled = false,
                        tiltGesturesEnabled = false,
                        zoomControlsEnabled = false
                    ),
                    onMapLoaded = {
                        isMapReady = true
                    },
                    onMapClick = {
                        selectedProperty = null
                        onClearSuggestions()
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ) {
                    properties.forEach { property ->
                        if (property.address.latitude != 0.0 && property.address.longitude != 0.0) {
                            val position = LatLng(property.address.latitude, property.address.longitude)
                            val customIcon = customMarkers[property]
                            customIcon?.let {
                                Marker(
                                    state = MarkerState(position = position),
                                    icon = it,
                                    alpha = 1.0f,
                                    onClick = {
                                        selectedProperty = property
                                        onClearSuggestions()
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        if (isMapReady) {
                                            coroutineScope.launch {
                                                try {
                                                    cameraPositionState.animate(
                                                        CameraUpdateFactory.newLatLng(position)
                                                    )
                                                } catch (_: Exception) {
                                                    // Ignore camera animation errors
                                                }
                                            }
                                        }
                                        true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Selected property card - outside the scaffold
            selectedProperty?.let { property ->
                if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(
                                start = AppSpacing.large,
                                end = AppSpacing.large,
                                bottom = AppSpacing.xxxlarge
                            )
                    ) {
                        PropertyCard(
                            property = property,
                            variant = PropertyCardVariant.COMPACT,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    if (!property.isAvailable) {
                                        val matrix = ColorMatrix().apply {
                                            setToSaturation(0.3f)
                                        }
                                        colorFilter = ColorFilter.colorMatrix(matrix)
                                    }
                                },
                            onClick = { onPropertyClick(property.id) }
                        )
                    }
                }
            }

            // FAB appears when bottom sheet is expanded - outside the scaffold
            if (isSheetExpanded) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = AppSpacing.large)
                        .height(33.dp),
                    containerColor = LocalAppColors.current.black,
                    contentColor = LocalAppColors.current.white,
                    shape = RoundedCornerShape(50),
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.search_back_to_map),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalAppColors.current.white
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.xsmall))
                        TrueStayIcon(
                            iconRes = TrueStayIcons.Map,
                            contentDescriptionRes = R.string.search_back_to_map,
                            size = 24.dp,
                            tint = LocalAppColors.current.white
                        )
                    }
                }
            }
        }
        }

        // Floating suggestions overlay (Popup) on top of everything
        if (suggestions.isNotEmpty()) {
            val widthDp = with(density) { anchorSize.width.toDp() }
            val xPx = anchorPos.x
            // Place right under the search field, compensating header bottom padding
            val yPx = anchorPos.y + anchorSize.height - with(density) { AppSpacing.large.roundToPx() } + with(density) { AppSpacing.xsmall.roundToPx() }

            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(xPx, yPx),
                properties = PopupProperties(focusable = false)
            ) {
                Card(
                    modifier = Modifier.width(widthDp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.white)
                ) {
                    Column(modifier = Modifier.padding(vertical = AppSpacing.xsmall)) {
                        suggestions.take(3).forEach { s ->
                            SuggestionRow(
                                primary = s.primaryText,
                                secondary = s.secondaryText,
                                onClick = {
                                    onSuggestionClick(s)
                                    onClearSuggestions()
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Filters Bottom Sheet
        if (showFiltersBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFiltersBottomSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = LocalAppColors.current.white,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.small)
                            .background(LocalAppColors.current.graySurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .fillMaxWidth(0.1f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(LocalAppColors.current.grayLight)
                        )
                    }
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.large, 0.dp, AppSpacing.large, AppSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
                ) {
                    Text(
                        text = stringResource(R.string.search_filters),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LocalAppColors.current.black,
                        modifier = Modifier.padding(bottom = AppSpacing.small)
                    )

                    // Price Range - dynamic bounds/steps computed from data (see utils/SliderUtils)
                    run {
                        val values = allProperties.map { it.monthlyRent }
                        val priceConfig = computeSliderConfig(values, fallbackMin = 300, fallbackMax = 5000)
                        val minBound = priceConfig.min
                        val maxBound = priceConfig.max
                        val steps = priceConfig.steps
                        val currentMin = (filters.minPrice?.toFloat() ?: minBound).coerceIn(minBound, maxBound)
                        val currentMax = (filters.maxPrice?.toFloat() ?: maxBound).coerceIn(minBound, maxBound)
                        TrueStaySlider(
                            label = stringResource(R.string.search_filter_price_range),
                            value = currentMin..currentMax,
                            valueRange = minBound..maxBound,
                            steps = steps,
                            onValueChange = { range ->
                                onPriceRangeChange(range.start.toInt(), range.endInclusive.toInt())
                            },
                            formatValue = { value -> "${value.toInt()}$" }
                        )
                    }

                    // Surface Range - dynamic bounds/steps computed from data (see utils/SliderUtils)
                    run {
                        val values = allProperties.map { it.surface }
                        val surfaceConfig = computeSliderConfig(values, fallbackMin = 10, fallbackMax = 300)
                        val minBound = surfaceConfig.min
                        val maxBound = surfaceConfig.max
                        val steps = surfaceConfig.steps
                        val currentMin = (filters.minSurface?.toFloat() ?: minBound).coerceIn(minBound, maxBound)
                        val currentMax = (filters.maxSurface?.toFloat() ?: maxBound).coerceIn(minBound, maxBound)
                        TrueStaySlider(
                            label = stringResource(R.string.search_filter_surface_range),
                            value = currentMin..currentMax,
                            valueRange = minBound..maxBound,
                            steps = steps,
                            onValueChange = { range ->
                                onSurfaceRangeChange(range.start.toInt(), range.endInclusive.toInt())
                            },
                            formatValue = { value -> "${value.toInt()}m²" }
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.search_filter_number_of_bedrooms),
                            style = MaterialTheme.typography.titleMedium,
                            color = LocalAppColors.current.black,
                            modifier = Modifier.padding(bottom = AppSpacing.small)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                            val selected = filters.bedroomCounts
                            TextSelectableButton(
                                selected = selected.contains(BedroomCount.ONE),
                                onClick = { onToggleBedroomCount(BedroomCount.ONE) },
                                text = "1",
                                modifier = Modifier.width(42.dp),
                                centerContent = true,
                            )
                            TextSelectableButton(
                                selected = selected.contains(BedroomCount.TWO),
                                onClick = { onToggleBedroomCount(BedroomCount.TWO) },
                                text = "2",
                                modifier = Modifier.width(42.dp),
                                centerContent = true,
                            )
                            TextSelectableButton(
                                selected = selected.contains(BedroomCount.THREE),
                                onClick = { onToggleBedroomCount(BedroomCount.THREE) },
                                text = "3",
                                modifier = Modifier.width(42.dp),
                                centerContent = true,
                            )
                            TextSelectableButton(
                                selected = selected.contains(BedroomCount.FOUR_PLUS),
                                onClick = { onToggleBedroomCount(BedroomCount.FOUR_PLUS) },
                                text = "4+",
                                modifier = Modifier.width(42.dp),
                                centerContent = true,
                            )
                        }
                    }

                    TrueStayRatingInput(
                        label = stringResource(R.string.search_filter_min_avg_rating),
                        rating = (filters.minRating ?: 0f).toInt(),
                        onRatingChange = onMinRatingChange
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.search_filter_available_now),
                            style = MaterialTheme.typography.titleMedium,
                            color = LocalAppColors.current.black
                        )
                        TrueStaySwitch(
                            checked = filters.availableOnly,
                            onCheckedChange = onAvailableOnlyChange
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.small),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                    ) {
                        TrueStayButton(
                            text = stringResource(R.string.search_filter_reset),
                            onClick = { onResetFilters() },
                            variant = ButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f)
                        )
                        TrueStayButton(
                            text = pluralStringResource(R.plurals.search_filter_display, properties.size, properties.size),
                            onClick = { showFiltersBottomSheet = false },
                            isLoading = isLoading,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    primary: String,
    secondary: String?,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TrueStayIcon(
                iconRes = TrueStayIcons.Search,
                contentDescriptionRes = R.string.search_placeholder,
                size = 16.dp,
                tint = colors.primary
            )
            Spacer(modifier = Modifier.width(AppSpacing.small))
            Text(
                text = primary,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.black
            )
        }
        if (!secondary.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = secondary,
                style = MaterialTheme.typography.bodySmall,
                color = colors.grayDark
            )
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true)
@Composable
private fun SuggestionRowPreview() {
    ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme {
        SuggestionRow(
            primary = "Montreal",
            secondary = "Quebec, Canada",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionRowNoSecondaryPreview() {
    ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme {
        SuggestionRow(
            primary = "Montreal",
            secondary = null,
            onClick = {}
        )
    }
}

@Preview(name = "Search Screen - Empty", showBackground = true)
@Composable
private fun SearchScreenEmptyPreview() {
    ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme {
        SearchScreenContent(
            allProperties = emptyList(),
            properties = emptyList(),
            filters = PropertyFilters(),
            isLoading = false,
            searchQuery = "",
            searchLocation = null,
            searchZoomLevel = null,
            searchBounds = null,
            suggestions = emptyList(),
            onSearchQueryChange = {},
            onSearchSubmit = {},
            onSuggestionClick = {},
            onClearSearchLocation = {},
            onClearSuggestions = {},
            onPriceRangeChange = { _, _ -> },
            onSurfaceRangeChange = { _, _ -> },
            onToggleBedroomCount = {},
            onMinRatingChange = {},
            onAvailableOnlyChange = {},
            onResetFilters = {},
            onPropertyClick = {},
            onMapBoundsChanged = { _, _ -> },
            errorMessage = null
        )
    }
}

