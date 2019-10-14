package com.lcj.sb.account.switcher.fragment

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Created by Larry on 2018-07-01.
 */
open class BaseFragment : Fragment() {
    val LOG_TAG: String = javaClass.simpleName
    var mActivity: Activity? = null
    var mHandler: Handler = Handler()

    var mRootView: View? = null

    protected lateinit var mCurrentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity
        mCurrentUser = FirebaseAuth.getInstance().currentUser!!
    }
}