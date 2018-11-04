package com.nitronapps.brsc_diary

import com.nitronapps.brsc_diary.Models.DayModel
import com.nitronapps.brsc_diary.Models.PersonModel
import com.nitronapps.brsc_diary.Models.ResultModel
import com.nitronapps.brsc_diary.Models.TableModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IBRSC {
    @GET("getId.php")
    fun getId(@Query("login") login : String,
                @Query("password") password: String): Call<String>

    @GET("parseMain.php")
    fun getDiary(@Query("action") action: String?,
                 @Query("login") login: String?,
                 @Query("password") password: String?,
                 @Query("week") week: String?,
                 @Query("userID") userID: String?
                 ): Call<Array<DayModel>>

    @GET("parseTable.php")
    fun getTable(@Query("login") login : String?,
                 @Query("password") password: String?,
                 @Query("userID") userID: String?): Call<Array<TableModel>>

    @POST("parseResults.php")
    fun getResults(@Query("login") login : String?,
                 @Query("password") password: String?,
                 @Query("userID") userID: String?): Call<Array<ResultModel>>

    @GET("getName.php")
    fun getName(@Query("login") login : String?,
                   @Query("password") password: String?,
                   @Query("userID") userID: String?): Call<PersonModel>
}