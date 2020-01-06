package com.lcj.sb.account.switcher.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.lcj.sb.account.switcher.adapter.PartyAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.DungeonParty
import com.lcj.sb.account.switcher.databinding.DialogCreatePartyBinding
import com.lcj.sb.account.switcher.databinding.FragmentPartyBinding
import com.lcj.sb.account.switcher.model.AccountInfoModel
import com.lcj.sb.account.switcher.model.CreatePartyModel
import com.lcj.sb.account.switcher.utils.Configs
import com.theartofdev.edmodo.cropper.CropImage

class PartyFragment : BaseFragment() {
    private lateinit var mBinding: FragmentPartyBinding
    private lateinit var mAccount: Account
    private lateinit var mAdapter: PartyAdapter
    private lateinit var mCreatePartyBinding: DialogCreatePartyBinding
    private var mCropImageUri: Uri? = null

    companion object {
        fun newInstance(account: Account): PartyFragment {
            return PartyFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(Configs.INTENT_KEY_ACCOUNT, account)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentPartyBinding.inflate(inflater, container, false)
        mAccount = arguments?.getSerializable(Configs.INTENT_KEY_ACCOUNT) as Account

        mBinding.model = AccountInfoModel()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.filterBtn.setOnClickListener {
            mBinding.model?.onFilterClick()
            Thread {
                BaseDatabase.getInstance(mActivity).dungeonPartyDAO()
                        .insert(DungeonParty(
                                accountId = mAccount.id,
                                dungeonType = 0,
                                elementType = 0,
                                title = "${System.currentTimeMillis()}",
                                imagePath = ""
                        ))
            }.start()
        }

        mBinding.addFab.setOnClickListener {
            Log.i(LOG_TAG, "addFab")
            showCreatePartyDialog()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)

        mAdapter = PartyAdapter(mActivity)
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = mAdapter

        BaseDatabase.getInstance(mActivity).dungeonPartyDAO()
                .partys(mAccount.id)
                .observe(this, Observer { mAdapter.update(it) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                mCropImageUri = result.uri
                mCreatePartyBinding.partyIv.setImageURI(mCropImageUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun showCreatePartyDialog() {
        Log.i(LOG_TAG, "addFab")
        mCreatePartyBinding = DialogCreatePartyBinding.inflate(layoutInflater)

        CreatePartyModel().let { model ->
            mCreatePartyBinding.model = model

            AlertDialog.Builder(mActivity).apply {
                setView(mCreatePartyBinding.root)
            }.create().let { dialog ->
                mCreatePartyBinding.partyIv.setOnClickListener {
                    mActivity.resources.displayMetrics.let {
                        val left = (18 * it.density).toInt()
                        val top = (112 * it.density).toInt()
                        val width = (266 * it.density).toInt()
                        val height = (68 * it.density).toInt()
                        val rect = Rect(left, top, left + width, top + height)

                        CropImage.activity()
                                .setInitialCropWindowRectangle(rect)
                                .setMinCropWindowSize(width, height)
                                .start(context!!, this@PartyFragment)
                    }
                }
                mCreatePartyBinding.cancelBtn.setOnClickListener { dialog.dismiss() }
                mCreatePartyBinding.createBtn.setOnClickListener {
                    val title = mCreatePartyBinding.inputEdit.text.toString()
                    val dungeonType = mCreatePartyBinding.model?.getSelectedDungeonType()
                    val elementType = mCreatePartyBinding.model?.getSelectedElementType()
                    Log.v(LOG_TAG, "title : $title")
                    Log.v(LOG_TAG, "dungeonType : $dungeonType")
                    Log.v(LOG_TAG, "elementType : $elementType")

                    if (dungeonType != -1 && elementType != -1 && title.isNotEmpty()) {
                        Thread {
                            BaseDatabase.getInstance(mActivity).dungeonPartyDAO()
                                    .insert(DungeonParty(
                                            accountId = mAccount.id,
                                            dungeonType = dungeonType!!,
                                            elementType = elementType!!,
                                            title = title,
                                            imagePath = mCropImageUri?.path!!
                                    ))
                            dialog.dismiss()
                        }.start()
                    } else {
                        Toast.makeText(mActivity, "請輸入完整資訊", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.show()
            }
        }
    }
}