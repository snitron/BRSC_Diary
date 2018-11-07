package com.nitronapps.brsc_diary.Others

import com.nitronapps.brsc_diary.Models.DayModel
import com.nitronapps.brsc_diary.Models.PersonModel
import com.nitronapps.brsc_diary.Models.ResultModel
import com.nitronapps.brsc_diary.Models.TableModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers


interface IBRSC {
    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("getId.php")
    fun getId(@Query("login") login : String,
                @Query("password") password: String): Call<String>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("parseMain.php")
    fun getDiary(@Query("action") action: String?,
                 @Query("login") login: String?,
                 @Query("password") password: String?,
                 @Query("week") week: String?,
                 @Query("userID") userID: String?
                 ): Call<Array<DayModel>>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("parseTable.php")
    fun getTable(@Query("login") login : String?,
                 @Query("password") password: String?,
                 @Query("userID") userID: String?): Call<Array<TableModel>>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("parseResults.php")
    fun getResults(@Query("login") login : String?,
                 @Query("password") password: String?,
                 @Query("userID") userID: String?): Call<Array<ResultModel>>

    @Headers("User-Agent: Nitron Apps BRSC Diary Http Connector")
    @GET("getName.php")
    fun getName(@Query("login") login : String?,
                   @Query("password") password: String?,
                   @Query("userID") userID: String?): Call<PersonModel>
}