package com.example.todolist.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey

object AppSettingDataStoreKeys {
    val IS_NOTIFICATION_ENABLED = booleanPreferencesKey("is_notification_enabled")
}