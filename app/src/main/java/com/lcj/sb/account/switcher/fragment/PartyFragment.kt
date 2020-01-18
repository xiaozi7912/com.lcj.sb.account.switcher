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
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
import com.lcj.sb.account.switcher.model.AccountInfoModel
import com.lcj.sb.account.switcher.model.CreatePartyModel
import com.lcj.sb.account.switcher.model.DungeonLevelModel
import com.lcj.sb.account.switcher.model.DungeonStageModel
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.utils.IconUtils
import com.theartofdev.edmodo.cropper.CropImage

class PartyFragment : BaseFragment() {
    private lateinit var mBinding: FragmentPartyBinding
    private lateinit var mAccount: Account
    private lateinit var mAdapter: PartyAdapter
    private lateinit var mCreatePartyBinding: DialogCreatePartyBinding

    private val mDungeonLevelList = arrayListOf(
            DungeonLevelModel(0, "冥"), DungeonLevelModel(1, "神"), DungeonLevelModel(2, "滅"),
            DungeonLevelModel(3, "塔"), DungeonLevelModel(4, "魔窟")
    )
    private val mDungeonElementList = arrayListOf("火", "水", "木", "光", "暗")
    private val mDungeonStageList = ArrayList<DungeonStageModel>()
    private var mCropImageUri: Uri? = null
    private var mSelectedLevelModel: DungeonLevelModel = mDungeonLevelList[0]
    private var mSelectedElementPosition = 0
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
        mBinding.filterBtn.setOnClickListener {
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
        val layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)

        mAdapter = PartyAdapter(mActivity)
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = mAdapter

        BaseDatabase.getInstance(mActivity).dungeonPartyDAO()
                .getPartyList(mAccount.id)
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

    private fun getDungeonInfoFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        val collectionPath = mSelectedLevelModel.title
        db.collection("summons_board").document("dungeon").collection(collectionPath)
                .whereEqualTo("element", mSelectedElementPosition).orderBy("icon", Query.Direction.ASCENDING).get()
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
            updateDungeonLevelView(mDungeonLevelList[0])
            updateDungeonElementView(0)
            getDungeonInfoFromFirebase()
            AlertDialog.Builder(mActivity, R.style.CustomDialog).create().let { dialog ->
                mCreatePartyBinding.partyDungeonLevelBtn.setOnClickListener {
                    onDungeonLevelButtonClick()
                }
                mCreatePartyBinding.partyDungeonElementBtn.setOnClickListener {
                    onDungeonElementButtonClick()
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
                dialog.setContentView(mCreatePartyBinding.root)
                dialog.setOnDismissListener {
                    mSelectedLevelModel = mDungeonLevelList[0]
                    mSelectedElementPosition = 0
                }
            }
        }
    }

    private fun updateDungeonLevelView(model: DungeonLevelModel) {
        val levelResId = IconUtils.getInstance(mActivity).getDungeonLevelResId(model.index)

        mCreatePartyBinding.partyDungeonLevelIcon.setImageResource(levelResId)
        mCreatePartyBinding.partyDungeonLevelText.text = model.title
        mSelectedLevelModel = model
        getDungeonInfoFromFirebase()
    }

    private fun updateDungeonElementView(position: Int) {
        val iconResId = IconUtils.getInstance(mActivity).getDungeonElementResId(position)

        mCreatePartyBinding.partyDungeonElementIcon.setImageResource(iconResId)
        mCreatePartyBinding.partyDungeonElementText.text = mDungeonElementList[position]
        mSelectedElementPosition = position
        getDungeonInfoFromFirebase()
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

    private fun onDungeonLevelButtonClick() {
        AlertDialog.Builder(mActivity).create().let {
            val rootView = layoutInflater.inflate(R.layout.dialog_recycler_view, null, false)
            val recyclerView: RecyclerView = rootView.findViewById(R.id.recycler_view)

            recyclerView.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = DungeonLevelAdapter(mActivity, mDungeonLevelList).apply {
                setCallback { selectedItem, position ->
                    updateDungeonLevelView(selectedItem)
                    Log.d(LOG_TAG, "selectedItem : $selectedItem")
                    Log.d(LOG_TAG, "position : $position")
                    it.dismiss()
                }
            }

            it.show()
            it.setCancelable(false)
            it.setContentView(rootView)
        }
    }

    private fun onDungeonElementButtonClick() {
        AlertDialog.Builder(mActivity).create().let {
            val rootView = layoutInflater.inflate(R.layout.dialog_recycler_view, null, false)
            val recyclerView: RecyclerView = rootView.findViewById(R.id.recycler_view)

            recyclerView.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = DungeonElementAdapter(mActivity, mDungeonElementList).apply {
                setCallback { selectedItem, position ->
                    updateDungeonElementView(position)
                    Log.d(LOG_TAG, "selectedItem : $selectedItem")
                    Log.d(LOG_TAG, "position : $position")
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
                    Log.d(LOG_TAG, "selectedItem : $selectedItem")
                    Log.d(LOG_TAG, "position : $position")
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

        if (mCropImageUri != null) {
            Thread {
                BaseDatabase.getInstance(mActivity).dungeonPartyDAO()
                        .insert(DungeonParty(
                                accountId = mAccount.id,
                                dungeonType = mSelectedLevelModel.index,
                                elementType = mSelectedElementPosition,
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
    }
}