package com.test.emp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StoreUser(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("User")
        val USER_KEY = stringPreferencesKey("user")
        val Bool_KEy = booleanPreferencesKey("status")
        val Token_Key = stringPreferencesKey("token")
    }

    //Function to get the user data
    val getUser: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_KEY] ?: ""
        }

    //Function to save the user in database preferences
    suspend fun saveUser(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_KEY] = name
        }
    }


    val getStatus : Flow<Boolean?> =  context.dataStore.data
        .map { preferences ->
            preferences[Bool_KEy] ?: true
        }

    suspend fun saveStatus(name: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Bool_KEy] = name
        }
    }

    suspend fun clear(){
        context.dataStore.edit {
            it.clear()
        }
    }
    val getToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[Token_Key] ?: ""
        }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[Token_Key] = token
        }
    }
}