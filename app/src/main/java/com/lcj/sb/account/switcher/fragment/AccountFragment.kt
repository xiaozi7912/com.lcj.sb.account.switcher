package com.lcj.sb.account.switcher.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lcj.sb.account.switcher.adapter.AccountAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.DialogBackupAccountBinding
import com.lcj.sb.account.switcher.databinding.FragmentAccountBinding
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.FileManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AccountFragment : BaseFragment() {
    private lateinit var mBinding: FragmentAccountBinding
    private lateinit var mAdapter: AccountAdapter
    private lateinit var mAlertDialog: AlertDialog
    private lateinit var mGameFolderPath: String

    companion object {
        fun newInstance(): AccountFragment {
            return AccountFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentAccountBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.addFab.setOnClickListener {
            showBackupAccountDialog()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(LOG_TAG, "onActivityCreated ${mCurrentLang.name}")
        val layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)

        mGameFolderPath = when (mCurrentLang) {
            Account.Language.JP -> String.format("%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_JP)
            Account.Language.TW -> String.format("%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_TW)
        }
        mAdapter = AccountAdapter(mActivity)
        mAdapter.setSaveButtonClick { holder, account ->
            Log.v(LOG_TAG, "onActivityCreated ${account.alias}")
            Log.v(LOG_TAG, "onActivityCreated ${account.folder}")
            Thread {
                FileManager.backupFolder(mGameFolderPath, account.folder) { current, total, finished ->
                    Log.v(LOG_TAG, "onActivityCreated current : $current")
                    Log.v(LOG_TAG, "onActivityCreated total : $total")
                    Log.v(LOG_TAG, "onActivityCreated finished : $finished")
                }
            }.start()
        }
        mBinding.accountList.addItemDecoration(DividerItemDecoration(mActivity, layoutManager.orientation))
        mBinding.accountList.layoutManager = layoutManager
        mBinding.accountList.adapter = mAdapter

        BaseDatabase.getInstance(mActivity).accountDAO()
                .loadAccounts(mCurrentLang.ordinal)
                .observe(this, Observer { mAdapter.update(it) })
    }

    private fun showBackupAccountDialog() {
        val builder = AlertDialog.Builder(mActivity)
        val binding = DialogBackupAccountBinding.inflate(layoutInflater)

        builder.setView(binding.root)
        mAlertDialog = builder.create()
        mAlertDialog.show()

        binding.backupCancelBtn.setOnClickListener {
            Log.i(LOG_TAG, "backupCancelBtn")
            mAlertDialog.dismiss()
        }
        binding.backupSubmitBtn.setOnClickListener {
            Log.i(LOG_TAG, "backupSubmitBtn")
            val currentTime = System.currentTimeMillis()


            val destPath = when (mCurrentLang) {
                Account.Language.JP -> String.format("%s/%s.%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_JP, currentTime)
                Account.Language.TW -> String.format("%s/%s.%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_TW, currentTime)
            }

            Log.v(LOG_TAG, "mGameFolderPath $mGameFolderPath")
            Log.v(LOG_TAG, "isFolderExists ${FileManager.isFolderExists(mGameFolderPath)}")
            it.isEnabled = false
            Observable.just(FileManager.isFolderExists(mGameFolderPath))
                    .subscribeOn(Schedulers.io())
                    .doOnNext { exists ->
                        val account = Account(
                                alias = binding.backupInputEt.text.toString(),
                                folder = destPath,
                                lang = mCurrentLang.ordinal,
                                createTime = currentTime,
                                updateTime = currentTime
                        )
                        BaseDatabase.getInstance(mActivity)
                                .accountDAO()
                                .insertAccount(account)

                        if (exists) FileManager.backupFolder(mGameFolderPath, destPath) { current, total, finished ->
                            binding.backupProgressBar.max = total
                            binding.backupProgressBar.progress = current
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { mAlertDialog.dismiss() }
        }
    }
}