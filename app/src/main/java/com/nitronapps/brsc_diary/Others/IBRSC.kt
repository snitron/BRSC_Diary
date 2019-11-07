package com.nitronapps.brsc_diary.Others

import com.google.gson.annotations.SerializedName
import com.nitronapps.brsc_diary.Data.Balance
import com.nitronapps.brsc_diary.Data.Departments
import com.nitronapps.brsc_diary.Data.Info
import com.nitronapps.brsc_diary.Models.*
import retrofit2.Call
import retrofit2.http.*


interface IBRSC {
    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("getId")
    fun getId(@Query("login") login: String,
              @Query("password") password: String): Call<UserModel>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("parseMain")
    fun getDiary(@Query("login") login: String?,
                 @Query("password") password: String?,
                 @Query("week") week: String?,
                 @Query("id") userID: String?,
                 @Query("rooId") rooId: String?,
                 @Query("departmentId") departmentId: String?,
                 @Query("instituteId") instituteId: String?,
                 @Query("year") year: String?): Call<Array<DayModel>>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("parseTable")
    fun getTable(@Query("login") login: String?,
                 @Query("password") password: String?,
                 @Query("id") userID: String?,
                 @Query("rooId") rooId: String?,
                 @Query("departmentId") departmentId: String?,
                 @Query("instituteId") instituteId: String?): Call<Array<TableModel>>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("parseResults")
    fun getResults(@Query("login") login: String?,
                   @Query("password") password: String?,
                   @Query("id") userID: String?,
                   @Query("rooId") rooId: String?,
                   @Query("departmentId") departmentId: String?,
                   @Query("instituteId") instituteId: String?): Call<Array<ResultModel>>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("getDiaryYears")
    fun getDiaryYears(@Query("login") login: String?,
                      @Query("password") password: String?,
                      @Query("id") userID: String?,
                      @Query("rooId") rooId: String?,
                      @Query("departmentId") departmentId: String?,
                      @Query("instituteId") instituteId: String?): Call<Array<Departments>>
    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("getBalance")
    fun getBalance(@Query("login") login: String,
                   @Query("password") password: String): Call<BalanceCall>
}

data class BalanceCall(@SerializedName("isChild") val isChild: Boolean,
                       @SerializedName("res") var res: ArrayList<Balance>,
                       @SerializedName("info") var info: ArrayList<Info>){}