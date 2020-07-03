package com.lcj.sb.account.switcher.fragment.monster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.lcj.sb.account.switcher.BaseFragment
import com.lcj.sb.account.switcher.activity.MonsterDetailActivity
import com.lcj.sb.account.switcher.adapter.MonsterListAdapter
import com.lcj.sb.account.switcher.databinding.FragmentMonsterBinding
import com.lcj.sb.account.switcher.http.APIManager
import com.lcj.sb.account.switcher.http.BaseObserver
import com.lcj.sb.account.switcher.http.model.MonsterModel
import com.lcj.sb.account.switcher.http.response.MonsterResponse
import com.lcj.sb.account.switcher.utils.Configs
import com.lcj.sb.account.switcher.view.MonsterFilterDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class MonsterFragment : BaseFragment() {
    private lateinit var mBinding: FragmentMonsterBinding
    private lateinit var mAdapter: MonsterListAdapter
    private lateinit var mDataList: ArrayList<MonsterModel>

    private var mCurrentPage = 1
    private var mTotalPage = 99
    private var mIsLoading: Boolean = false

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
        mBinding.filterDialogButton.setOnClickListener { onFilterDialogButtonClick() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecyclerView()
        getMonsterList(1)
    }

    private fun initRecyclerView() {
        mAdapter = MonsterListAdapter(mActivity)
        mAdapter.setOnItemClick { position, item -> onMonsterClick(position, item) }

        mBinding.recyclerView.layoutManager = GridLayoutManager(mActivity, 4)
        mBinding.recyclerView.adapter = mAdapter
        mBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (!mIsLoading) {
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == (mDataList.size - 1)) {
                        getMonsterList(mCurrentPage + 1, false)
                    }
                }
            }
        })
    }

    private fun getMonsterList(queryPage: Int, showProgress: Boolean = true) {
        if (queryPage > mTotalPage) return
        if (queryPage == 1) mDataList = ArrayList()

        APIManager.getInstance(mActivity).getMonsterList(queryPage, 50)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : BaseObserver<MonsterResponse>(mActivity, showProgress) {
                    override fun onSubscribe(d: Disposable) {
                        super.onSubscribe(d)
                        mIsLoading = true
                    }

                    override fun onSuccess(response: MonsterResponse) {
                        mCurrentPage = response.result.current_page
                        mTotalPage = response.result.last_page
                        mDataList.addAll(response.result.data!!)
                    }

                    override fun onComplete() {
                        super.onComplete()
                        mIsLoading = false
                        mAdapter.update(mDataList)
                    }
                })
    }

    private fun onFilterDialogButtonClick() {
        MonsterFilterDialog.getInstance(mActivity).show()
    }

    private fun onMonsterClick(position: Int, item: MonsterModel) {
        Log.d(LOG_TAG, "position : $position")
        Log.d(LOG_TAG, "item : ${item.id}")
        Intent(mActivity, MonsterDetailActivity::class.java).let {
            it.putExtra(Configs.INTENT_KEY_MONSTER_MODEL, Gson().toJson(item))
            startActivity(it)
        }
    }
}