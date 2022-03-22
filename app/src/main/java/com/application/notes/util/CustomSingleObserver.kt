package com.application.notes.util

import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class CustomSingleObserver<T>(private val disposable: CompositeDisposable) :
    SingleObserver<T> {
    override fun onSubscribe(d: Disposable) {
        disposable.addAll(d)
    }

    override fun onError(e: Throwable) {
        //todo
    }
}