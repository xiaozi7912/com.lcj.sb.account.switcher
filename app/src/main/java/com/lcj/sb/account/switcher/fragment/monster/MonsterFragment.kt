package com.lcj.sb.account.switcher.fragment.monster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.lcj.sb.account.switcher.BaseFragment
import com.lcj.sb.account.switcher.adapter.MonsterListAdapter
import com.lcj.sb.account.switcher.databinding.FragmentMonsterBinding
import com.lcj.sb.account.switcher.http.APIManager
import com.lcj.sb.account.switcher.http.BaseObserver
import com.lcj.sb.account.switcher.http.model.MonsterModel
import com.lcj.sb.account.switcher.http.response.MonsterResponse
import io.reactivex.android.schedulers.AndroidSchedulers

class MonsterFragment : BaseFragment() {
    private lateinit var mBinding: FragmentMonsterBinding
    private lateinit var mAdapter: MonsterListAdapter
    private lateinit var mDataList: List<MonsterModel>

    companion object {
        fun newInstance(): MonsterFragment {
            return MonsterFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentMonsterBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        getMonsterList()
    }

    private fun initRecyclerView() {
        mAdapter = MonsterListAdapter(mActivity)

        mBinding.recyclerView.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
        mBinding.recyclerView.adapter = mAdapter
    }

    private fun getMonsterList() {
        APIManager.getInstance(mActivity).getMonsterList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<MonsterResponse>(mActivity) {
                    override fun onNext(response: MonsterResponse) {
                        if (response.code == 200) {
                            mDataList = response.result.data!!
                        }
                    }

                    override fun onComplete() {
                        mAdapter.update(mDataList)
                    }
                })
    }
}