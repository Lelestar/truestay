package ca.uqac.inf865.truestay.presentation.common.icons

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import ca.uqac.inf865.truestay.R

/**
 * Object containing all TrueStay icons as drawable resource IDs
 * Organized by category for easy navigation and usage
 */
object TrueStayIcons {

    // ============================================
    // NAVIGATION & MAIN FEATURES
    // ============================================
    val Search = R.drawable.ic_search
    val Heart = R.drawable.ic_heart
    val HeartFilled = R.drawable.ic_heart_filled
    val House = R.drawable.ic_house
    val User = R.drawable.ic_user
    val FolderOpen = R.drawable.ic_folder_open

    // ============================================
    // NAVIGATION CONTROLS
    // ============================================
    val ArrowLeft = R.drawable.ic_arrow_left
    val ArrowDownUp = R.drawable.ic_arrow_down_up
    val ChevronDown = R.drawable.ic_chevron_down
    val ChevronUp = R.drawable.ic_chevron_up
    val ChevronRight = R.drawable.ic_chevron_right

    // ============================================
    // PROPERTY & ROOM FEATURES
    // ============================================
    val Bed = R.drawable.ic_bed
    val Bath = R.drawable.ic_bath
    val Building = R.drawable.ic_building
    val Scale = R.drawable.ic_scale
    val MapPin = R.drawable.ic_map_pin
    val Globe = R.drawable.ic_globe
    val Image = R.drawable.ic_image

    // ============================================
    // ACTIONS & EDITING
    // ============================================
    val Plus = R.drawable.ic_plus
    val CirclePlus = R.drawable.ic_circle_plus
    val Check = R.drawable.ic_check
    val X = R.drawable.ic_x
    val Trash = R.drawable.ic_trash_2
    val PenLine = R.drawable.ic_pen_line
    val PenTool = R.drawable.ic_pen_tool
    val SquarePen = R.drawable.ic_square_pen
    val Eraser = R.drawable.ic_eraser

    // ============================================
    // INVENTORY & DOCUMENTS
    // ============================================
    val ClipboardCheck = R.drawable.ic_clipboard_check
    val FileText = R.drawable.ic_file_text
    val Camera = R.drawable.ic_camera
    val Download = R.drawable.ic_download
    val Square = R.drawable.ic_square

    // ============================================
    // COMMUNICATION
    // ============================================
    val Mail = R.drawable.ic_mail
    val Phone = R.drawable.ic_phone
    val MessageSquare = R.drawable.ic_message_square
    val Bell = R.drawable.ic_bell

    // ============================================
    // AUTHENTICATION & SECURITY
    // ============================================
    val Lock = R.drawable.ic_lock
    val Key = R.drawable.ic_key
    val ShieldCheck = R.drawable.ic_shield_check
    val LogOut = R.drawable.ic_log_out
    val Eye = R.drawable.ic_eye
    val EyeOff = R.drawable.ic_eye_off

    // ============================================
    // STATUS & ALERTS
    // ============================================
    val CircleAlert = R.drawable.ic_circle_alert
    val Danger = R.drawable.ic_danger
    val Star = R.drawable.ic_star
    val StarFilled = R.drawable.ic_star_filled
    val TrendingUp = R.drawable.ic_trending_up
    val TrendingDown = R.drawable.ic_trending_down

    // ============================================
    // UTILITY
    // ============================================
    val Calendar = R.drawable.ic_calendar
    val History = R.drawable.ic_history
    val Funnel = R.drawable.ic_funnel
    val EllipsisVertical = R.drawable.ic_elipisis_vertical
    val SunMoon = R.drawable.ic_sun_moon
    val Map = R.drawable.ic_map
}

/**
 * Helper function to load an ImageVector from a drawable resource ID
 */
@Composable
fun rememberTrueStayIcon(@DrawableRes iconRes: Int): ImageVector {
    return ImageVector.vectorResource(id = iconRes)
}