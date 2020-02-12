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
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lcj.sb.account.switcher.BaseAdapter
import com.lcj.sb.account.switcher.BaseApplication
import com.lcj.sb.account.switcher.BaseFragment
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.adapter.DungeonElementAdapter
import com.lcj.sb.account.switcher.adapter.DungeonLevelAdapter
import com.lcj.sb.account.switcher.adapter.DungeonStageAdapter
import com.lcj.sb.account.switcher.adapter.PartyAdapter
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.DungeonParty
import com.lcj.sb.account.switcher.databinding.DialogCreatePartyBinding
import com.lcj.sb.account.switcher.databinding.FragmentPartyBinding
import com.lcj.sb.account.switcher.model.*
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.IconUtils
import com.theartofdev.edmodo.cropper.CropImage
import java.io.File

class PartyFragment : BaseFragment(), BaseAdapter.PartyListListener {
    private lateinit var mBinding: FragmentPartyBinding
    private lateinit var mAccount: Account
    private lateinit var mAdapter: PartyAdapter
    private lateinit var mCreatePartyBinding: DialogCreatePartyBinding

    private val mFilterLevelList = ArrayList<DungeonLevelModel>().apply {
        add(0, DungeonLevelModel(99, "全部"))
        addAll(Configs.DUNGEON_LEVEL_LIST)
    }
    private val mFilterElementList = ArrayList<DungeonElementModel>().apply {
        add(0, DungeonElementModel(99, "全部"))
        addAll(Configs.DUNGEON_ELEMENT_LIST)
    }
    private val mCreateDialogLevelList = ArrayList<DungeonLevelModel>().apply {
        addAll(Configs.DUNGEON_LEVEL_LIST)
    }
    private val mCreateDialogElementList = ArrayList<DungeonElementModel>().apply {
        addAll(Configs.DUNGEON_ELEMENT_LIST)
    }
    private val mDungeonStageList = ArrayList<DungeonStageModel>()
    private var mCropImageUri: Uri? = null
    private var mSelectedFilterLevelModel = mFilterLevelList.first()
    private var mSelectedFilterElementModel = mFilterElementList.first()
    private var mSelectedLevelModel = mCreateDialogLevelList.first()
    private var mSelectedElementModel = mCreateDialogElementList.first()
    private var mSelectedStageModel: DungeonStageModel? = null

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

        mBinding.model = AccountInfoModel(mAccount)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.filterLevelBtn.setOnClickListener {
            showLevelListDialog(it)
        }
        mBinding.filterElementBtn.setOnClickListener {
            showElementListDialog(it)
        }
        mBinding.filterBtn.setOnClickListener {
            mBinding.model?.updateLevelList(when (mSelectedFilterLevelModel.index) {
                99 -> arrayListOf<Int>().apply {
                    for (model in Configs.DUNGEON_LEVEL_LIST) {
                        add(model.index)
                    }
                }
                else -> arrayListOf(mSelectedFilterLevelModel.index)
            })

            mBinding.model?.updateElementList(when (mSelectedFilterElementModel.index) {
                99 -> arrayListOf<Int>().apply {
                    for (model in Configs.DUNGEON_ELEMENT_LIST) {
                        add(model.index)
                    }
                }
                else -> arrayListOf(mSelectedFilterElementModel.index)
            })

            mBinding.model?.onFilterClick(mActivity) { dataList ->
                dataList?.let {
                    mHandler.post { mAdapter.update(dataList) }
                }
            }
        }

        mBinding.addFab.setOnClickListener {
            showCreatePartyDialog()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateFilterLevelView(mFilterLevelList.first())
        updateFilterElementView(mFilterElementList.first())

        mAdapter = PartyAdapter(mActivity)
        mAdapter.setOnClickListener(this)
        mBinding.recyclerView.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        mBinding.recyclerView.adapter = mAdapter

        BaseDatabase.getInstance(mActivity).dungeonPartyDAO()
                .getPartyList(mAccount.id)
                .observe(this, Observer { mAdapter.update(it) })
    }

    override fun onResume() {
        super.onResume()
        BaseApplication.setCurrentScreen(mActivity, Configs.SCREEN_PARTY, LOG_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                mCropImageUri = result.uri
                mCreatePartyBinding.partyIv.setImageURI(mCropImageUri)
                Log.d(LOG_TAG, "mCropImageUri : ${mCropImageUri}")
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.error.printStackTrace()
            }
        }
    }

    override fun onDeleteClick(item: DungeonParty) {
        Log.i(LOG_TAG, "onDeleteClick")
        Log.d(LOG_TAG, "onDeleteClick item : $item")
        AlertDialog.Builder(mActivity).apply {
            setTitle("刪除隊伍")
            setMessage("確定要刪除此隊伍？")
            setPositiveButton(getString(R.string.dialog_button_confirmed)) { dialog, which ->
                Thread {
                    File(item.imagePath).let { if (it.exists()) it.delete() }
                    BaseDatabase.getInstance(mActivity).dungeonPartyDAO().delete(item)
                    mHandler.post {
                        Snackbar.make(mContentView, R.string.dialog_message_delete_success, Snackbar.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }.start()
            }
            setNegativeButton(getString(R.string.dialog_button_cancel)) { dialog, which -> dialog.dismiss() }
        }.create().show()
    }

    private fun getDungeonInfoFromRemote() {
        val db = FirebaseFirestore.getInstance()
        val collectionPath = mSelectedLevelModel.title
        db.document("/summons_board/dungeon").collection(collectionPath)
                .whereEqualTo("element", mSelectedElementModel.index).orderBy("number", Query.Direction.ASCENDING).get()
                .addOnSuccessListener { result ->
                    mDungeonStageList.clear()

                    for (document in result) {
                        val model = document.toObject(DungeonStageModel::class.java)
                        mDungeonStageList.add(model)
                        Log.d(LOG_TAG, "${document.id} => ${document.data}")
                        Log.d(LOG_TAG, "${document.id} => ${model.event_title}")
                    }

                    if (result.isEmpty) {
                        updateDungeonStageView(null)
                    } else {
                        updateDungeonStageView(mDungeonStageList[0])
                    }
                }
                .addOnFailureListener { error ->
                    Log.w(LOG_TAG, error)
                }
    }

    private fun showCreatePartyDialog() {
        mCreatePartyBinding = DialogCreatePartyBinding.inflate(layoutInflater)

        CreatePartyModel().let { model ->
            mCreatePartyBinding.model = model
            updateDungeonLevelView(mCreateDialogLevelList.first())
            updateDungeonElementView(mCreateDialogElementList.first())
            getDungeonInfoFromRemote()
            AlertDialog.Builder(mActivity, R.style.CustomDialog).create().let { dialog ->
                mCreatePartyBinding.partyDungeonLevelBtn.setOnClickListener {
                    showLevelListDialog(it)
                }
                mCreatePartyBinding.partyDungeonElementBtn.setOnClickListener {
                    showElementListDialog(it)
                }
                mCreatePartyBinding.partyDungeonStageBtn.setOnClickListener {
                    onDungeonStageButtonClick()
                }
                mCreatePartyBinding.partyIv.setOnClickListener {
                    onSelectPartyImageButtonClick()
                }
                mCreatePartyBinding.cancelBtn.setOnClickListener {
                    dialog.dismiss()
                }
                mCreatePartyBinding.createBtn.setOnClickListener {
                    onCreatePartyButtonClick(dialog)
                }

                dialog.show()
                dialog.window?.apply {
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
                dialog.setContentView(mCreatePartyBinding.root)
                dialog.setOnDismissListener {
                    mSelectedLevelModel = mCreateDialogLevelList.first()
                    mSelectedElementModel = mCreateDialogElementList.first()
                }
            }
        }
    }

    private fun updateFilterLevelView(model: DungeonLevelModel) {
        val iconResId = IconUtils.getInstance(mActivity).getDungeonLevelResId(model.index)

        mBinding.filterLevelIcon.setImageResource(iconResId)
        mBinding.filterLevelText.text = model.title
        mSelectedFilterLevelModel = model
    }

    private fun updateFilterElementView(model: DungeonElementModel) {
        val iconResId = IconUtils.getInstance(mActivity).getDungeonElementResId(model.index)

        mBinding.filterElementIcon.setImageResource(iconResId)
        mBinding.filterElementText.text = model.title
        mSelectedFilterElementModel = model
    }

    private fun updateDungeonLevelView(model: DungeonLevelModel) {
        val iconResId = IconUtils.getInstance(mActivity).getDungeonLevelResId(model.index)

        mCreatePartyBinding.partyDungeonLevelIcon.setImageResource(iconResId)
        mCreatePartyBinding.partyDungeonLevelText.text = model.title
        mSelectedLevelModel = model
        getDungeonInfoFromRemote()
    }

    private fun updateDungeonElementView(model: DungeonElementModel) {
        val iconResId = IconUtils.getInstance(mActivity).getDungeonElementResId(model.index)

        mCreatePartyBinding.partyDungeonElementIcon.setImageResource(iconResId)
        mCreatePartyBinding.partyDungeonElementText.text = model.title
        mSelectedElementModel = model
        getDungeonInfoFromRemote()
    }

    private fun updateDungeonStageView(model: DungeonStageModel?) {
        if (model != null) {
            val resourceId = resources.getIdentifier(model.icon, "drawable", context?.packageName)
            mCreatePartyBinding.partyDungeonStageIcon.setImageResource(resourceId)
            mCreatePartyBinding.partyDungeonStageText.text = model.title
        } else {
            mCreatePartyBinding.partyDungeonStageIcon.setImageDrawable(null)
            mCreatePartyBinding.partyDungeonStageText.text = "沒有關卡資料"
        }
        mSelectedStageModel = model
    }

    private fun showLevelListDialog(view: View) {
        AlertDialog.Builder(mActivity).create().let {
            val rootView = layoutInflater.inflate(R.layout.dialog_recycler_view, null, false)
            val recyclerView: RecyclerView = rootView.findViewById(R.id.recycler_view)
            val dataList = when (view.id) {
                R.id.filter_level_btn -> mFilterLevelList
                R.id.party_dungeon_level_btn -> mCreateDialogLevelList
                else -> mFilterLevelList
            }

            recyclerView.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = DungeonLevelAdapter(mActivity, dataList).apply {
                setCallback { selectedItem, position ->
                    when (view.id) {
                        R.id.filter_level_btn -> updateFilterLevelView(selectedItem)
                        R.id.party_dungeon_level_btn -> updateDungeonLevelView(selectedItem)
                    }
                    it.dismiss()
                }
            }

            it.show()
            it.setCancelable(false)
            it.setContentView(rootView)
        }
    }

    private fun showElementListDialog(view: View) {
        AlertDialog.Builder(mActivity).create().let {
            val rootView = layoutInflater.inflate(R.layout.dialog_recycler_view, null, false)
            val recyclerView: RecyclerView = rootView.findViewById(R.id.recycler_view)
            val dataList = when (view.id) {
                R.id.filter_element_btn -> mFilterElementList
                R.id.party_dungeon_element_btn -> mCreateDialogElementList
                else -> mFilterElementList
            }

            recyclerView.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = DungeonElementAdapter(mActivity, dataList).apply {
                setCallback { selectedItem, position ->
                    when (view.id) {
                        R.id.filter_element_btn -> updateFilterElementView(selectedItem)
                        R.id.party_dungeon_element_btn -> updateDungeonElementView(selectedItem)
                    }
                    it.dismiss()
                }
            }

            it.show()
            it.setCancelable(false)
            it.setContentView(rootView)
        }
    }

    private fun onDungeonStageButtonClick() {
        AlertDialog.Builder(mActivity).create().let {
            val rootView = layoutInflater.inflate(R.layout.dialog_recycler_view, null, false)
            val recyclerView: RecyclerView = rootView.findViewById(R.id.recycler_view)

            recyclerView.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = DungeonStageAdapter(mActivity, mDungeonStageList).apply {
                setCallback { selectedItem, position ->
                    updateDungeonStageView(selectedItem)
                    it.dismiss()
                }
            }

            if (mDungeonStageList.isNotEmpty()) it.show()
            it.setCancelable(false)
            it.setContentView(rootView)
        }
    }

    private fun onSelectPartyImageButtonClick() {
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

    private fun onCreatePartyButtonClick(dialog: AlertDialog) {
        val remark = mCreatePartyBinding.inputEdit.text.toString()

        if (mSelectedStageModel != null) {
            if (mCropImageUri != null) {
                Thread {
                    BaseDatabase.getInstance(mActivity).dungeonPartyDAO()
                            .insert(DungeonParty(
                                    accountId = mAccount.id,
                                    dungeonType = mSelectedLevelModel.index,
                                    elementType = mSelectedElementModel.index,
                                    title = mSelectedStageModel?.title!!,
                                    imagePath = mCropImageUri?.path!!,
                                    iconName = mSelectedStageModel?.icon,
                                    eventTitle = mSelectedStageModel?.event_title,
                                    monsterName = mSelectedStageModel?.monster_name!!,
                                    remark = remark
                            ))
                    dialog.dismiss()
                }.start()
            } else {
                Toast.makeText(mActivity, "請選取圖片", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(mActivity, "請選擇關卡", Toast.LENGTH_SHORT).show()
        }
    }
}