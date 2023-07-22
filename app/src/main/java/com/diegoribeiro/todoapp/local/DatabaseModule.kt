package com.diegoribeiro.todoapp.local

import android.content.Context
import androidx.room.Room
import com.diegoribeiro.todoapp.data.ToDoDao
import com.diegoribeiro.todoapp.data.ToDoDatabase
import com.diegoribeiro.todoapp.utils.Constants.TODO_DATABASE
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
    @Singleton
    fun providesDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, ToDoDatabase::class.java, TODO_DATABASE)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun providesDao(database: ToDoDatabase) : ToDoDao {
        return database.toDoDao()
    }

}