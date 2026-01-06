package com.taehyeong.commutealarm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class CommuteSettings(
    val isEnabled: Boolean = true,
    val checkInHour: Int = 8,
    val checkInMinute: Int = 30,
    val checkOutHour: Int = 18,
    val checkOutMinute: Int = 30,
    val enabledDays: Set<Int> = setOf(1, 2, 3, 4, 5) // Monday to Friday
) {
    val checkInTime: LocalTime get() = LocalTime.of(checkInHour, checkInMinute)
    val checkOutTime: LocalTime get() = LocalTime.of(checkOutHour, checkOutMinute)
}

object SettingsRepository {
    
    private val IS_ENABLED = booleanPreferencesKey("is_enabled")
    private val CHECK_IN_HOUR = intPreferencesKey("check_in_hour")
    private val CHECK_IN_MINUTE = intPreferencesKey("check_in_minute")
    private val CHECK_OUT_HOUR = intPreferencesKey("check_out_hour")
    private val CHECK_OUT_MINUTE = intPreferencesKey("check_out_minute")
    private val ENABLED_DAYS = stringSetPreferencesKey("enabled_days")
    
    fun getSettings(context: Context): Flow<CommuteSettings> {
        return context.settingsDataStore.data.map { preferences ->
            CommuteSettings(
                isEnabled = preferences[IS_ENABLED] ?: true,
                checkInHour = preferences[CHECK_IN_HOUR] ?: 8,
                checkInMinute = preferences[CHECK_IN_MINUTE] ?: 30,
                checkOutHour = preferences[CHECK_OUT_HOUR] ?: 18,
                checkOutMinute = preferences[CHECK_OUT_MINUTE] ?: 30,
                enabledDays = preferences[ENABLED_DAYS]?.map { it.toInt() }?.toSet() 
                    ?: setOf(1, 2, 3, 4, 5)
            )
        }
    }
    
    suspend fun saveSettings(context: Context, settings: CommuteSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[IS_ENABLED] = settings.isEnabled
            preferences[CHECK_IN_HOUR] = settings.checkInHour
            preferences[CHECK_IN_MINUTE] = settings.checkInMinute
            preferences[CHECK_OUT_HOUR] = settings.checkOutHour
            preferences[CHECK_OUT_MINUTE] = settings.checkOutMinute
            preferences[ENABLED_DAYS] = settings.enabledDays.map { it.toString() }.toSet()
        }
    }
    
    suspend fun setEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[IS_ENABLED] = enabled
        }
    }
}
