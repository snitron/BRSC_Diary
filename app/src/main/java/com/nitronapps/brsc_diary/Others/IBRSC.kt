package com.nitronapps.brsc_diary.Others

import com.nitronapps.brsc_diary.Models.*
import retrofit2.Call
import retrofit2.http.*


interface IBRSC {
    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("getId.php")
    fun getId(@Query("login") login: String,
              @Query("password") password: String,
              @Query("version") version: String): Call<UserModel>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("parseMain.php")
    fun getDiary(@Query("login") login: String?,
                 @Query("password") password: String?,
                 @Query("week") week: String?,
                 @Query("userID") userID: String?
                 ): Call<Array<DayModel>>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("parseTable.php")
    fun getTable(@Query("login") login: String?,
                 @Query("password") password: String?,
                 @Query("userID") userID: String?): Call<Array<TableModel>>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("parseResults.php")
    fun getResults(@Query("login") login: String?,
                   @Query("password") password: String?,
                   @Query("userID") userID: String?): Call<Array<ResultModel>>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("getName.php")
    fun getName(@Query("login") login: String?,
                @Query("password") password: String?,
                @Query("child_ids") userID: String?,
                @Query("version") version: String,
                @Query("option") option: String): Call<NameModel>
}