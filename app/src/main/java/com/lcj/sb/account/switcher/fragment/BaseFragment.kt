package com.lcj.sb.account.switcher.fragment

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseUser
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.utils.Configs

/**
 * Created by Larry on 2018-07-01.
 */
open class BaseFragment : Fragment() {
    val LOG_TAG: String = javaClass.simpleName
    lateinit var mActivity: Activity
    var mHandler: Handler = Handler()

    var mRootView: View? = null

    protected lateinit var mCurrentLang: Account.Language
    protected lateinit var mCurrentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mCurrentUser = FirebaseAuth.getInstance().currentUser!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.i(LOG_TAG, "onActivityCreated")
        mActivity = activity!!

        PreferenceManager.getDefaultSharedPreferences(mActivity).apply {
            mCurrentLang = Account.Language.valueOf(getString(Configs.PREF_KEY_LANGUAGE, Account.Language.JP.name)!!)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(LOG_TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i(LOG_TAG, "onResume")
    }
}