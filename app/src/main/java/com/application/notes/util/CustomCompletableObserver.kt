package com.application.notes.util

import io.reactivex.CompletableObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class CustomCompletableObserver(private val compositeDisposable: CompositeDisposable) :
    CompletableObserver {
    override fun onSubscribe(d: Disposable) {
        compositeDisposable.addAll(d)
    }

    override fun onError(e: Throwable) {
       //todo
    }
}