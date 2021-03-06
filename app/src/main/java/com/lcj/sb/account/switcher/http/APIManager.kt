package com.lcj.sb.account.switcher.http

import android.content.Context
import com.lcj.sb.account.switcher.BuildConfig
import com.lcj.sb.account.switcher.http.response.DungeonResponse
import com.lcj.sb.account.switcher.http.response.MonsterResponse
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*

class APIManager : BaseAPIManager<APIService>(BuildConfig.API_BASE_URL) {
    init {
        mService = mRetrofit?.create(APIService::class.java)
    }

    companion object {
        private var instance: APIManager? = null

        fun getInstance(context: Context): APIManager {
            if (instance == null) {
                instance = APIManager()
            }
            return instance!!
        }
    }

    fun getMonsterList(page: Int, limit: Int = 15): Observable<MonsterResponse> {
        val params = HashMap<String, String>()
        params["page"] = page.toString()
        params["limit"] = limit.toString()
        return mService?.getMonsterList(params)!!.subscribeOn(Schedulers.io())
    }

    fun getDungeonList(): Observable<DungeonResponse> {
        val params = HashMap<String, String>()
        return mService?.getDungeonList(params)!!.subscribeOn(Schedulers.io())
    }
}