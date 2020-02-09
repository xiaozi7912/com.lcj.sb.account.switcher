package com.lcj.sb.account.switcher.fragment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lcj.sb.account.switcher.BaseApplication
import com.lcj.sb.account.switcher.BaseFragment
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.activity.AccountInfoActivity
import com.lcj.sb.account.switcher.adapter.AccountAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.DialogAccountManagementBinding
import com.lcj.sb.account.switcher.databinding.DialogBackupAccountBinding
import com.lcj.sb.account.switcher.databinding.DialogEditAccountBinding
import com.lcj.sb.account.switcher.databinding.FragmentAccountBinding
import com.lcj.sb.account.switcher.model.AccountEditModel
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.FileManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AccountFragment : BaseFragment() {
    private lateinit var mBinding: FragmentAccountBinding
    private lateinit var mGameFolderPath: String
    private lateinit var mDisplayLang: Account.Language

    companion object {
        fun newInstance(): AccountFragment {
            return AccountFragment()
        }

        fun newInstance(lang: Account.Language): AccountFragment {
            return AccountFragment().apply {
                arguments = Bundle().apply {
                    putInt(Configs.PREF_KEY_LANGUAGE, lang.ordinal)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentAccountBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDisplayLang = Account.Language.values()[arguments?.getInt(Configs.PREF_KEY_LANGUAGE)!!]

        mBinding.addFab.setOnClickListener {
            getPackageName().let {
                if (FileManager.isPackageInstalled(it, mActivity)) {
                    showBackupAccountDialog()
                } else {
                    showErrorNoInstalled(it)
                }
            }
        }

        mBinding.gameFab.setOnClickListener {
            getPackageName().let {
                if (FileManager.isPackageInstalled(it, mActivity)) {
                    startApplication(it)
                } else {
                    showErrorNoInstalled(it)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val adapter = AccountAdapter(mActivity)
        adapter.setOnClickListener(object : AccountAdapter.OnClickListener {
            override fun onItemClick(holder: AccountAdapter.ViewHolder, account: Account) {
                Intent(mActivity, AccountInfoActivity::class.java).let {
                    it.putExtras(Bundle().apply {
                        putSerializable(Configs.INTENT_KEY_ACCOUNT, account)
                    })
                    startActivity(it)
                }
            }

            override fun onMoreClick(holder: AccountAdapter.ViewHolder, account: Account) {
                showAccountManagement(account)
            }
        })

        mBinding.accountList.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        mBinding.accountList.adapter = adapter

        BaseDatabase.getInstance(mActivity).accountDAO()
                .liveAccounts(mDisplayLang.ordinal, false)
                .observe(this, Observer { adapter.update(it) })
    }

    override fun onResume() {
        super.onResume()
        mActivity.invalidateOptionsMenu()

        when (mDisplayLang) {
            Account.Language.JP -> {
                mGameFolderPath = String.format("%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_JP)
                BaseApplication.setCurrentScreen(mActivity, Configs.SCREEN_SB_JP, LOG_TAG)
            }
            Account.Language.TW -> {
                mGameFolderPath = String.format("%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_TW)
                BaseApplication.setCurrentScreen(mActivity, Configs.SCREEN_SB_TW, LOG_TAG)
            }
        }

        PreferenceManager.getDefaultSharedPreferences(mActivity).edit().apply {
            putBoolean(Configs.PREF_KEY_FIRST_RUN, false)
            putString(Configs.PREF_KEY_LANGUAGE, mDisplayLang.name)
            apply()
        }
    }

    private fun showAccountManagement(account: Account) {
        AlertDialog.Builder(mActivity, R.style.CustomDialog).create().let { dialog ->
            val binding = DialogAccountManagementBinding.inflate(layoutInflater)

            binding.accountAliasTv.text = String.format(getString(R.string.account_alias_text), account.alias)
            binding.accountPathTv.text = String.format(getString(R.string.dialog_folder_path), account.folder)
            binding.accountAliasEdit.setOnClickListener { onEditClick(null, account); dialog.dismiss() }
            binding.accountRemoveButton.setOnClickListener { onRemoveClick(account);dialog.dismiss() }
            binding.accountBackupButton.setOnClickListener { onBackupClick(null, account); dialog.dismiss() }
            binding.accountLoadButton.setOnClickListener { onLoadClick(null, account);dialog.dismiss() }

            dialog.show()
            dialog.window?.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
            dialog.setContentView(binding.root)
        }
    }

    private fun onEditClick(holder: AccountAdapter.ViewHolder?, account: Account) {
        AccountEditModel(account.alias).let { model ->
            val binding = DialogEditAccountBinding.inflate(layoutInflater)
            binding.model = model

            AlertDialog.Builder(mActivity, R.style.CustomDialog).create().let { dialog ->
                binding.editAccountAliasEdit.setText(account.alias)

                binding.editAccountCancelBtn.setOnClickListener {
                    model.onCancelClick()
                    dialog.dismiss()
                }
                binding.editAccountEditBtn.setOnClickListener {
                    model.onEditClick(mActivity, account)
                    dialog.dismiss()
                }

                dialog.show()
                dialog.window?.apply {
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
                dialog.setContentView(binding.root)
            }
        }
    }

    private fun onLoadClick(holder: AccountAdapter.ViewHolder?, account: Account) {
        val langStr = if (account.lang == Account.Language.JP.ordinal) Configs.PREFIX_NAME_SB_JP else Configs.PREFIX_NAME_SB_TW
        val srcFolder: String = String.format("%s/%s", account.folder, "files")
        val dstFolder: String = String.format("%s/%s", Configs.PATH_APP_DATA, langStr)

        FileManager.loadFolder(srcFolder, dstFolder, object : FileManager.LoadCallback {
            override fun onCompleted() {
                mHandler.post { mBinding.gameFab.performClick() }
            }

            override fun onError() {

            }
        })
        setAccountSelected(account)
    }

    private fun onBackupClick(holder: AccountAdapter.ViewHolder?, account: Account) {
        AlertDialog.Builder(mActivity).apply {
            setTitle("備份遊戲資料")
            setMessage("確定要覆蓋當前備份的資料嗎？")
            setPositiveButton(getString(R.string.dialog_button_confirmed)) { dialog, which ->
                Thread {
                    FileManager.backupFolder(mGameFolderPath, account.folder, object : FileManager.BackupCallback {
                        override fun onProcess(current: Int, total: Int) {
                        }

                        override fun onCompleted() {
                            BaseDatabase.getInstance(mActivity).accountDAO().update(account.apply {
                                updateTime = System.currentTimeMillis()
                            })

                            mHandler.post {
                                Snackbar.make(mContentView, "備份成功", Snackbar.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                        }

                        override fun onError() {
                            mHandler.post {
                                Snackbar.make(mContentView, getString(R.string.game_folder_not_exists), Snackbar.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                        }
                    })
                }.start()
            }
            setNegativeButton(getString(R.string.dialog_button_cancel)) { dialog, which -> dialog.dismiss() }
        }.create().show()

    }

    private fun onRemoveClick(account: Account) {
        Thread {
            BaseDatabase.getInstance(mActivity).accountDAO().update(account.apply {
                hidden = true
            })
        }.start()
    }

    private fun setAccountSelected(account: Account) {
        Thread {
            account.selected = true
            BaseDatabase.getInstance(mActivity).accountDAO().deselectAll(account.lang)
            BaseDatabase.getInstance(mActivity).accountDAO().update(account)
        }.start()
    }

    private fun showBackupAccountDialog() {
        AlertDialog.Builder(mActivity, R.style.CustomDialog).create().let { dialog ->
            val binding = DialogBackupAccountBinding.inflate(layoutInflater)

            binding.backupCancelBtn.setOnClickListener {
                dialog.dismiss()
            }
            binding.backupSubmitBtn.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                val destPath = when (mDisplayLang) {
                    Account.Language.JP -> String.format("%s/%s.%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_JP, currentTime)
                    Account.Language.TW -> String.format("%s/%s.%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_TW, currentTime)
                }

                it.isEnabled = false
                Observable.just(FileManager.isFolderExists(mGameFolderPath))
                        .subscribeOn(Schedulers.io())
                        .doOnNext { exists ->
                            if (exists) {
                                val alias = if (binding.backupInputEt.text.isNotEmpty()) {
                                    binding.backupInputEt.text.toString()
                                } else {
                                    destPath.substring(destPath.lastIndexOf("/") + 1)
                                }

                                val account = Account(
                                        alias = alias,
                                        folder = destPath,
                                        lang = mDisplayLang.ordinal,
                                        createTime = currentTime,
                                        updateTime = currentTime
                                )
                                BaseDatabase.getInstance(mActivity).accountDAO().insert(account)

                                FileManager.backupFolder(mGameFolderPath, destPath, object : FileManager.BackupCallback {
                                    override fun onProcess(current: Int, total: Int) {
                                        binding.backupProgressBar.max = total
                                        binding.backupProgressBar.progress = current
                                    }

                                    override fun onCompleted() {
                                    }

                                    override fun onError() {
                                    }
                                })
                            } else {
                                mHandler.post { Snackbar.make(mContentView, getString(R.string.game_folder_not_exists), Snackbar.LENGTH_SHORT).show() }
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { dialog.dismiss() }
            }

            dialog.show()
            dialog.window?.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
            dialog.setContentView(binding.root)
        }
    }

    private fun showErrorNoInstalled(packageName: String) {
        AlertDialog.Builder(mActivity)
                .setMessage(R.string.no_install_game)
                .setPositiveButton(R.string.google_play_text) { dialog, which ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                        setPackage("com.android.vending")
                    }
                    startActivity(intent)
                }
                .setNegativeButton(R.string.dialog_button_cancel) { dialog, which ->
                    dialog.dismiss()
                }.create().show()
    }
}