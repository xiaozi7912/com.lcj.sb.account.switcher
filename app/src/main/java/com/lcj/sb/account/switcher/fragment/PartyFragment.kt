package com.lcj.sb.account.switcher.fragment

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
import com.lcj.sb.account.switcher.databinding.FragmentPartyBinding
import com.lcj.sb.account.switcher.model.AccountInfoModel
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
        Thread {
            BaseDatabase.getInstance(mActivity).dungeonPartyDAO()
                    .deleteAll()
        }.start()

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
}