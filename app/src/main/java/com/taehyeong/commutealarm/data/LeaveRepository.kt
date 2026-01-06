package com.taehyeong.commutealarm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.leaveDataStore: DataStore<Preferences> by preferencesDataStore(name = "leaves")

object LeaveRepository {
    
    private val LEAVES_KEY = stringSetPreferencesKey("leave_dates")
    
    suspend fun getLeaves(context: Context): Set<LocalDate> {
        return context.leaveDataStore.data.map { preferences ->
            preferences[LEAVES_KEY]?.map { LocalDate.parse(it) }?.toSet() ?: emptySet()
        }.first()
    }
    
    suspend fun addLeave(context: Context, date: LocalDate) {
        context.leaveDataStore.edit { preferences ->
            val currentLeaves = preferences[LEAVES_KEY]?.toMutableSet() ?: mutableSetOf()
            currentLeaves.add(date.toString())
            preferences[LEAVES_KEY] = currentLeaves
        }
    }
    
    suspend fun removeLeave(context: Context, date: LocalDate) {
        context.leaveDataStore.edit { preferences ->
            val currentLeaves = preferences[LEAVES_KEY]?.toMutableSet() ?: mutableSetOf()
            currentLeaves.remove(date.toString())
            preferences[LEAVES_KEY] = currentLeaves
        }
    }
    
    suspend fun isLeaveDay(context: Context, date: LocalDate): Boolean {
        return getLeaves(context).contains(date)
    }
    
    suspend fun clearAllLeaves(context: Context) {
        context.leaveDataStore.edit { preferences ->
            preferences.remove(LEAVES_KEY)
        }
    }
}
