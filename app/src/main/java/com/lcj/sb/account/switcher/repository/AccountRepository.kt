package com.lcj.sb.account.switcher.repository

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import com.lcj.sb.account.switcher.BaseRepository
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.activity.AccountInfoActivity
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.databinding.DialogAccountManagementBinding
import com.lcj.sb.account.switcher.databinding.DialogBackupAccountBinding
import com.lcj.sb.account.switcher.databinding.DialogEditAccountBinding
import com.lcj.sb.account.switcher.model.AccountEditModel
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.FileManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

class AccountRepository(activity: Activity) : BaseRepository(activity) {
    companion object {
        fun getInstance(activity: Activity): AccountRepository {
            return AccountRepository(activity)
        }
    }

    fun onItemClick(account: Account) {
        Intent(activity, AccountInfoActivity::class.java).let {
            it.putExtras(Bundle().apply {
                putSerializable(Configs.INTENT_KEY_ACCOUNT, account)
            })
            activity.startActivity(it)
        }
    }

    fun onDeleteClick(account: Account, onSuccess: () -> Unit, onError: () -> Unit, onNotExists: () -> Unit) {
        AlertDialog.Builder(activity).apply {
            setTitle(activity.getString(R.string.dialog_title_delete_backup))
            setMessage(activity.getString(R.string.dialog_message_delete_backup))
            setPositiveButton(activity.getString(R.string.dialog_button_confirmed)) { dialog, which ->
                val folder = File(account.folder)
                Thread {
                    if (folder.exists()) {
                        if (folder.isDirectory) {
                            folder.listFiles().forEach { childFile ->
                                if (childFile.isDirectory) {
                                    childFile.listFiles().forEach { it.delete() }
                                }
                                childFile.delete()
                            }

                            if (folder.delete()) {
                                BaseDatabase.getInstance(activity).accountDAO().delete(account)
                                onSuccess()
                            } else {
                                onError()
                            }
                        }
                    } else {
                        BaseDatabase.getInstance(activity).accountDAO().delete(account)
                        onNotExists()
                    }
                }.start()
            }
            setNegativeButton(activity.getString(R.string.dialog_button_cancel)) { dialog, which ->
                dialog.dismiss()
            }
        }.create().show()
    }

    fun onEditAliasClick(account: Account) {
        AccountEditModel(account.alias).let { model ->
            val binding = DialogEditAccountBinding.inflate(activity.layoutInflater)
            binding.model = model

            AlertDialog.Builder(activity, R.style.CustomDialog).create().let { dialog ->
                binding.editAccountAliasEdit.setText(account.alias)

                binding.editAccountCancelBtn.setOnClickListener {
                    model.onCancelClick()
                    dialog.dismiss()
                }
                binding.editAccountEditBtn.setOnClickListener {
                    model.onEditClick(activity, account)
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

    fun onSaveClick(account: Account, onSuccess: () -> Unit, onError: () -> Unit) {
        AlertDialog.Builder(activity).apply {
            setTitle("備份遊戲資料")
            setMessage("確定要覆蓋當前備份的資料嗎？")
            setPositiveButton(activity.getString(R.string.dialog_button_confirmed)) { dialog, which ->
                Thread {
                    FileManager.backupFolder(mGameFolderPath, account.folder, object : FileManager.BackupCallback {
                        override fun onProcess(current: Int, total: Int) {
                        }

                        override fun onCompleted() {
                            BaseDatabase.getInstance(activity).accountDAO().update(account.apply {
                                updateTime = System.currentTimeMillis()
                            })
                            dialog.dismiss()
                            onSuccess()
                        }

                        override fun onError() {
                            dialog.dismiss()
                            onError()
                        }
                    })
                }.start()
            }
            setNegativeButton(activity.getString(R.string.dialog_button_cancel)) { dialog, which -> dialog.dismiss() }
        }.create().show()
    }

    fun onLoadGameClick(account: Account, onSuccess: () -> Unit, onError: () -> Unit) {
        val langStr = if (account.lang == Account.Language.JP.ordinal) Configs.PREFIX_NAME_SB_JP else Configs.PREFIX_NAME_SB_TW
        val srcFolder: String = account.folder
        val dstFolder: String = String.format("%s/%s", Configs.PATH_APP_DATA, langStr)

        FileManager.loadFolder(srcFolder, dstFolder, object : FileManager.LoadCallback {
            override fun onCompleted() {
                account.selected = true
                BaseDatabase.getInstance(activity).accountDAO().deselectAll(account.lang)
                BaseDatabase.getInstance(activity).accountDAO().update(account)
                onSuccess()
            }

            override fun onError() {
                onError()
            }
        })
    }

    fun onMoreClick(account: Account) {
        AlertDialog.Builder(activity, R.style.CustomDialog).create().let { dialog ->
            val binding = DialogAccountManagementBinding.inflate(activity.layoutInflater)

            binding.accountAliasTv.text = String.format(activity.getString(R.string.account_alias_text), account.alias)
            binding.accountPathTv.text = String.format(activity.getString(R.string.dialog_folder_path), account.folder)
            binding.accountAliasEdit.setOnClickListener { onEditAliasClick(account); dialog.dismiss() }
//            binding.accountRemoveButton.setOnClickListener { onDeleteClick(account);dialog.dismiss() }
//            binding.accountBackupButton.setOnClickListener { onSaveClick(account); dialog.dismiss() }
//            binding.accountLoadButton.setOnClickListener { onLoadGameClick(account);dialog.dismiss() }

            dialog.show()
            dialog.window?.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
            dialog.setContentView(binding.root)
        }
    }

    fun showCreateAccountDialog(onSuccess: () -> Unit, onNotExists: () -> Unit) {
        AlertDialog.Builder(activity, R.style.CustomDialog).create().let { dialog ->
            val binding = DialogBackupAccountBinding.inflate(activity.layoutInflater)

            binding.backupCancelBtn.setOnClickListener {
                dialog.dismiss()
            }
            binding.backupSubmitBtn.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                val destPath = when (mLanguage) {
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
                                        lang = mLanguage.ordinal,
                                        createTime = currentTime,
                                        updateTime = currentTime
                                )
                                BaseDatabase.getInstance(activity).accountDAO().insert(account)

                                FileManager.backupFolder(mGameFolderPath, destPath, object : FileManager.BackupCallback {
                                    override fun onProcess(current: Int, total: Int) {
                                        binding.backupProgressBar.max = total
                                        binding.backupProgressBar.progress = current
                                    }

                                    override fun onCompleted() {
                                        onSuccess()
                                    }

                                    override fun onError() {
                                    }
                                })
                            } else {
                                onNotExists()
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

    fun showErrorNoInstalled(packageName: String) {
        AlertDialog.Builder(activity)
                .setMessage(R.string.no_install_game)
                .setPositiveButton(R.string.google_play_text) { dialog, which ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                        setPackage("com.android.vending")
                    }
                    activity.startActivity(intent)
                }
                .setNegativeButton(R.string.dialog_button_cancel) { dialog, which ->
                    dialog.dismiss()
                }.create().show()
    }
}