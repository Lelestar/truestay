package ca.uqac.inf865.truestay.domain.repository

import kotlinx.coroutines.flow.Flow

interface ThemePreferencesRepository {
    val isDarkTheme: Flow<Boolean>
    suspend fun setDarkTheme(isDark: Boolean)
}