package ca.uqac.inf865.truestay.presentation.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import coil3.compose.AsyncImage

/**
 * Reusable image carousel with pager and dot indicators.
 *
 * @param photos List of image URLs. If empty, a placeholder is shown.
 * @param contentDescription Description for accessibility (applied to images).
 * @param modifier Compose modifier.
 * @param onImageClick Optional click handler on the current image (provides index).
 */
@Composable
fun ImageCarousel(
    photos: List<String>,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onImageClick: ((Int) -> Unit)? = null
) {
    val items: List<String?> = photos.ifEmpty { listOf(null) }
    val pagerState = rememberPagerState(pageCount = { items.size })

    Box(
        modifier = modifier
            .background(LocalAppColors.current.grayLight)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val photoUrl = items[page]
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxSize()
                        .let {
                            if (onImageClick != null) {
                                it.clickable { onImageClick(page) }
                            } else {
                                it
                            }
                        },
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.img_placeholder),
                    error = painterResource(R.drawable.img_placeholder)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.img_placeholder),
                    contentDescription = stringResource(R.string.property_image_placeholder),
                    modifier = Modifier
                        .fillMaxSize()
                        .let {
                            if (onImageClick != null) {
                                it.clickable { onImageClick(page) }
                            } else {
                                it
                            }
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = AppSpacing.medium),
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
@Preview(showBackground = true, name = "ImageCarousel - Multiple")
@Composable
private fun ImageCarouselMultiplePreview() {
    TrueStayTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            ImageCarousel(
                photos = listOf(
                    "https://picsum.photos/seed/1/600/400",
                    "https://picsum.photos/seed/2/600/400",
                    "https://picsum.photos/seed/3/600/400"
                ),
                contentDescription = "Carousel preview",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, name = "ImageCarousel - Empty")
@Composable
private fun ImageCarouselEmptyPreview() {
    TrueStayTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            ImageCarousel(
                photos = emptyList(),
                contentDescription = "Carousel empty",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
