package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import coil3.compose.AsyncImage

/**
 * Fullscreen overlay for browsing images with a pager.
 *
 * Shows the property name, a "Photos x sur y" counter, and a close button.
 */
@Composable
fun FullscreenImageCarouselOverlay(
    photos: List<String>,
    title: String,
    initialPage: Int = 0,
    onClose: () -> Unit
) {
    val items: List<String?> = photos.ifEmpty { listOf(null) }
    val startPage = initialPage.coerceIn(0, items.lastIndex)
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { items.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
    ) {
        // Top bar: title, counter, close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xsmall)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LocalAppColors.current.white
                )
                if (items.size > 1) {
                    Text(
                        text = stringResource(
                            R.string.image_carousel_photos_counter,
                            pagerState.currentPage + 1,
                            items.size
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = LocalAppColors.current.grayDefault
                    )
                }
            }

            IconButton(onClick = onClose) {
                TrueStayIcon(
                    iconRes = TrueStayIcons.X,
                    contentDescriptionRes = null,
                    tint = LocalAppColors.current.white
                )
            }
        }

        // Pager
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 73.dp),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val photoUrl = items[page]
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(R.drawable.img_placeholder),
                        error = painterResource(R.drawable.img_placeholder)
                    )
                } else {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.img_placeholder),
                        contentDescription = stringResource(R.string.property_image_placeholder),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // Dot indicators
        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = AppSpacing.large),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xsmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(items.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) LocalAppColors.current.white
                                else LocalAppColors.current.white.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(showBackground = true, name = "Fullscreen Carousel Overlay")
@Composable
private fun FullscreenImageCarouselOverlayPreview() {
    TrueStayTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(800.dp)
        ) {
            FullscreenImageCarouselOverlay(
                photos = listOf(
                    "https://picsum.photos/seed/21/800/600",
                    "https://picsum.photos/seed/22/800/600",
                    "https://picsum.photos/seed/23/800/600"
                ),
                title = "Appartement moderne 2 pièces",
                initialPage = 0,
                onClose = {}
            )
        }
    }
}
