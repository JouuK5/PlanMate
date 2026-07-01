package com.example.todolist.data.datastore

import kotlinx.coroutines.flow.Flow

interface AppSetting {
    val appSettingDataFlow: Flow<AppSettingData>

    suspend fun setNotification(isNotificationEnabled: Boolean)
    suspend fun getNotification(): Boolean
}