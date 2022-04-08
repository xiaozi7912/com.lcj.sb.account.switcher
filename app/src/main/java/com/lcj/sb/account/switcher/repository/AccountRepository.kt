package com.lcj.sb.account.switcher.repository

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.documentfile.provider.DocumentFile
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
import io.reactivex.internal.operators.completable.CompletableFromAction
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

    fun onDeleteClick(account: Account, callback: DeleteAccountCallback) {
        AlertDialog.Builder(activity).apply {
            setTitle(activity.getString(R.string.dialog_title_delete_backup))
            setMessage(activity.getString(R.string.dialog_message_delete_backup))
            setPositiveButton(activity.getString(R.string.dialog_button_confirmed)) { _, _ ->
                val folder = File(account.folder)
                val d = CompletableFromAction.fromAction {
                    if (folder.exists()) {
                        if (folder.isDirectory) {
                            folder.listFiles()?.forEach { childFile ->
                                if (childFile.isDirectory) childFile.listFiles()?.forEach { it.delete() }
                                childFile.delete()
                            }

                            if (folder.delete()) {
                                BaseDatabase.getInstance(activity).accountDAO().delete(account)
                                callback.onSuccess()
                            } else {
                                callback.onError(activity.getString(R.string.dialog_message_delete_fail))
                            }
                        }
                    } else {
                        BaseDatabase.getInstance(activity).accountDAO().delete(account)
                        callback.onNotExists()
                    }
                }
                    .subscribeOn(Schedulers.io())
                    .subscribe { }
            }
            setNegativeButton(activity.getString(R.string.dialog_button_cancel)) { dialog, _ -> dialog.dismiss() }
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

    fun onSaveClick(lang: Account.Language, account: Account, callback: SaveAccountCallback) {
        AlertDialog.Builder(activity).apply {
            setTitle("備份遊戲資料")
            setMessage("確定要覆蓋當前備份的資料嗎？")
            setPositiveButton(activity.getString(R.string.dialog_button_confirmed)) { dialog, _ ->
                val d = CompletableFromAction.fromAction {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val gameDirName = lang.packageName
                        val replacedName = account.folder.replace("%2F", "/", true)
                        val destDirName = replacedName.substring(replacedName.lastIndexOf("/") + 1)
                        val rootDir = DocumentFile.fromTreeUri(activity, Uri.parse(Configs.URI_ANDROID_DATA))

                        rootDir?.let {
                            FileManager.backupFolder(activity.contentResolver, rootDir, gameDirName, destDirName,
                                object : FileManager.BackupCallback {
                                    override fun onProcess(current: Int, total: Int) {
                                    }

                                    override fun onCompleted(folderPath: String) {
                                        BaseDatabase.getInstance(activity).accountDAO().update(account.apply {
                                            updateTime = System.currentTimeMillis()
                                        })
                                        dialog.dismiss()
                                        callback.onSuccess()
                                    }

                                    override fun onError(message: String) {
                                        dialog.dismiss()
                                        callback.onError(activity.getString(R.string.game_folder_not_exists))
                                    }
                                })
                        }
                    } else {
                        val gameFolderPath = String.format("%s/%s", Configs.PATH_APP_DATA, lang.packageName)
                        FileManager.backupFolder(gameFolderPath, account.folder, object : FileManager.BackupCallback {
                            override fun onProcess(current: Int, total: Int) {
                            }

                            override fun onCompleted(folderPath: String) {
                                BaseDatabase.getInstance(activity).accountDAO().update(account.apply {
                                    updateTime = System.currentTimeMillis()
                                })
                                dialog.dismiss()
                                callback.onSuccess()
                            }

                            override fun onError(message: String) {
                                dialog.dismiss()
                                callback.onError(activity.getString(R.string.game_folder_not_exists))
                            }
                        })
                    }
                }
                    .subscribeOn(Schedulers.io())
                    .subscribe {}
            }
            setNegativeButton(activity.getString(R.string.dialog_button_cancel)) { dialog, _ -> dialog.dismiss() }
        }.create().show()
    }

    fun onLoadGameClick(account: Account, callback: LoadAccountCallback) {
        val destDirName = if (account.lang == Account.Language.JP.ordinal) Configs.PREFIX_NAME_SB_JP else Configs.PREFIX_NAME_SB_TW

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            DocumentFile.fromTreeUri(activity, Uri.parse(Configs.URI_ANDROID_DATA))?.let {
                val replacedName = account.folder.replace("%2F", "/")
                val srcDirName = replacedName.substring(replacedName.lastIndexOf("/") + 1)

                FileManager.loadFolder(activity.contentResolver, it, srcDirName, destDirName, object : FileManager.LoadCallback {
                    override fun onCompleted() {
                        account.selected = true
                        BaseDatabase.getInstance(activity).accountDAO().deselectAll(account.lang)
                        BaseDatabase.getInstance(activity).accountDAO().update(account)
                        callback.onSuccess()
                    }

                    override fun onError() {
                        callback.onError("Error")
                    }
                })
            } ?: run {
                callback.onError("沒有資料夾存取權限。")
            }
        } else {
            val srcFolder: String = account.folder
            val dstFolder: String = String.format("%s/%s", Configs.PATH_APP_DATA, destDirName)
            
            FileManager.loadFolder(srcFolder, dstFolder, object : FileManager.LoadCallback {
                override fun onCompleted() {
                    account.selected = true
                    BaseDatabase.getInstance(activity).accountDAO().deselectAll(account.lang)
                    BaseDatabase.getInstance(activity).accountDAO().update(account)
                    callback.onSuccess()
                }

                override fun onError() {
                    callback.onError("Error")
                }
            })
        }
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

    fun showCreateAccountDialog(lang: Account.Language, callback: CreateAccountCallback) {
        AlertDialog.Builder(activity, R.style.CustomDialog).create().let { dialog ->
            val binding = DialogBackupAccountBinding.inflate(activity.layoutInflater)

            binding.backupCancelBtn.setOnClickListener {
                dialog.dismiss()
            }
            binding.backupSubmitBtn.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    startBackup(dialog, binding, lang, callback)
                } else {
                    startLegacyBackup(dialog, binding, lang, callback)
                }
            }

            dialog.setCancelable(false)
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
            .setPositiveButton(R.string.google_play_text) { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    setPackage("com.android.vending")
                }
                activity.startActivity(intent)
            }
            .setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    private fun startBackup(dialog: AlertDialog, binding: DialogBackupAccountBinding, lang: Account.Language, callback: CreateAccountCallback) {
        val currentTime = System.currentTimeMillis()
        val gameDirName = lang.packageName
        val destFolderName = String.format("%s.%s", gameDirName, currentTime)
        val rootDir = DocumentFile.fromTreeUri(activity, Uri.parse(Configs.URI_ANDROID_DATA))

        binding.backupSubmitBtn.isEnabled = false
        val d = CompletableFromAction.fromAction {
            rootDir?.let {
                val exists = FileManager.isFolderExists(rootDir, gameDirName)
                if (exists) {
                    val alias = if (binding.backupInputEt.text.isNotEmpty()) {
                        binding.backupInputEt.text.toString()
                    } else {
                        destFolderName
                    }

                    FileManager.backupFolder(activity.contentResolver, rootDir, gameDirName, alias, object : FileManager.BackupCallback {
                        override fun onProcess(current: Int, total: Int) {
                            binding.backupProgressBar.max = total
                            binding.backupProgressBar.progress = current
                        }

                        override fun onCompleted(folderPath: String) {
                            val account =
                                Account(alias = alias, folder = folderPath, lang = lang.ordinal, createTime = currentTime, updateTime = currentTime)
                            BaseDatabase.getInstance(activity).accountDAO().insert(account)
                            callback.onSuccess()
                        }

                        override fun onError(message: String) {
                            callback.onError("備份失敗。")
                        }
                    })
                } else {
                    callback.onNotExists()
                }
            } ?: run { callback.onError("沒有資料夾存取權限。") }
        }
            .subscribeOn(Schedulers.io())
            .subscribe { dialog.dismiss() }
    }

    private fun startLegacyBackup(dialog: AlertDialog, binding: DialogBackupAccountBinding, lang: Account.Language, callback: CreateAccountCallback) {
        val currentTime = System.currentTimeMillis()
        val gameFolderPath = String.format("%s/%s", Configs.PATH_APP_DATA, lang.packageName)
        val destPath = String.format("%s.%s", gameFolderPath, currentTime)

        binding.backupSubmitBtn.isEnabled = false
        val d = CompletableFromAction.fromAction {
            val exists = FileManager.isFolderExists(gameFolderPath)
            if (exists) {
                val alias = if (binding.backupInputEt.text.isNotEmpty()) {
                    binding.backupInputEt.text.toString()
                } else {
                    destPath.substring(destPath.lastIndexOf("/") + 1)
                }

                FileManager.backupFolder(gameFolderPath, destPath, object : FileManager.BackupCallback {
                    override fun onProcess(current: Int, total: Int) {
                        binding.backupProgressBar.max = total
                        binding.backupProgressBar.progress = current
                    }

                    override fun onCompleted(folderPath: String) {
                        val account =
                            Account(alias = alias, folder = folderPath, lang = lang.ordinal, createTime = currentTime, updateTime = currentTime)
                        BaseDatabase.getInstance(activity).accountDAO().insert(account)
                        callback.onSuccess()
                    }

                    override fun onError(message: String) {
                        callback.onError("備份失敗。")
                    }
                })
            } else {
                callback.onNotExists()
            }
        }
            .subscribeOn(Schedulers.io())
            .subscribe { dialog.dismiss() }
    }
}