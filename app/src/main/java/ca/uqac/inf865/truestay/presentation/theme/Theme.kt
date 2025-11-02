package ca.uqac.inf865.truestay.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalAppColors = staticCompositionLocalOf<TrueStayColors> {
    error("No AppColors provided")
}

private val LightColorScheme = lightColorScheme(
    // ====== Core brand colors ======
    primary = AppColorsLight.primary,
    onPrimary = AppColorsLight.white,
    primaryContainer = AppColorsLight.primarySurface,
    onPrimaryContainer = AppColorsLight.black,

    secondary = AppColorsLight.primaryVariant,
    onSecondary = AppColorsLight.white,
    secondaryContainer = AppColorsLight.primaryVariantSurface,
    onSecondaryContainer = AppColorsLight.black,

    // ====== Background & surface ======
    background = AppColorsLight.graySurface,
    onBackground = AppColorsLight.black,
    surface = AppColorsLight.white,
    onSurface = AppColorsLight.black,
    outline = AppColorsLight.grayBorder,

    // ====== Status colors ======
    error = AppColorsLight.error,
    onError = AppColorsLight.white,
    errorContainer = AppColorsLight.errorSurface,
    onErrorContainer = AppColorsLight.onErrorSurface,
)

private val DarkColorScheme = darkColorScheme(
    // ====== Core brand colors ======
    primary = AppColorsDark.primary,
    onPrimary = AppColorsDark.white,
    primaryContainer = AppColorsDark.primarySurface,
    onPrimaryContainer = AppColorsDark.black,

    secondary = AppColorsDark.primaryVariant,
    onSecondary = AppColorsDark.white,
    secondaryContainer = AppColorsDark.primaryVariantSurface,
    onSecondaryContainer = AppColorsDark.black,

    // ====== Background & surface ======
    background = AppColorsDark.graySurface,
    onBackground = AppColorsDark.black,
    surface = AppColorsDark.white,
    onSurface = AppColorsDark.black,
    outline = AppColorsDark.grayBorder,

    // ====== Status colors ======
    error = AppColorsDark.error,
    onError = AppColorsDark.white,
    errorContainer = AppColorsDark.errorSurface,
    onErrorContainer = AppColorsDark.onErrorSurface,
)

/**
 * TrueStay Application Theme
 *
 * This composable sets up the theme for the application, including color schemes
 * for light and dark modes, as well as dynamic colors for Android 12 and above.
 *
 * @param darkTheme Boolean flag to indicate if dark theme should be used. Defaults to system setting.
 * @param dynamicColor Boolean flag to indicate if dynamic colors should be used on Android 12+. Defaults to false.
 * @param content Composable content that will be styled with the theme.
 */
@Composable
fun TrueStayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val appColors = if (darkTheme) AppColorsDark else AppColorsLight

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}