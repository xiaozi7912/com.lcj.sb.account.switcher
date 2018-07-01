package com.lcj.sb.account.switcher.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.adapter.AccountListAdapter
import com.lcj.sb.account.switcher.model.AccountModel
import com.lcj.sb.account.switcher.utils.AccountInfoManager
import com.lcj.sb.account.switcher.utils.Configs
import java.io.File
import java.io.FileFilter
import java.util.*

/**
 * Created by Larry on 2018-07-01.
 */
class SBTWFragment : BaseFragment() {
    private var mStartGameButton: Button? = null
    private var mCurrentAccountTextView: TextView? = null
    private var mAccountListView: RecyclerView? = null
    private var mActionStatusTextView: TextView? = null

    private var mDataList: ArrayList<AccountModel>? = null
    private var mAccountListAdapter: AccountListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater?.inflate(R.layout.fragment_account, null, false)
        mStartGameButton = mRootView?.findViewById(R.id.fragment_account_start_game_button)
        mCurrentAccountTextView = mRootView?.findViewById(R.id.main_current_account_text)
        mAccountListView = mRootView?.findViewById(R.id.main_account_list)
        mActionStatusTextView = mRootView?.findViewById(R.id.main_action_status_text)

        mCurrentAccountTextView?.visibility = View.GONE
        mStartGameButton?.setOnClickListener({
            startGame()
        })
        return mRootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateCurrentAccountTextView("")
        mActionStatusTextView?.text = ""
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        checkSBFolderExists()
        updateCurrentAccountTextView(AccountInfoManager.getInstance().currentTWAccount)
        initAccountList()
    }

    companion object {
        fun newInstance(): SBTWFragment {
            return SBTWFragment()
        }
    }

    private fun updateCurrentAccountTextView(account: String?) {
        Log.i(LOG_TAG, "updateCurrentAccountTextView")
        mCurrentAccountTextView?.text = String.format("Current Account : %s", account)
    }

    private fun checkSBFolderExists() {
        Log.i(LOG_TAG, "checkSBFolderExists")
        Log.v(LOG_TAG, "checkSBFolderExists Configs.PATH_EXTERNAL_STORAGE : " + Configs.PATH_EXTERNAL_STORAGE)
        Log.v(LOG_TAG, "checkSBFolderExists Configs.PATH_APP_DATA : " + Configs.PATH_APP_DATA)
        var dirAppData: File = File(Configs.PATH_APP_DATA)
        var dirSB: File = File(String.format("%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_TW))
        var fileFilter: FileFilter = FileFilter { file ->
            var regex: Regex = Regex(String.format("%s\\.\\w", Configs.PREFIX_NAME_SB_TW))
            file.name.contains(regex)
        }
        var folders: Array<File> = dirAppData.listFiles(fileFilter)

        if (!dirSB.exists()) dirSB.mkdir()

        Arrays.sort(folders)
        mDataList = ArrayList<AccountModel>()
        for (folder in folders) {
            Log.v(LOG_TAG, "checkSBFolderExists folder.name : " + folder.name)
            Log.v(LOG_TAG, "checkSBFolderExists folder.absolutePath : " + folder.absolutePath)
            mDataList?.add(AccountModel(folder.name, folder.absolutePath))
        }
    }

    private fun initAccountList() {
        Log.i(LOG_TAG, "initAccountList")
        mAccountListAdapter = AccountListAdapter(mActivity, Configs.PREFIX_NAME_SB_TW, mDataList!!)

        mAccountListAdapter?.currentFolderName = AccountInfoManager.getInstance().currentTWAccount
        mAccountListAdapter?.setCallback(object : AccountListAdapter.Callback {
            override fun onLoadSuccess(account: String?) {
                Log.i(LOG_TAG, "onLoadSuccess")
                AccountInfoManager.getInstance().currentTWAccount = account
                mAccountListAdapter?.currentFolderName = account
                Log.v(LOG_TAG, "onLoadSuccess account : " + account)

                mHandler.post {
                    updateCurrentAccountTextView(account)
                    Toast.makeText(mActivity, "Load Success", Toast.LENGTH_SHORT).show()
                    mAccountListAdapter?.notifyDataSetChanged()
                }

                startGame()
            }

            override fun onSaveStart() {
                Log.i(LOG_TAG, "onSaveStart")
                mDataList?.forEach { item -> item.disable = true }
                mHandler.post {
                    mActionStatusTextView?.text = "File Saving..."
                    mAccountListAdapter?.notifyDataSetChanged()
                }
            }

            override fun onSaveSuccess() {
                mDataList?.forEach { item -> item.disable = false }
                mHandler.post {
                    mActionStatusTextView?.text = ""
                    mAccountListAdapter?.notifyDataSetChanged()
                    Toast.makeText(mActivity, "Save Success", Toast.LENGTH_SHORT).show()
                }
            }
        })

        var layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        mAccountListView?.layoutManager = layoutManager
        mAccountListView?.adapter = mAccountListAdapter
    }

    private fun startGame() {
        Log.i(LOG_TAG, "startGame")
        var intent = mActivity?.packageManager?.getLaunchIntentForPackage(Configs.PREFIX_NAME_SB_TW)
        startActivity(intent)
    }
}