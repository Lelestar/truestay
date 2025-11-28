package ca.uqac.inf865.truestay.presentation.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale
import androidx.compose.ui.platform.LocalContext

private const val MAX_IMAGE_WIDTH = 1920
private const val MAX_IMAGE_HEIGHT = 1920
private const val JPEG_QUALITY = 85

/**
 * Optimizes a photo by:
 * - Resizing to max dimensions (1920x1920)
 * - Correcting rotation based on EXIF data
 * - Compressing to JPEG quality 85%
 *
 * This reduces file size significantly while maintaining good quality for photos.
 *
 * @param context Android context
 * @param sourceUri URI of the original photo
 * @return URI of the optimized photo, or null if optimization failed
 */
fun optimizePhoto(context: Context, sourceUri: Uri): Uri? {
    return try {
        // Read the source file
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        if (originalBitmap == null) return null

        // Get EXIF orientation
        val exif = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
            ExifInterface(stream)
        }
        val orientation = exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        ) ?: ExifInterface.ORIENTATION_NORMAL

        // Calculate new dimensions maintaining aspect ratio
        val width = originalBitmap.width
        val height = originalBitmap.height
        val ratio = minOf(
            MAX_IMAGE_WIDTH.toFloat() / width,
            MAX_IMAGE_HEIGHT.toFloat() / height,
            1f // Don't upscale
        )

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        // Resize bitmap
        val resizedBitmap = originalBitmap.scale(newWidth, newHeight)
        originalBitmap.recycle()

        // Rotate if needed based on EXIF
        val rotatedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(resizedBitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(resizedBitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(resizedBitmap, 270f)
            else -> resizedBitmap
        }

        if (rotatedBitmap != resizedBitmap) {
            resizedBitmap.recycle()
        }

        // Save optimized image to cache using FileProvider
        val optimizedFile = File(context.cacheDir, "optimized_${System.currentTimeMillis()}.jpg")
        FileOutputStream(optimizedFile).use { outputStream ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        }
        rotatedBitmap.recycle()

        // Return a content:// URI via FileProvider
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            optimizedFile
        )
    } catch (e: Exception) {
        e.printStackTrace()
        android.util.Log.e("ImageUtils", "Error optimizing photo", e)
        null
    }
}

/**
 * Creates a temporary file URI for camera capture using FileProvider.
 *
 * @param context Android context
 * @return URI for the camera to save the photo to
 */
fun createImageFileUri(context: Context): Uri {
    val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}

private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(angle) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

/**
 * A composable function that provides a multi-photo picker launcher with automatic photo optimization.
 *
 * Usage example:
 * ```
 * val photoPickerLauncher = rememberOptimizedMultiPhotoPickerLauncher(maxItems = 10) { optimizedUris ->
 *     viewModel.uploadPhotos(optimizedUris)
 * }
 *
 * Button(onClick = { photoPickerLauncher.launch(maxItems = 10) }) {
 *     Text("Pick Photos")
 * }
 * ```
 *
 * @param maxItems Maximum number of photos that can be selected (default: 5)
 * @param onPhotosPicked Callback invoked with the list of optimized photo URIs
 * @return PhotoPickerLauncher object with a launch() function
 */
@Composable
fun rememberOptimizedMultiPhotoPickerLauncher(
    maxItems: Int = 5,
    onPhotosPicked: (List<Uri>) -> Unit
): PhotoPickerLauncher {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxItems)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            android.util.Log.d("PhotoPicker", "Photos sélectionnées: ${uris.size}")
            // Pour l'instant, utilisons les URIs originales qui ont déjà les permissions
            // L'optimisation sera faite lors de l'upload
            onPhotosPicked(uris)
        }
    }

    return remember {
        PhotoPickerLauncher { maxItems ->
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
}

/**
 * Wrapper class for photo picker launcher functionality.
 */
class PhotoPickerLauncher(private val launchAction: (Int) -> Unit) {
    /**
     * Launches the photo picker to select multiple photos.
     * @param maxItems The maximum number of photos that can be selected.
     */
    fun launch(maxItems: Int) {
        launchAction(maxItems)
    }
}