package ca.uqac.inf865.truestay.presentation.common.utils

object DateUtils {
    fun formatLocalDate(timestamp: Long): String? {
        if (timestamp <= 0L) return null
        return try {
            val formatter = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM)
            formatter.format(java.util.Date(timestamp))
        } catch (_: Exception) {
            null
        }
    }
}

