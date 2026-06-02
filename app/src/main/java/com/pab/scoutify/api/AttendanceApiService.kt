package com.pab.scoutify.api

import com.pab.scoutify.model.ActiveActivity
import com.pab.scoutify.model.AttendanceStatus
import com.pab.scoutify.model.BaseResponse
import com.pab.scoutify.model.CheckInRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AttendanceApiService {
    @GET("attendance/current-activity")
    suspend fun getCurrentActivity(): Response<ActiveActivity>

    @POST("attendance/checkin")
    suspend fun checkIn(@Body request: CheckInRequest): Response<BaseResponse<Any>>

    @GET("attendance/status")
    suspend fun getAttendanceStatus(): Response<AttendanceStatus>
}
