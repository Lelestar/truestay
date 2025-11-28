package ca.uqac.inf865.truestay.presentation.common.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors

data class SignatureLine(
    val path: Path,
    val color: Color,
    val strokeWidth: Float
)

/**
 * A composable that allows a user to draw a signature using touch gestures.
 *
 * It manages the drawing state internally and uses [Canvas] to render the lines.
 * This component provides interactivity through a [SignatureController], allowing
 * the parent to clear the canvas or capture the drawing as a [Bitmap].
 *
 * @param modifier The modifier to be applied to the layout.
 * @param strokeWidth The thickness of the signature lines (default is 2.dp).
 * @param strokeColor The color of the signature lines (default is black from [LocalAppColors]).
 * @param backgroundColor The background color of the canvas area (default is white from [LocalAppColors]).
 * @param controller A [SignatureController] instance used to control the canvas actions (clear, capture) from outside the composable.
 */
@Composable
fun SignatureCanvas(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp,
    strokeColor: Color = LocalAppColors.current.black,
    backgroundColor: Color = LocalAppColors.current.white,
    controller: SignatureController
) {
    val lines = remember { mutableStateListOf<SignatureLine>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }

    // Bind controller actions so external callers can clear or capture the drawing
    controller.clear = {
        lines.clear()
        currentPath = null
        controller.hasSignature = false
    }

    controller.capture = {
        if (canvasSize.width <= 0 || canvasSize.height <= 0) {
            controller.hasSignature = false
            null
        } else {
            val bitmap = Bitmap.createBitmap(canvasSize.width, canvasSize.height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(backgroundColor.toArgb())

            val paint = android.graphics.Paint().apply {
                // Configure stroke once; color/width are reset per line below
                this.strokeWidth = strokeWidthPx
                style = android.graphics.Paint.Style.STROKE
                strokeCap = android.graphics.Paint.Cap.ROUND
                strokeJoin = android.graphics.Paint.Join.ROUND
                isAntiAlias = true
            }

            lines.forEach { line ->
                paint.color = line.color.toArgb()
                paint.strokeWidth = line.strokeWidth
                canvas.drawPath(line.path.asAndroidPath(), paint)
            }

            currentPath?.let { activePath ->
                paint.color = strokeColor.toArgb()
                paint.strokeWidth = strokeWidthPx
                canvas.drawPath(activePath.asAndroidPath(), paint)
            }

            controller.hasSignature = lines.isNotEmpty() || currentPath != null
            bitmap
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { canvasSize = it }
            .clipToBounds()
            .background(backgroundColor)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val activePath = Path().apply { moveTo(down.position.x, down.position.y) }
                    currentPath = Path().apply { addPath(activePath) }
                    controller.hasSignature = true

                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                activePath.lineTo(change.position.x, change.position.y)
                                currentPath = Path().apply { addPath(activePath) }
                                controller.hasSignature = true
                                change.consume()
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // Finger lifted - save the path
                    lines.add(SignatureLine(Path().apply { addPath(activePath) }, strokeColor, strokeWidthPx))
                    currentPath = null
                    controller.hasSignature = lines.isNotEmpty()
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            lines.forEach { line ->
                drawPath(
                    path = line.path,
                    color = line.color,
                    style = Stroke(
                        width = line.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            currentPath?.let { path ->
                drawPath(
                    path = path,
                    color = strokeColor,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

class SignatureController {
    var clear: () -> Unit = {}
    var capture: () -> Bitmap? = { null }

    private var _hasSignature = mutableStateOf(false)
    var hasSignature: Boolean
        get() = _hasSignature.value
        set(value) { _hasSignature.value = value }
}
