package com.nitronapps.brsc_diary

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.nitronapps.brsc_diary.Adapters.DayAdapter
import com.nitronapps.brsc_diary.Data.APP_SETTINGS
import com.nitronapps.brsc_diary.Data.SERVER_ADRESS
import com.nitronapps.brsc_diary.Models.DayModel
import com.nitronapps.brsc_diary.Models.PersonModel
import com.nitronapps.brsc_diary.Others.CustomTypefaceSpan
import com.nitronapps.brsc_diary.Others.IBRSC
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var mSharedPreferences: SharedPreferences
    lateinit var mServerAPI: IBRSC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSharedPreferences = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)

        if (!mSharedPreferences.contains("wasLogin") || !mSharedPreferences.getBoolean("wasLogin", false))
            startActivity(Intent(this, LoginActivity::class.java))

        if (mSharedPreferences.contains("saved")) {
            val day = Gson().fromJson(mSharedPreferences.getString("saved", "[]"), Array<DayModel>::class.java)
            recyclerView.adapter = DayAdapter(day, applicationContext)
            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        }

        mSharedPreferences.edit().putInt("curWeek", GregorianCalendar().get(GregorianCalendar.WEEK_OF_YEAR)).apply()

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

        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#2980b9"), Color.parseColor("#e74c3c"), Color.parseColor("#f1c40f"), Color.parseColor("#2ecc71"))
        swipeRefreshLayout.isRefreshing = true
        swipeRefreshLayout.setOnRefreshListener {
            mServerAPI.getDiary("getMod",
                    mSharedPreferences.getString("login", ""),
                    mSharedPreferences.getString("password", ""),
                    mSharedPreferences.getInt("curWeek", 0).toString(),
                    mSharedPreferences.getString("id", ""))
                    .enqueue(object : retrofit2.Callback<Array<DayModel>> {
                        override fun onFailure(call: Call<Array<DayModel>>, t: Throwable) {
                            Toast.makeText(applicationContext, "ERR CONNECT", Toast.LENGTH_SHORT).show()
                            swipeRefreshLayout.isRefreshing = false
                        }

                        override fun onResponse(call: Call<Array<DayModel>>, response: Response<Array<DayModel>>) {

                            Log.w("logpass", response.isSuccessful.toString())
                            swipeRefreshLayout.isRefreshing = false

                            if (response.isSuccessful) {
                                recyclerView.adapter = DayAdapter(response.body()!!, applicationContext)
                                recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                                Toast.makeText(applicationContext, "OK CONNECT", Toast.LENGTH_SHORT).show()
                            }
                        }

                    })
        }

        recyclerView.addItemDecoration(SpacesItemDecoration(10))

        if (mSharedPreferences.contains("wasLogin") || mSharedPreferences.getBoolean("wasLogin", false))
            mServerAPI.getDiary("getMod",
                    mSharedPreferences.getString("login", ""),
                    mSharedPreferences.getString("password", ""),
                    mSharedPreferences.getInt("curWeek", 0).toString(),
                    mSharedPreferences.getString("id", ""))
                    .enqueue(object : retrofit2.Callback<Array<DayModel>> {
                        override fun onFailure(call: Call<Array<DayModel>>, t: Throwable) {
                            Toast.makeText(applicationContext, "ERR CONNECT", Toast.LENGTH_SHORT).show()
                            swipeRefreshLayout.isRefreshing = false
                        }

                        override fun onResponse(call: Call<Array<DayModel>>, response: Response<Array<DayModel>>) {

                            Log.w("logpass", response.isSuccessful.toString())
                            swipeRefreshLayout.isRefreshing = false

                            if (response.isSuccessful) {
                                recyclerView.adapter = DayAdapter(response.body()!!, applicationContext)
                                recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                                Toast.makeText(applicationContext, "OK CONNECT", Toast.LENGTH_SHORT).show()

                                mSharedPreferences.edit().putString("saved", Gson().toJson(response.body())).apply()
                            }
                        }

                    })

        setPerson()

        buttonNext.setOnClickListener {
            swipeRefreshLayout.isRefreshing = true

            mServerAPI.getDiary("getMod",
                    mSharedPreferences.getString("login", ""),
                    mSharedPreferences.getString("password", ""),
                    (mSharedPreferences.getInt("curWeek", 0) + 1).toString(),
                    mSharedPreferences.getString("id", "test"))
                    .enqueue(object : retrofit2.Callback<Array<DayModel>> {
                        override fun onFailure(call: Call<Array<DayModel>>, t: Throwable) {
                            Toast.makeText(applicationContext, "ERR CONNECT", Toast.LENGTH_SHORT).show()
                            swipeRefreshLayout.isRefreshing = false
                        }

                        override fun onResponse(call: Call<Array<DayModel>>, response: Response<Array<DayModel>>) {
                            swipeRefreshLayout.isRefreshing = false

                            if (response.isSuccessful) {
                                recyclerView.adapter = DayAdapter(response.body()!!, applicationContext)
                                recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                                Toast.makeText(applicationContext, "OK CONNECT", Toast.LENGTH_SHORT).show()

                                mSharedPreferences.edit().putInt("curWeek", mSharedPreferences.getInt("curWeek", 0) + 1).apply()
                            }
                        }

                    })
        }

        buttonPrev.setOnClickListener {
            swipeRefreshLayout.isRefreshing = true

            mServerAPI.getDiary("getMod",
                    mSharedPreferences.getString("login", ""),
                    mSharedPreferences.getString("password", ""),
                    (mSharedPreferences.getInt("curWeek", 0) - 1).toString(),
                    mSharedPreferences.getString("id", "test"))
                    .enqueue(object : retrofit2.Callback<Array<DayModel>> {
                        override fun onFailure(call: Call<Array<DayModel>>, t: Throwable) {
                            Toast.makeText(applicationContext, "ERR CONNECT", Toast.LENGTH_SHORT).show()
                            swipeRefreshLayout.isRefreshing = false
                        }

                        override fun onResponse(call: Call<Array<DayModel>>, response: Response<Array<DayModel>>) {
                            swipeRefreshLayout.isRefreshing = false

                            if (response.isSuccessful) {
                                recyclerView.adapter = DayAdapter(response.body()!!, applicationContext)
                                recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                                Toast.makeText(applicationContext, "OK CONNECT", Toast.LENGTH_SHORT).show()

                                mSharedPreferences.edit().putInt("curWeek", mSharedPreferences.getInt("curWeek", 0) - 1).apply()
                            }
                        }

                    })
        }


        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.title_activity_login, R.string.title_activity_main2)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        navigation_view.setNavigationItemSelectedListener(this)
        val menu = navigation_view.menu

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

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.nav_table -> {
                val intent = Intent(this, TableActivity::class.java)
                intent.putExtra("type", "table")
                intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }

            R.id.nav_results -> {
                val intent = Intent(this, TableActivity::class.java)
                intent.putExtra("type", "results")
                intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION)

                startActivity(intent)
            }

            R.id.nav_quit -> {
                AlertDialog.Builder(this)
                        .setTitle("Подтвердите действие")
                        .setMessage("Вы действительно хотите выйти из аккаунта?")
                        .setPositiveButton("Да", { dialog, which ->
                            deleteAccount()
                        })
                        .setNegativeButton("Нет", { dialog, which ->
                        })
                        .show()
            }

            R.id.nav_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION)

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
                navigation_view.removeHeaderView(navigation_view.getHeaderView(0))
                val view = navigation_view.inflateHeaderView(R.layout.nav_header)
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
                                Log.w("getName", t.message)
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


    fun deleteAccount(){
        mSharedPreferences.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

            outRect.bottom = space
        }
    }
}
