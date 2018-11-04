package com.nitronapps.brsc_diary

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.nitronapps.brsc_diary.Adapters.TableAdapter
import com.nitronapps.brsc_diary.Data.Lesson
import com.nitronapps.brsc_diary.Models.TableModel

import kotlinx.android.synthetic.main.activity_table.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_table.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class TableActivity : AppCompatActivity() {

    val SERVER_ADRESS = "https://brsc-diary-server.herokuapp.com/web/"
    val APP_SETTINGS = "account"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)
        setSupportActionBar(toolbar)

        swipeRefreshLayoutTable.setColorSchemeColors(Color.parseColor("#2980b9"), Color.parseColor("#e74c3c"), Color.parseColor("#f1c40f"), Color.parseColor("#2ecc71"))
        swipeRefreshLayoutTable.isRefreshing = true

        val mSharedPreferences = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)

        val okhttp = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(SERVER_ADRESS)
                .client(okhttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val serverApi = retrofit.create(IBRSC::class.java)

        recyclerViewTable.layoutManager = LinearLayoutManager(this)

        serverApi.getTable(mSharedPreferences.getString("login", ""),
                           mSharedPreferences.getString("password", ""),
                           mSharedPreferences.getString("id", "")).enqueue(
                        object: Callback<Array<TableModel>>{
                            override fun onFailure(call: Call<Array<TableModel>>, t: Throwable) {
                                Log.w("table", t.message)
                            }

                            override fun onResponse(call: Call<Array<TableModel>>, response: Response<Array<TableModel>>) {
                                val list = LinkedList<Lesson>()

                                for(i in response.body()!!.iterator())
                                    list.add(i.getLesson())

                                val adapter = TableAdapter(list)
                                recyclerViewTable.adapter = adapter
                                Log.w("table", "success")

                                swipeRefreshLayoutTable.isRefreshing = false
                            }
                        }
                )
    }
}
