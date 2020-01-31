package com.lcj.sb.account.switcher.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.lcj.sb.account.switcher.BaseApplication
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
        mBinding.settingsSbJSyncBtn.setOnClickListener { onSyncJPButtonClick() }
        mBinding.settingsSbTSyncBtn.setOnClickListener { onSyncTWButtonClick() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        updateSyncView(Account.Language.JP)
        updateSyncView(Account.Language.TW)
    }

    override fun onResume() {
        super.onResume()
        BaseApplication.analytics.setCurrentScreen(mActivity, Configs.SCREEN_LOCAL_BACKUP, LOG_TAG)
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

    private fun onSyncJPButtonClick() {
        Log.i(LOG_TAG, "onSyncJPButtonClick")
        FileManager.syncBackupFolder(mActivity, Account.Language.JP) {
            updateSyncView(Account.Language.JP)
            Toast.makeText(mActivity, "同步成功！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSyncTWButtonClick() {
        Log.i(LOG_TAG, "onSyncTWButtonClick")
        FileManager.syncBackupFolder(mActivity, Account.Language.TW) {
            updateSyncView(Account.Language.TW)
            Toast.makeText(mActivity, "同步成功！", Toast.LENGTH_SHORT).show()
        }
    }
}