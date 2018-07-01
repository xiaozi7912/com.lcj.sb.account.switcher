package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.model.AccountModel
import com.lcj.sb.account.switcher.utils.Configs
import java.io.*

/**
 * Created by Larry on 2018-06-18.
 */
class AccountListAdapter() : RecyclerView.Adapter<AccountListAdapter.ViewHolder>() {
    private val LOG_TAG: String = javaClass.simpleName
    private var mActivity: Activity? = null
    private var mInflater: LayoutInflater? = null
    private var mDataList: ArrayList<AccountModel>? = null
    private var mCallback: Callback? = null

    private val BUFFER_SIZE = 256

    private var mPrefixNameSB: String? = null
    var currentFolderName: String? = null
        get() {
            return field
        }
        set(value) {
            field = value
        }

    constructor(activity: Activity?, prefixNameSB: String, dataList: ArrayList<AccountModel>) : this() {
        Log.i(LOG_TAG, "constructor")
        this.mActivity = activity
        this.mInflater = LayoutInflater.from(activity)
        this.mPrefixNameSB = prefixNameSB
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

        if (currentFolderName.equals(selectedItem?.folderName)) {
            holder?.rootLayout?.setBackgroundColor(Color.parseColor("#55ff0000"))
        } else {
            holder?.rootLayout?.setBackgroundColor(Color.TRANSPARENT)
        }

        holder?.saveButton?.isEnabled = !selectedItem?.disable!!
        holder?.loadButton?.isEnabled = !selectedItem?.disable!!

        holder?.folderNameTextView?.text = selectedItem?.folderName
        holder?.saveButton?.setOnClickListener({
            Log.i(LOG_TAG, "Save Button Click")
            Log.v(LOG_TAG, "Save Button Click selectedItem?.folderPath : " + selectedItem?.folderPath)
            var pathSrcFolder = String.format("%s/%s/%s", Configs.PATH_APP_DATA, mPrefixNameSB, "files")
            var pathDstFolder = String.format("%s/%s", selectedItem?.folderPath, "files")
            var fileSrcFolder = File(pathSrcFolder)

            Thread({
                mCallback?.onSaveStart()

                for (srcFile in fileSrcFolder.listFiles()) {
                    if (srcFile.isFile) {
                        var dstFileName = String.format("%s/%s", pathDstFolder, srcFile.name)
                        writeFile(srcFile.absolutePath, dstFileName)
                    }
                }
                mCallback?.onSaveSuccess()
            }).start()
        })
        holder?.loadButton?.setOnClickListener({
            Log.i(LOG_TAG, "Load Button Click")
            Log.v(LOG_TAG, "Load Button Click selectedItem?.folderPath : " + selectedItem?.folderPath)
            var srcFolder: String = String.format("%s/%s", selectedItem?.folderPath, "files")
            var dstFolder: String = String.format("%s/%s", Configs.PATH_APP_DATA, mPrefixNameSB)
            var command: String = String.format("cp -a %s %s", srcFolder, dstFolder)
            Log.v(LOG_TAG, "Load Button Click command : " + command)

            Thread({
                var process: Process = Runtime.getRuntime().exec(command)
                var buffReader = BufferedReader(InputStreamReader(process.inputStream))
                var readLine: String? = null

                do {
                    readLine = buffReader.readLine()
                    Log.v(LOG_TAG, "Load Button Click readLine : " + readLine)
                } while (readLine != null)

                buffReader.close()
                process.waitFor()

                mCallback?.onLoadSuccess(selectedItem?.folderName)
            }).start()
        })
    }

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    private fun writeFile(srcFilePath: String, dstFilePath: String) {
        Log.i(LOG_TAG, "writeFile")
        Log.v(LOG_TAG, "writeFile srcFilePath : " + srcFilePath)
        Log.v(LOG_TAG, "writeFile dstFilePath : " + dstFilePath)

        var reader = FileInputStream(srcFilePath)
        var writer = FileOutputStream(dstFilePath)
        var buffer = ByteArray(BUFFER_SIZE)
        var readSize: Int

        do {
            readSize = reader.read(buffer)
            if (readSize > 0) writer.write(buffer, 0, readSize)
            Log.v(LOG_TAG, "writeFile readSize : " + readSize)
        } while (readSize > 0)

        writer.flush()
        writer.close()
        reader.close()
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var rootLayout: LinearLayout? = null
        var folderNameTextView: TextView? = null
        var saveButton: Button? = null
        var loadButton: Button? = null

        init {
            rootLayout = itemView?.findViewById(R.id.item_account_list_root)
            folderNameTextView = itemView?.findViewById(R.id.item_account_list_folder_name_text)
            saveButton = itemView?.findViewById(R.id.item_account_list_save_button)
            loadButton = itemView?.findViewById(R.id.item_account_list_load_button)
        }
    }

    interface Callback {
        fun onLoadSuccess(account: String?)

        fun onSaveStart()

        fun onSaveSuccess()
    }
}