package com.example.todolist.di

import android.content.Context
import com.example.todolist.data.datastore.AppSetting
import com.example.todolist.data.datastore.AppSettingImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAppSetting(@ApplicationContext context: Context): AppSetting {
        // Trả về class Implementation của bạn
        return AppSettingImpl(context)
    }
}