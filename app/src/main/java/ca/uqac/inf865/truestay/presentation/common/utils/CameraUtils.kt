package ca.uqac.inf865.truestay.presentation.common.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * A composable function that provides a camera launcher with automatic photo optimization.
 *
 * Usage example:
 * ```
 * val cameraLauncher = rememberOptimizedCameraLauncher { optimizedUri ->
 *     viewModel.uploadPhoto(optimizedUri)
 * }
 *
 * Button(onClick = { cameraLauncher.launch() }) {
 *     Text("Take Photo")
 * }
 * ```
 *
 * @param onPhotoTaken Callback invoked with the optimized photo URI
 * @return CameraLauncher object with a launch() function
 */
@Composable
fun rememberOptimizedCameraLauncher(
    onPhotoTaken: (Uri) -> Unit
): CameraLauncher {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            photoUri?.let { uri ->
                // Optimize the photo before passing it to the callback
                val optimizedUri = optimizePhoto(context, uri)
                optimizedUri?.let { validUri ->
                    onPhotoTaken(validUri)
                }
            }
        }
    }

    return remember {
        CameraLauncher {
            photoUri = createImageFileUri(context)
            launcher.launch(photoUri)
        }
    }
}

/**
 * Wrapper class for camera launcher functionality.
 */
class CameraLauncher(private val launchAction: () -> Unit) {
    /**
     * Launches the camera to take a photo.
     */
    fun launch() {
        launchAction()
    }
}
