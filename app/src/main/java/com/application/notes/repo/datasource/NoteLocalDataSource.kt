package com.application.notes.repo.datasource

import com.application.notes.data.db.Note
import com.application.notes.data.db.NotesDao
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class NoteLocalDataSource @Inject constructor(
    private val notesDao: NotesDao
) : NoteDataSource {
    override fun deleteNote(note: Note): Completable {
        return notesDao.deleteNote(note)
    }

    override fun addNewNote(note: Note): Completable {
        return notesDao.addNewNote(note)
    }

    override fun getAllNote(): Single<List<Note>> {
        return notesDao.getAllNote()
    }

    override fun deleteNotes(ids: Array<Int>): Completable {
        return notesDao.deleteNotes(ids)
    }
}