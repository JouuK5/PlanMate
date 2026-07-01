package com.example.todolist.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppSettingImpl(private val context: Context) : AppSetting {
    private val Context.dataStoreAppSetting: DataStore<androidx.datastore.preferences.core.Preferences>
        by preferencesDataStore(name = "app_setting_pref")

    override val appSettingDataFlow: Flow<AppSettingData>
        get()= context.dataStoreAppSetting.data.map { pref ->
            AppSettingData(
                isNotificationEnabled = pref[AppSettingDataStoreKeys.IS_NOTIFICATION_ENABLED] ?: false
            )
        }

    override suspend fun setNotification(isNotificationEnabled: Boolean) = withContext(Dispatchers.IO){
        context.dataStoreAppSetting.edit { pref ->
            pref[AppSettingDataStoreKeys.IS_NOTIFICATION_ENABLED] = isNotificationEnabled
        }
        Unit
    }

    override suspend fun getNotification(): Boolean = withContext(Dispatchers.IO) {
        appSettingDataFlow.first().isNotificationEnabled
    }
}
