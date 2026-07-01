package com.example.todolist.di

import com.example.todolist.data.local.TodoDao
import com.example.todolist.data.repository.TodoRepo
import com.example.todolist.data.repository.TodoRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {
    @Singleton
    @Provides
    fun provideTodoRepo(todoDao: TodoDao): TodoRepo{
        return TodoRepoImpl(todoDao)
    }
}