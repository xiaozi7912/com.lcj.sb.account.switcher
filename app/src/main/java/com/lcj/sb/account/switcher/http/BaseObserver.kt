package com.lcj.sb.account.switcher.http

import android.app.Activity
import com.lcj.sb.account.switcher.http.response.BaseResponse
import com.lcj.sb.account.switcher.view.ProgressDialog
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class BaseObserver<T : BaseResponse>(
        private val mActivity: Activity,
        private val mShowProgress: Boolean = true)
    : Observer<T> {
    override fun onSubscribe(d: Disposable) {
        if (mShowProgress) ProgressDialog.getInstance(mActivity).show()
    }

    override fun onNext(response: T) {
        if (response.code == 200) {
            onSuccess(response)
        } else {
            onFailure(response)
        }
    }

    override fun onError(e: Throwable) {
        ProgressDialog.getInstance(mActivity).dismiss()
    }

    override fun onComplete() {
        ProgressDialog.getInstance(mActivity).dismiss()
    }

    abstract fun onSuccess(response: T)

    open fun onFailure(response: T) {

    }
}