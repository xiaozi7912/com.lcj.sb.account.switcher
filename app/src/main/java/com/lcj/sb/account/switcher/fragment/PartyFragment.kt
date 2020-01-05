package com.lcj.sb.account.switcher.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class PartyFragment : BaseFragment() {
    private lateinit var mBinding: FragmentPartyBinding
    private lateinit var mAccount: Account
    private lateinit var mAdapter: PartyAdapter

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

    private fun showCreatePartyDialog() {
        Log.i(LOG_TAG, "addFab")
        val binding = DialogCreatePartyBinding.inflate(layoutInflater)

        CreatePartyModel().let { model ->
            binding.model = model

            AlertDialog.Builder(mActivity).apply {
                setView(binding.root)
            }.create().let { dialog ->
                binding.cancelBtn.setOnClickListener { dialog.dismiss() }
                binding.createBtn.setOnClickListener {
                    val title = binding.inputEdit.text.toString()
                    val dungeonType = binding.model?.getSelectedDungeonType()
                    val elementType = binding.model?.getSelectedElementType()
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
                                            imagePath = title
                                    ))
                        }.start()
                    } else {

                    }
                }
                dialog.show()
            }
        }
    }
}