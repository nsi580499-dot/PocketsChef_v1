package es.uc3m.android.pockets_chef_app.ui.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

object LocaleHelper {

    private const val PREFS_NAME = "pocketschef_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    fun setLocale(context: Context, languageCode: String) {
        saveLanguage(context, languageCode)
        applyLocale(context, languageCode)
    }

    fun applyLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getSavedLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE, "en") ?: "en"
    }

    private fun saveLanguage(context: Context, languageCode: String) {
        getPrefs(context).edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun restartActivity(activity: Activity) {
        activity.recreate()
    }
}

val supportedLanguages = listOf(
    "en" to "English",
    "es" to "Español",
    "fr" to "Français",
    "it" to "Italiano",
    "de" to "Deutsch"
)