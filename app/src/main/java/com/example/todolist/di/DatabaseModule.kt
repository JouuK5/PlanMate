package com.example.todolist.di

import android.content.Context
import com.example.todolist.App
import com.example.todolist.data.local.AppDatabase
import com.example.todolist.data.local.TodoDao
import com.example.todolist.data.repository.TodoRepo
import com.example.todolist.data.repository.TodoRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideTodoDao(appDb: AppDatabase): TodoDao{
        return appDb.todoDao()
    }

    @Singleton
    @Provides
    fun provideAppDb(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.invoke(context)
    }
}