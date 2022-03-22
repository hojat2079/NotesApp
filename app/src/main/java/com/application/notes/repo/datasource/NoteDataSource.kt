package com.application.notes.repo.datasource

import com.application.notes.data.db.Note
import io.reactivex.Completable
import io.reactivex.Single

interface NoteDataSource {
    fun deleteNote(note: Note): Completable

    fun addNewNote(note: Note): Completable

    fun getAllNote(): Single<List<Note>>

    fun deleteNotes(ids: Array<Int>):Completable
}