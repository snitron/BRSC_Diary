package com.nitronapps.brsc_diary

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nitronapps.brsc_diary.Adapters.ResultsAdapter
import com.nitronapps.brsc_diary.Adapters.TableAdapter
import com.nitronapps.brsc_diary.Data.*
import com.nitronapps.brsc_diary.Models.PersonModel
import com.nitronapps.brsc_diary.Models.ResultModel
import com.nitronapps.brsc_diary.Models.TableModel
import com.nitronapps.brsc_diary.Others.CustomTypefaceSpan
import com.nitronapps.brsc_diary.Others.IBRSC

import kotlinx.android.synthetic.main.activity_table.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_table.*
import kotlinx.android.synthetic.main.content_table.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class TableActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var mServerAPI: IBRSC
    lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)
        setSupportActionBar(toolbar)

        swipeRefreshLayoutTable.setColorSchemeColors(Color.parseColor("#2980b9"), Color.parseColor("#e74c3c"), Color.parseColor("#f1c40f"), Color.parseColor("#2ecc71"))
        swipeRefreshLayoutTable.isRefreshing = true

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

        recyclerViewTable.layoutManager = LinearLayoutManager(this)

        initRecyclerView()

        setPerson()

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout_table, findViewById(R.id.toolbarTable), R.string.title_activity_login, R.string.title_activity_main2)
        drawer_layout_table.addDrawerListener(toggle)
        toggle.syncState()

        navigation_viewTable.setNavigationItemSelectedListener(this)

        val menu = navigation_viewTable.menu

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
            R.id.nav_diary -> {
                val intent = Intent(this, MainActivity::class.java)
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

    fun initRecyclerView() {
                if (mSharedPreferences.contains("saveTable")) {
                    val list = Gson().fromJson<List<Lesson>>(mSharedPreferences.getString("saveTable", ""),
                            object : TypeToken<List<Lesson>>() {}.type)

                    recyclerViewTable.layoutManager = LinearLayoutManager(this)
                    recyclerViewTable.adapter = TableAdapter(list)
                    recyclerViewTable.addItemDecoration(MainActivity.SpacesItemDecoration(10))
                } else
                    mServerAPI.getTable(mSharedPreferences.getString("login", ""),
                            mSharedPreferences.getString("password", ""),
                            mSharedPreferences.getString("id", "")).enqueue(
                            object : Callback<Array<TableModel>> {
                                override fun onFailure(call: Call<Array<TableModel>>, t: Throwable) {
                                    Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_LONG).show()
                                }

                                override fun onResponse(call: Call<Array<TableModel>>, response: Response<Array<TableModel>>) {
                                        val list = LinkedList<Lesson>()

                                        for (i in response.body()!!.iterator())
                                            list.add(i.getLesson())

                                        list.add(Lesson("", LinkedList<TableMarks>()))

                                        val adapter = TableAdapter(list)
                                        recyclerViewTable.adapter = adapter
                                        recyclerViewTable.addItemDecoration(MainActivity.SpacesItemDecoration(5))
                                        Log.w("table", "success")

                                        swipeRefreshLayoutTable.isRefreshing = false

                                }
                            }
                    )

                swipeRefreshLayoutTable.setOnRefreshListener {
                    mServerAPI.getTable(mSharedPreferences.getString("login", ""),
                            mSharedPreferences.getString("password", ""),
                            mSharedPreferences.getString("id", "")).enqueue(
                            object : Callback<Array<TableModel>> {
                                override fun onFailure(call: Call<Array<TableModel>>, t: Throwable) {
                                    Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_LONG).show()
                                }

                                override fun onResponse(call: Call<Array<TableModel>>, response: Response<Array<TableModel>>) {
                                        val list = LinkedList<Lesson>()

                                        for (i in response.body()!!.iterator())
                                            list.add(i.getLesson())

                                        val adapter = TableAdapter(list)
                                        recyclerViewTable.adapter = adapter
                                        recyclerViewTable.addItemDecoration(MainActivity.SpacesItemDecoration(5))
                                        Log.w("table", "success")

                                        swipeRefreshLayoutTable.isRefreshing = false

                                }
                            }
                    )
                }
            }

    fun setPerson() {
        if (mSharedPreferences.contains("wasLogin") || mSharedPreferences.getBoolean("wasLogin", false)) {
            if (mSharedPreferences.contains("wasPersonGot") && mSharedPreferences.getBoolean("wasPersonGot", false)) {
                navigation_viewTable.removeHeaderView(navigation_viewTable.getHeaderView(0))
                val view = navigation_viewTable.inflateHeaderView(R.layout.nav_header)
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
                                navigation_viewTable.removeHeaderView(navigation_viewTable.getHeaderView(0))
                                val view = navigation_viewTable.inflateHeaderView(R.layout.nav_header)
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

    fun deleteAccount(){
        mSharedPreferences.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    fun applyFontToMenuItem(mi: MenuItem){
        val font = Typeface.createFromAsset(assets, "segoe_ui_light.ttf")
        val mNewName = SpannableString(mi.title)
        mNewName.setSpan(CustomTypefaceSpan("", font), 0, mNewName.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        mi.title = mNewName
    }
}
