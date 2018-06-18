package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.lcj.sb.account.switcher.BaseActivity
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.model.AccountModel
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Created by Larry on 2018-06-18.
 */
class AccountListAdapter() : RecyclerView.Adapter<AccountListAdapter.ViewHolder>() {
    val LOG_TAG: String = javaClass.simpleName
    var mActivity: Activity? = null
    var mInflater: LayoutInflater? = null
    var mDataList: ArrayList<AccountModel>? = null

    constructor(activity: Activity, dataList: ArrayList<AccountModel>) : this() {
        Log.i(LOG_TAG, "constructor")
        this.mActivity = activity
        this.mInflater = LayoutInflater.from(activity)
        this.mDataList = dataList
    }

    override fun getItemCount(): Int {
        return mDataList?.size!!
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        var rootView: View = mInflater?.inflate(R.layout.item_account_list, null, false)!!
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        var selectedItem = mDataList?.get(position)

        holder?.folderNameTextView?.text = selectedItem?.folderName
        holder?.saveButton?.setOnClickListener({
            Log.i(LOG_TAG, "Save Button Click")
            Log.v(LOG_TAG, "Save Button Click selectedItem?.folderPath : " + selectedItem?.folderPath)
        })
        holder?.loadButton?.setOnClickListener({
            Log.i(LOG_TAG, "Load Button Click")
            Log.v(LOG_TAG, "Load Button Click selectedItem?.folderPath : " + selectedItem?.folderPath)
            var srcFolder: String = String.format("%s/%s", selectedItem?.folderPath, "files")
            var dstFolder: String = String.format("%s/%s", BaseActivity.PATH_APP_DATA, BaseActivity.PREFIX_NAME_SB)
            var command: String = String.format("cp -a %s %s", srcFolder, dstFolder)
            Log.v(LOG_TAG, "Load Button Click command : " + command)

            var process: Process = Runtime.getRuntime().exec(command)
            var buffReader: BufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var readLine: String? = null

            do {
                readLine = buffReader.readLine()
                Log.v(LOG_TAG, "Load Button Click readLine : " + readLine)
            } while (readLine != null)

            buffReader.close()
            process.waitFor()
        })
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var folderNameTextView: TextView? = null
        var saveButton: Button? = null
        var loadButton: Button? = null

        init {
            folderNameTextView = itemView?.findViewById(R.id.item_account_list_folder_name_text)
            saveButton = itemView?.findViewById(R.id.item_account_list_save_button)
            loadButton = itemView?.findViewById(R.id.item_account_list_load_button)
        }
    }
}