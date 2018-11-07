package com.nitronapps.brsc_diary

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nitronapps.brsc_diary.Adapters.ResultsAdapter
import com.nitronapps.brsc_diary.Adapters.TableAdapter
import com.nitronapps.brsc_diary.Data.APP_SETTINGS
import com.nitronapps.brsc_diary.Data.Lesson
import com.nitronapps.brsc_diary.Data.SERVER_ADRESS
import com.nitronapps.brsc_diary.Models.PersonModel
import com.nitronapps.brsc_diary.Models.ResultModel
import com.nitronapps.brsc_diary.Models.TableModel
import com.nitronapps.brsc_diary.Others.CustomTypefaceSpan
import com.nitronapps.brsc_diary.Others.IBRSC
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.android.synthetic.main.app_bar_about.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_result.*
import kotlinx.android.synthetic.main.content_result.*
import kotlinx.android.synthetic.main.content_table.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class ResultActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var mServerAPI: IBRSC
    lateinit var mSharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        setSupportActionBar(toolbarAbout)

        swipeRefreshLayoutResult.setColorSchemeColors(Color.parseColor("#2980b9"), Color.parseColor("#e74c3c"), Color.parseColor("#f1c40f"), Color.parseColor("#2ecc71"))
        swipeRefreshLayoutResult.isRefreshing = true

        mSharedPreferences = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)

        val okhttp = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(SERVER_ADRESS)
                .client(okhttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        mServerAPI = retrofit.create(IBRSC::class.java)

        recyclerViewResult.layoutManager = LinearLayoutManager(this)


        initRecyclerView()
        setPerson()

        val toggle = ActionBarDrawerToggle(
                this, drawer_layoutResult, toolbarResult, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layoutResult.addDrawerListener(toggle)
        toggle.syncState()

        nav_viewResult.setNavigationItemSelectedListener(this)

        val menu = nav_viewResult.menu

        for(i in 0 until menu.size()){
            val menuItem = menu.getItem(i)

            val subMenu = menuItem.subMenu

            if(subMenu != null && subMenu.size() != 0)
                for(j in 0 until subMenu.size()){
                    val subItem = subMenu.getItem(i)
                    applyFontToMenuItem(subItem)
                }

            applyFontToMenuItem(menuItem)
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_table -> {
                val intent = Intent(this, TableActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }

            R.id.nav_results -> {
                val intent = Intent(this, ResultActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

                startActivity(intent)
            }

            R.id.nav_quit -> {
                AlertDialog.Builder(this)
                        .setTitle(resources.getString(R.string.accept))
                        .setMessage(resources.getString(R.string.accept_2))
                        .setPositiveButton(resources.getString(R.string.yes), { dialog, which ->
                            deleteAccount()
                        })
                        .setNegativeButton(R.string.no, { dialog, which ->
                        })
                        .show()
            }


            R.id.nav_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

                startActivity(intent)
            }

            else -> {
            }
        }
        return true
    }


    fun setPerson() {
        if (mSharedPreferences.contains("wasLogin") || mSharedPreferences.getBoolean("wasLogin", false)) {
            if (mSharedPreferences.contains("wasPersonGot") && mSharedPreferences.getBoolean("wasPersonGot", false)) {
                nav_viewResult.removeHeaderView(nav_viewResult.getHeaderView(0))
                val view = nav_viewResult.inflateHeaderView(R.layout.nav_header)
                val imageView = view.findViewById<ImageView>(R.id.imageViewPerson)
                val name = view.findViewById<TextView>(R.id.textViewName)

                val person = Gson().fromJson(mSharedPreferences.getString("person", "[]"), PersonModel::class.java)

                imageView.setImageBitmap(person.decodeImage())
                name.text = person.name
            } else
                mServerAPI.getName(mSharedPreferences.getString("login", ""),
                        mSharedPreferences.getString("password", ""),
                        mSharedPreferences.getString("id", "test")).enqueue(
                        object : Callback<PersonModel> {
                            override fun onFailure(call: Call<PersonModel>, t: Throwable) {
                                Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_LONG).show()
                            }

                            override fun onResponse(call: Call<PersonModel>, response: Response<PersonModel>) {
                                navigation_view.removeHeaderView(navigation_view.getHeaderView(0))
                                val view = navigation_view.inflateHeaderView(R.layout.nav_header)
                                val imageView = view.findViewById<ImageView>(R.id.imageViewPerson)
                                val name = view.findViewById<TextView>(R.id.textViewName)

                                imageView.setImageBitmap(response.body()?.decodeImage())
                                name.text = response.body()?.name?.trim()

                                mSharedPreferences.edit().putBoolean("wasPersonGot", true).apply()
                                mSharedPreferences.edit().putString("person", Gson().toJson(PersonModel(response.body()?.name?.trim()!!, response.body()?.img!!))).apply()

                                Log.w("getName", "success")
                                Log.w("getName", response.body()?.name!!.trim())
                                Log.w("getName", response.body()?.img)
                            }
                        }
                )
        }
    }

    fun applyFontToMenuItem(mi: MenuItem){
        val font = Typeface.createFromAsset(assets, "segoe_ui_light.ttf")
        val mNewName = SpannableString(mi.title)
        mNewName.setSpan(CustomTypefaceSpan("", font), 0, mNewName.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        mi.title = mNewName
    }

    fun initRecyclerView() {
            mServerAPI.getResults(mSharedPreferences.getString("login", ""),
                    mSharedPreferences.getString("password", ""),
                    mSharedPreferences.getString("id", "")).enqueue(
                    object : Callback<Array<ResultModel>> {
                        override fun onFailure(call: Call<Array<ResultModel>>, t: Throwable) {
                            Log.w("table", t.message)
                        }

                        override fun onResponse(call: Call<Array<ResultModel>>, response: Response<Array<ResultModel>>) {

                                mSharedPreferences.edit().putString("saveResults", Gson().toJson(response.body())).apply()

                                val adapter = ResultsAdapter(response.body()!!)
                                recyclerViewResult.adapter = adapter
                                recyclerViewResult.addItemDecoration(MainActivity.SpacesItemDecoration(5))
                                Log.w("table", "success")

                                swipeRefreshLayoutResult.isRefreshing = false
                            }
                        }
            )

        swipeRefreshLayoutResult.setOnRefreshListener {
            mServerAPI.getResults(mSharedPreferences.getString("login", ""),
                    mSharedPreferences.getString("password", ""),
                    mSharedPreferences.getString("id", "")).enqueue(
                    object : Callback<Array<ResultModel>> {
                        override fun onFailure(call: Call<Array<ResultModel>>, t: Throwable) {
                            Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_LONG).show()
                        }

                        override fun onResponse(call: Call<Array<ResultModel>>, response: Response<Array<ResultModel>>) {

                            mSharedPreferences.edit().putString("saveResults", Gson().toJson(response.body())).apply()

                            val adapter = ResultsAdapter(response.body()!!)
                            recyclerViewResult.adapter = adapter
                            recyclerViewResult.addItemDecoration(MainActivity.SpacesItemDecoration(5))
                            Log.w("table", "success")

                            swipeRefreshLayoutResult.isRefreshing = false
                        }
                    }
            )
        }
}

    fun deleteAccount(){
        mSharedPreferences.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
