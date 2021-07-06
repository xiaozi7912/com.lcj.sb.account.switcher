package com.lcj.sb.account.switcher.http

import com.lcj.sb.account.switcher.http.response.DungeonResponse
import com.lcj.sb.account.switcher.http.response.MonsterResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface APIService {
    @GET("api/monster")
    fun getMonsterList(@QueryMap params: Map<String, String>): Observable<MonsterResponse>

    @GET("api/dungeon")
    fun getDungeonList(@QueryMap params: Map<String, String>): Observable<DungeonResponse>
}