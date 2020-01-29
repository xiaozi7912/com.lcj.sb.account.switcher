package com.lcj.sb.account.switcher.adapter

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.model.AccountModel
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.ZipManager
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var rootView: View = mInflater?.inflate(R.layout.item_account_list, null, false)!!
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var selectedItem = mDataList?.get(position)

        if (currentFolderName.equals(selectedItem?.folderName)) {
            holder.rootLayout?.setBackgroundColor(Color.parseColor("#55ff0000"))
        } else {
            holder.rootLayout?.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.saveButton?.isEnabled = !selectedItem?.disable!!
        holder.loadButton?.isEnabled = !selectedItem.disable

        holder.folderNameTextView?.text = selectedItem.folderName
        holder.saveButton?.setOnClickListener {
            Log.i(LOG_TAG, "Save Button Click")
            Log.v(LOG_TAG, "Save Button Click selectedItem?.folderPath : " + selectedItem.folderPath)
            val pathSrcFolder = String.format("%s/%s/%s", Configs.PATH_APP_DATA, mPrefixNameSB, "files")
            val pathDstFolder = String.format("%s/%s", selectedItem.folderPath, "files")
            val fileSrcFolder = File(pathSrcFolder)

            Thread {
                mCallback?.onSaveStart()

                for (srcFile in fileSrcFolder.listFiles()) {
                    if (srcFile.isFile) {
                        var dstFileName = String.format("%s/%s", pathDstFolder, srcFile.name)
                        writeFile(srcFile.absolutePath, dstFileName)
                    }
                }
                mCallback?.onSaveSuccess()
            }.start()
        }

        holder.loadButton?.setOnClickListener {
            Log.i(LOG_TAG, "Load Button Click")
            Log.v(LOG_TAG, "Load Button Click selectedItem?.folderPath : " + selectedItem.folderPath)
            val srcFolder: String = String.format("%s/%s", selectedItem.folderPath, "files")
            val dstFolder: String = String.format("%s/%s", Configs.PATH_APP_DATA, mPrefixNameSB)
            val command: String = String.format("cp -a %s %s", srcFolder, dstFolder)
            Log.v(LOG_TAG, "Load Button Click command : $command")

            Thread {
                val process: Process = Runtime.getRuntime().exec(command)
                val buffReader = BufferedReader(InputStreamReader(process.inputStream))
                var readLine: String? = null

                do {
                    readLine = buffReader.readLine()
                    Log.v(LOG_TAG, "Load Button Click readLine : $readLine")
                } while (readLine != null)

                buffReader.close()
                process.waitFor()

                mCallback?.onLoadSuccess(selectedItem.folderName)
            }.start()
        }

        holder.uploadButton?.setOnClickListener {
            Log.i(LOG_TAG, "Upload Button Click")
            Thread {
                val srcFilePath = String.format("%s/%s", selectedItem.folderPath, "files")
                val fileSrcFolder = File(srcFilePath)
                val fileList = ArrayList<String>()
                val zipFilePath = String.format("%s/%s.zip", selectedItem.folderPath, selectedItem.folderName)

                for (srcFile in fileSrcFolder.listFiles()) {
                    if (srcFile.isFile) {
                        fileList.add(srcFile.absolutePath)
                    }
                }

                ZipManager.zip(fileList, zipFilePath)

                val file = Uri.fromFile(File(zipFilePath))
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference
                val fileRef = storageRef.child("summons/${file.lastPathSegment}")
                val uploadTask = fileRef.putFile(file)

                storageRef.listAll()
                        .addOnSuccessListener { result ->
                            Log.i(LOG_TAG, "addOnSuccessListener")
                            result.items.forEach { item ->
                                Log.v(LOG_TAG, "${item.name}")
                            }
                        }
                        .addOnFailureListener {
                            Log.i(LOG_TAG, "addOnFailureListener")
                        }

                uploadTask
                        .addOnSuccessListener { Log.i(LOG_TAG, "addOnSuccessListener") }
                        .addOnFailureListener { Log.i(LOG_TAG, "addOnFailureListener") }
            }.start()
        }

        holder.downloadButton.setOnClickListener {
            Log.i(LOG_TAG, "downloadButton")
            Thread {
                val storage = FirebaseStorage.getInstance()
                val zipFileRef = storage.reference.child("${selectedItem.folderName}.zip")
                val localFile = File.createTempFile("summons_", ".zip")

                zipFileRef.getFile(localFile)
                        .addOnProgressListener { taskSnapshot ->
                            val progress: Double = (taskSnapshot.bytesTransferred * 100.0) / taskSnapshot.totalByteCount
                            Log.v(LOG_TAG, "getFile bytesTransferred : ${taskSnapshot.bytesTransferred}")
                            Log.v(LOG_TAG, "getFile totalByteCount : ${taskSnapshot.totalByteCount}")
                            Log.v(LOG_TAG, "getFile progress : $progress")
                        }
                        .addOnSuccessListener { Log.i(LOG_TAG, "getFile addOnSuccessListener") }
                        .addOnFailureListener { Log.i(LOG_TAG, "getFile addOnFailureListener") }
            }.start()
        }
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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rootLayout: LinearLayout? = null
        lateinit var folderNameTextView: TextView
        lateinit var saveButton: Button
        lateinit var loadButton: Button
        lateinit var uploadButton: Button
        lateinit var downloadButton: Button

        init {
//            rootLayout = itemView.findViewById(R.id.item_account_list_root)
//            folderNameTextView = itemView.findViewById(R.id.item_account_list_folder_name_text)
//            saveButton = itemView.findViewById(R.id.item_account_list_save_button)
//            loadButton = itemView.findViewById(R.id.item_account_list_load_button)
//            uploadButton = itemView.findViewById(R.id.item_account_list_upload_button)
//            downloadButton = itemView.findViewById(R.id.item_download_zip_button)
        }
    }

    interface Callback {
        fun onLoadSuccess(account: String?)

        fun onSaveStart()

        fun onSaveSuccess()
    }
}