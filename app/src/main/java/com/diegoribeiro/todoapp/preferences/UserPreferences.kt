package com.diegoribeiro.todoapp.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


const val LAYOUT_PREFERENCES = "layout_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = LAYOUT_PREFERENCES)

class UserPreferences(context: Context) {


    private val mDatastore: DataStore<Preferences> = context.dataStore

    suspend fun storePreferenceLayout(layout: Boolean){
        mDatastore.edit {
            it[LAYOUT_KEY] = layout
        }
    }

    val userLayoutPreference: Flow<Boolean> = mDatastore.data.map {
        it[LAYOUT_KEY] ?: true
    }

    companion object{
        val LAYOUT_KEY = booleanPreferencesKey("GRID_LAYOUT")
    }
}