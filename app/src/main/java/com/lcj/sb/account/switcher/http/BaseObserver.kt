package com.lcj.sb.account.switcher.http

import android.app.Activity
import com.lcj.sb.account.switcher.http.response.BaseResponse
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class BaseObserver<T : BaseResponse>(mActivity: Activity) : Observer<T> {
    override fun onSubscribe(d: Disposable) {

    }

    override fun onError(e: Throwable) {

    }
}