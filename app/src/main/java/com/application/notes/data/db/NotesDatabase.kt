package com.application.notes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 1, exportSchema = false, entities = [Note::class])
abstract class NotesDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NotesDao
}