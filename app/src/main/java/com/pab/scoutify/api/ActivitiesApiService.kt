package com.pab.scoutify.api

import com.pab.scoutify.model.ActivityItem
import com.pab.scoutify.model.BaseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ActivitiesApiService {
    @GET("activities")
    suspend fun getActivities(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<BaseResponse<List<ActivityItem>>>
}
