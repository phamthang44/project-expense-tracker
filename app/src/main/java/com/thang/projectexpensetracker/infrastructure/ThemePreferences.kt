package com.thang.projectexpensetracker.infrastructure

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the user's theme choice ("light" / "dark" / "system") using
 * SharedPreferences so it survives app restarts.
 *
 * Thread-safe singleton — call [getInstance] from anywhere.
 */
class ThemePreferences private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME)

    /** Observable theme mode: "light", "dark", or "system". */
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    /** Persist and broadcast a new theme choice. */
    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME, mode).apply()
        _themeMode.value = mode
    }

    companion object {
        private const val PREFS_NAME    = "theme_prefs"
        private const val KEY_THEME     = "theme_mode"
        private const val DEFAULT_THEME = "system"

        @Volatile
        private var INSTANCE: ThemePreferences? = null

        fun getInstance(context: Context): ThemePreferences =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemePreferences(context).also { INSTANCE = it }
            }
    }
}
