package ca.uqac.inf865.truestay.presentation.common.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppShapes
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import coil3.compose.AsyncImage

private const val PHOTOS_DISPLAYED_BEFORE_COUNTER = 3

/**
 * Photo grid with "+n" indicator and optional delete buttons
 *
 * Displays up to 3 photos in a row. If there are more than 3 photos, shows a "+n" overlay
 * on the third photo indicating the number of additional photos.
 *
 * @param photos List of photo URLs to display
 * @param onPhotoClick Callback invoked when a photo is clicked (receives photo index)
 * @param onDeletePhoto Optional callback to delete a photo (receives photo URL). If null, delete buttons are not shown
 */
@Composable
fun PhotoGrid(
    photos: List<String>,
    onPhotoClick: (Int) -> Unit,
    onDeletePhoto: ((String) -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val displayedPhotos = photos.take(PHOTOS_DISPLAYED_BEFORE_COUNTER)
    val remainingCount = photos.size - PHOTOS_DISPLAYED_BEFORE_COUNTER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        displayedPhotos.forEachIndexed { index, photoUrl ->
            Box(
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(AppShapes.medium)
                        .clickable { onPhotoClick(index) },
                    placeholder = painterResource(R.drawable.img_placeholder),
                    error = painterResource(R.drawable.img_placeholder)
                )

                // +n indicator for the last photo if there are more
                val hasMorePhotos = index == PHOTOS_DISPLAYED_BEFORE_COUNTER - 1 && remainingCount > 0
                if (hasMorePhotos) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(AppShapes.medium)
                            .background(colors.black.copy(alpha = 0.5f))
                            .clickable { onPhotoClick(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.room_details_photo_count_more, remainingCount),
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.white
                        )
                    }
                }

                // Delete button - don't show on photo with +n indicator
                if (onDeletePhoto != null && !hasMorePhotos) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(AppSpacing.xsmall)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.black.copy(alpha = 0.6f))
                                .clickable { onDeletePhoto(photoUrl) },
                            contentAlignment = Alignment.Center
                        ) {
                            TrueStayIcon(
                                iconRes = TrueStayIcons.X,
                                contentDescriptionRes = R.string.room_details_delete_photo,
                                tint = colors.white,
                                size = 16.dp
                            )
                        }
                    }
                }
            }
        }

        // Fill remaining space if less than 3 photos
        repeat((PHOTOS_DISPLAYED_BEFORE_COUNTER - displayedPhotos.size).coerceAtLeast(0)) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Photo grid that supports both local URIs (not yet uploaded) and uploaded URLs
 * with separate delete callbacks for each type
 *
 * @param localUris List of local photo URIs (pending upload)
 * @param uploadedUrls List of already uploaded photo URLs
 * @param onDeleteLocalPhoto Callback when a local photo is deleted
 * @param onDeleteUploadedPhoto Callback when an uploaded photo is deleted
 * @param onPhotoClick Optional callback when a photo is clicked (receives photo index)
 */
@Composable
fun PhotoGrid(
    localUris: List<Uri>,
    uploadedUrls: List<String>,
    onDeleteLocalPhoto: (Uri) -> Unit,
    onDeleteUploadedPhoto: (String) -> Unit,
    onPhotoClick: ((Int) -> Unit)? = null
) {
    val colors = LocalAppColors.current

    // Combine both lists for display
    val allPhotos = uploadedUrls + localUris.map { it.toString() }
    val displayedPhotos = allPhotos.take(PHOTOS_DISPLAYED_BEFORE_COUNTER)
    val remainingCount = allPhotos.size - PHOTOS_DISPLAYED_BEFORE_COUNTER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        displayedPhotos.forEachIndexed { index, photoSource ->
            Box(
                modifier = Modifier.weight(1f)
            ) {
                // Check if it's a URL or URI
                val isUploadedPhoto = uploadedUrls.contains(photoSource)

                AsyncImage(
                    model = photoSource,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(AppShapes.medium)
                        .then(
                            if (onPhotoClick != null) {
                                Modifier.clickable { onPhotoClick(index) }
                            } else {
                                Modifier
                            }
                        ),
                    placeholder = painterResource(R.drawable.img_placeholder),
                    error = painterResource(R.drawable.img_placeholder)
                )

                // +n indicator for the last photo if there are more
                val hasMorePhotos = index == PHOTOS_DISPLAYED_BEFORE_COUNTER - 1 && remainingCount > 0
                if (hasMorePhotos) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(AppShapes.medium)
                            .background(colors.black.copy(alpha = 0.5f))
                            .then(
                                if (onPhotoClick != null) {
                                    Modifier.clickable { onPhotoClick(index) }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$remainingCount",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.white
                        )
                    }
                }

                // Delete button - don't show on photo with +n indicator
                if (!hasMorePhotos) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(AppSpacing.xsmall)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.black.copy(alpha = 0.6f))
                                .clickable {
                                    if (isUploadedPhoto) {
                                        onDeleteUploadedPhoto(photoSource)
                                    } else {
                                        onDeleteLocalPhoto(photoSource.toUri())
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            TrueStayIcon(
                                iconRes = TrueStayIcons.X,
                                contentDescriptionRes = R.string.room_details_delete_photo,
                                tint = colors.white,
                                size = 16.dp
                            )
                        }
                    }
                }
            }
        }

        // Fill remaining space if less than 3 photos
        repeat((PHOTOS_DISPLAYED_BEFORE_COUNTER - displayedPhotos.size).coerceAtLeast(0)) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
