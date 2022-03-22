package com.application.notes.data.db

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface NotesDao {
    @Delete
    fun deleteNote(note: Note): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNewNote(note: Note): Completable

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNote(): Single<List<Note>>

    @Query("DELETE FROM notes WHERE id IN (:ids) ")
    fun deleteNotes(ids: Array<Int>):Completable
}