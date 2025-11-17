package ca.uqac.inf865.truestay.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * TrueStay Application Color Palette
 */
interface TrueStayColors {
    // ====== PRIMARY COLORS ======
    val primary: Color
    val primaryLight: Color
    val primarySurface: Color

    // ====== PRIMARY VARIANT COLORS ======
    val primaryVariant: Color
    val primaryVariantSurface: Color

    // ====== BASE COLORS ======
    val white: Color
    val black: Color
    val transparent: Color


    // ====== STATUS COLORS ======
    val success: Color
    val error: Color
    val warning: Color
    val info: Color

    val successSurface: Color
    val warningSurface: Color
    val errorSurface: Color
    val infoSurface: Color

    // Text on colored surfaces
    val onSuccessSurface: Color
    val onErrorSurface: Color
    val onWarningSurface: Color
    val onInfoSurface: Color

    // ====== GRAY SCALE ======
    val grayLight: Color
    val grayDefault: Color
    val grayMedium: Color
    val grayDark: Color
    val graySurface: Color
    val grayBorder: Color
    val yellow: Color
}

/**
 * TrueStay Application Colors - Light Mode
 */
object AppColorsLight : TrueStayColors {
    // ====== PRIMARY COLORS ======
    override val primary: Color = Color(0xFF2F80ED)
    override val primaryLight: Color = Color(0xFF5FA0F2)
    override val primarySurface: Color = Color(0xFFEFF6FF)

    // ====== PRIMARY VARIANT COLORS ======
    override val primaryVariant: Color = Color(0xFF8A2FED)
    override val primaryVariantSurface: Color = Color(0xFFF4E6FF)

    // ====== BASE COLORS ======
    override val white: Color = Color(0xFFFFFFFF)
    override val black: Color = Color(0xFF000000)
    override val transparent: Color = Color(0x00000000)

    // ====== STATUS COLORS ======
    override val success: Color = Color(0xFF27AE60)
    override val error: Color = Color(0xFFEB5757)
    override val warning: Color = Color(0xFFF2C94C)
    override val info: Color = primary

    override val successSurface: Color = Color(0xFFE6F9EF)
    override val warningSurface: Color = Color(0xFFFDF6E2)
    override val errorSurface: Color = Color(0xFFFFEBEB)
    override val infoSurface: Color = primarySurface

    // Text on colored surfaces
    override val onSuccessSurface: Color = Color(0xFF084C2E)
    override val onErrorSurface: Color = Color(0xFF6A1B1B)
    override val onWarningSurface: Color = Color(0xFF7A3206)
    override val onInfoSurface: Color = Color(0xFF0E3A73)

    // ====== GRAY SCALE ======
    override val grayLight: Color = Color(0xFFECECF0)
    override val grayDefault: Color = Color(0xFFBDBDBD)
    override val grayMedium: Color = Color(0xFF7D7E87)
    override val grayDark: Color = Color(0xFF4F4F4F)
    override val graySurface: Color = Color(0xFFF9FAFB)
    override val grayBorder: Color = Color(0xFFE5E5E5)
    override val yellow: Color = Color(0xFFF7C948)
}

/**
 * TrueStay Application Colors - Dark Mode
 */
object AppColorsDark : TrueStayColors {
    // ====== PRIMARY COLORS ======
    override val primary: Color = Color(0xFF2F80ED)
    override val primaryLight: Color = Color(0xFF7EB3F6)
    override val primarySurface: Color = Color(0xFF0E1A2A)

    // ====== PRIMARY VARIANT COLORS ======
    override val primaryVariant: Color = Color(0xFF8A2FED)
    override val primaryVariantSurface: Color = Color(0xFF1A0F2A)

    // ====== BASE COLORS ======
    override val white: Color = Color(0xFF000000)
    override val black: Color = Color(0xFFFFFFFF)
    override val transparent: Color = Color(0x00000000)

    // ====== STATUS COLORS ======
    override val success: Color = Color(0xFF27AE60)
    override val error: Color = Color(0xFFEB5757)
    override val warning: Color = Color(0xFFF2C94C)
    override val info: Color = primary

    override val successSurface: Color = Color(0xFF0D2619)
    override val warningSurface: Color = Color(0xFF2A250B)
    override val errorSurface: Color = Color(0xFF2A1212)
    override val infoSurface: Color = primarySurface

    // Text on colored surfaces
    override val onSuccessSurface: Color = Color(0xFFCFEBD9)
    override val onErrorSurface: Color = Color(0xFFF5C9C9)
    override val onWarningSurface: Color = Color(0xFFEEDCA5)
    override val onInfoSurface: Color = Color(0xFFCCE0FF)

    // ====== GRAY SCALE ======
    override val grayLight: Color = Color(0xFFBFC6CF)
    override val grayDefault: Color = Color(0xFF98A1AB)
    override val grayMedium: Color = Color(0xFF6B7380)
    override val grayDark: Color = Color(0xFF3A404A)
    override val graySurface: Color = Color(0xFF121418)
    override val grayBorder: Color = Color(0xFF2A2F36)
    override val yellow: Color = Color(0xFFF7C948)

}