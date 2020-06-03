package com.lcj.sb.account.switcher.http

import android.app.Activity
import com.lcj.sb.account.switcher.http.response.BaseResponse
import io.reactivex.Observer

abstract class BaseObserver<T : BaseResponse>(mActivity: Activity) : Observer<T> {

}