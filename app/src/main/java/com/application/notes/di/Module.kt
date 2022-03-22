package com.application.notes.di

import android.content.Context
import androidx.room.Room
import com.application.notes.data.db.NotesDao
import com.application.notes.data.db.NotesDatabase
import com.application.notes.util.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {
    @Singleton
    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context): NotesDatabase =
        Room.databaseBuilder(context, NotesDatabase::class.java, DATABASE_NAME).build()

    @Singleton
    @Provides
    fun provideDao(database: NotesDatabase): NotesDao {
        return database.getNoteDao()
    }
}