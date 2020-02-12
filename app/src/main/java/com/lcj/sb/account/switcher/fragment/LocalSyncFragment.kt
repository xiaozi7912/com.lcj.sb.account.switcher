package com.lcj.sb.account.switcher.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.lcj.sb.account.switcher.BaseApplication
import com.lcj.sb.account.switcher.BaseFragment
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.activity.LocalSyncListActivity
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.FolderSync
import com.lcj.sb.account.switcher.databinding.FragmentLocalBackupBinding
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.FileManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat

class LocalSyncFragment : BaseFragment() {
    private lateinit var mBinding: FragmentLocalBackupBinding

    companion object {
        fun newInstance(): LocalSyncFragment {
            return LocalSyncFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentLocalBackupBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.settingsSbJRoot.setOnClickListener { showLocalSyncList(Account.Language.JP) }
        mBinding.settingsSbJSyncBtn.setOnClickListener { onSyncJPButtonClick() }
        mBinding.settingsSbTRoot.setOnClickListener { showLocalSyncList(Account.Language.TW) }
        mBinding.settingsSbTSyncBtn.setOnClickListener { onSyncTWButtonClick() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        updateSyncView(Account.Language.JP)
        updateSyncView(Account.Language.TW)
    }

    override fun onResume() {
        super.onResume()
        BaseApplication.setCurrentScreen(mActivity, Configs.SCREEN_LOCAL_BACKUP, LOG_TAG)
    }

    private fun updateSyncView(lang: Account.Language) {
        val type = FolderSync.Type.LOCAL
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val d = BaseDatabase.getInstance(mActivity).folderSyncDAO().folderSync(type.ordinal, lang.ordinal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ entity ->
                    entity?.let {
                        when (lang) {
                            Account.Language.JP -> mBinding.settingsSbJSyncTimeTv.text = sdf.format(it.updateTime)
                            Account.Language.TW -> mBinding.settingsSbTSyncTimeTv.text = sdf.format(it.updateTime)
                        }
                    }
                }, { err -> err.printStackTrace() })
    }

    private fun showLocalSyncList(lang: Account.Language) {
//        startActivity(Intent(mActivity, LocalSyncListActivity::class.java).apply {
//            putExtra(Configs.INTENT_KEY_LANGUAGE, lang.name)
//        })
    }

    private fun onSyncJPButtonClick() {
        Log.i(LOG_TAG, "onSyncJPButtonClick")
        FileManager.syncBackupFolder(mActivity, Account.Language.JP) {
            updateSyncView(Account.Language.JP)
            Snackbar.make(mContentView, getString(R.string.sync_account_completed), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun onSyncTWButtonClick() {
        Log.i(LOG_TAG, "onSyncTWButtonClick")
        FileManager.syncBackupFolder(mActivity, Account.Language.TW) {
            updateSyncView(Account.Language.TW)
            Snackbar.make(mContentView, getString(R.string.sync_account_completed), Snackbar.LENGTH_SHORT).show()
        }
    }
}