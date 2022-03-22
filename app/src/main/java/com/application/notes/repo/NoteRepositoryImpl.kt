package com.application.notes.repo

import com.application.notes.data.db.Note
import com.application.notes.repo.datasource.NoteLocalDataSource
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@ActivityRetainedScoped
class NoteRepositoryImpl @Inject constructor(
    private val dataSource: NoteLocalDataSource
) :
    NoteRepository {
    override fun deleteNote(note: Note): Completable {
        return dataSource.deleteNote(note)
    }

    override fun deleteNotes(ids: Array<Int>): Completable {
        return dataSource.deleteNotes(ids)
    }

    override fun addNewNote(note: Note): Completable {
     return dataSource.addNewNote(note)
    }

    override fun getAllNote(): Single<List<Note>> {
        return dataSource.getAllNote()
    }
}