package com.application.notes.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.application.notes.data.db.Note
import com.application.notes.repo.NoteRepositoryImpl
import com.application.notes.util.CustomCompletableObserver
import com.application.notes.util.CustomSingleObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepositoryImpl
) : ViewModel() {
    val notesLiveData = MutableLiveData<List<Note>>()
    val successFullInsertNoteLiveData = MutableLiveData(false)
    val successFullDeleteNoteLiveData = MutableLiveData(false)
    val successFullDeleteNotesLiveData = MutableLiveData(false)
    private val compositeDisposable = CompositeDisposable()

    fun insertNote(note: Note) {
        repository.addNewNote(note).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CustomCompletableObserver(compositeDisposable) {
                override fun onComplete() {
                    successFullInsertNoteLiveData.value = true
                }
            })
    }

    fun deleteNote(note: Note) {
        repository.deleteNote(note).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CustomCompletableObserver(compositeDisposable) {
                override fun onComplete() {
                    successFullDeleteNoteLiveData.value = true
                }
            })
    }

    fun getAllNote() {
        repository.getAllNote().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CustomSingleObserver<List<Note>>(compositeDisposable) {
                override fun onSuccess(t: List<Note>) {
                    Timber.i("insert note call -> list %s", t)
                    notesLiveData.value = t
                }
            })
    }

    fun deleteNotes(ids: Array<Int>) {
        repository.deleteNotes(ids).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CustomCompletableObserver(compositeDisposable) {
                override fun onComplete() {
                    successFullDeleteNotesLiveData.value = true
                }
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}