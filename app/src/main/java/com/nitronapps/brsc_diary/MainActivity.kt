package com.nitronapps.brsc_diary

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
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
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nitronapps.brsc_diary.Adapters.DayAdapter
import com.nitronapps.brsc_diary.Data.APP_SETTINGS
import com.nitronapps.brsc_diary.Data.SERVER_ADRESS
import com.nitronapps.brsc_diary.Models.DayModel
import com.nitronapps.brsc_diary.Models.NameModel
import com.nitronapps.brsc_diary.Models.UserModel
import com.nitronapps.brsc_diary.Others.CustomTypefaceSpan
import com.nitronapps.brsc_diary.Others.IBRSC
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header.view.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mServerAPI: IBRSC
    private lateinit var mOkHttpClient: OkHttpClient
    private var prefId = 0
    private val arrayListType: Type = object : TypeToken<ArrayList<String>>() {}.type

    private var login = ""
    private var password = ""
    private lateinit var ids: ArrayList<String>
    private var isParent = false
    private var count = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSharedPreferences = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)

        if (!mSharedPreferences.contains("wasLogin") || !mSharedPreferences.getBoolean("wasLogin", false)) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("type", "first")
            startActivity(intent)
        }

        if (mSharedPreferences.contains("saved")) {
            val day = Gson().fromJson(mSharedPreferences.getString("saved", "[]"), Array<DayModel>::class.java)
            recyclerView.adapter = DayAdapter(day, applicationContext)
            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        }

        mSharedPreferences.edit().putInt("curWeek", GregorianCalendar().get(GregorianCalendar.WEEK_OF_YEAR)).apply()

        isParent = mSharedPreferences.getBoolean("isParent", false)
        prefId = mSharedPreferences.getInt("prefId", 0)

        login = mSharedPreferences.getString("login", "")!!

        password = mSharedPreferences.getString("password", "")!!

        ids = Gson().fromJson<ArrayList<String>>(
                mSharedPreferences.getString("ids", "[]"),
                arrayListType
           )

        count = mSharedPreferences.getInt("count", 0)


        mOkHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(SERVER_ADRESS)
                .client(mOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(
                        GsonBuilder()
                                .setLenient()
                                .create()
                ))
                .build()

        mServerAPI = retrofit.create(IBRSC::class.java)

        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#2980b9"), Color.parseColor("#e74c3c"), Color.parseColor("#f1c40f"), Color.parseColor("#2ecc71"))
        swipeRefreshLayout.isRefreshing = true
        swipeRefreshLayout.setDistanceToTriggerSync(20)


        swipeRefreshLayout.setOnRefreshListener {
            initRecyclerView()
        }

        recyclerView.addItemDecoration(SpacesItemDecoration(10))

        if (mSharedPreferences.contains("wasLogin") || mSharedPreferences.getBoolean("wasLogin", false))
            initRecyclerView()


        if (!mSharedPreferences.contains("name"))
            getNames()
        else
            setPerson()

        buttonNext.setOnClickListener {
            var curId = ""
            if(isParent)
                curId = ids[prefId]
            else
                curId = ids[0]

            swipeRefreshLayout.isRefreshing = true

            mServerAPI.getDiary(
                    login,
                    password,
                    if ((mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() in 35.0..52.0 ||
                            (mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() in 1.0..22.0)
                        (mSharedPreferences.getInt("curWeek", 1) + 1).toString()
                    else
                        "1",
                    curId
            )
                    .enqueue(object : retrofit2.Callback<Array<DayModel>> {
                        override fun onFailure(call: Call<Array<DayModel>>, t: Throwable) {
                            Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_SHORT).show()
                            swipeRefreshLayout.isRefreshing = false
                            Log.w("mainError", t.message)
                        }

                        override fun onResponse(call: Call<Array<DayModel>>, response: Response<Array<DayModel>>) {
                            swipeRefreshLayout.isRefreshing = false

                            Log.w("mainSucces", Gson().toJson(response.body()))
                            if (response.isSuccessful && response.body() != null) {
                                recyclerView.adapter = DayAdapter(response.body()!!, applicationContext)
                                recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                                if ((mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() !in 35.0..52.0 &&
                                        (mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() !in 1.0..22.0)
                                    mSharedPreferences.edit().putInt("curWeek", mSharedPreferences.getInt("curWeek", 0) + 1).apply()
                            } else
                                Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_SHORT).show()

                        }

                    })
        }

        buttonPrev.setOnClickListener {
            var curId = ""
            if(isParent)
                curId = ids[prefId]
            else
                curId = ids[0]

            swipeRefreshLayout.isRefreshing = true

            mServerAPI.getDiary(
                    login,
                    password,
                    if ((mSharedPreferences.getInt("curWeek", 1) - 1).toDouble() in 35.0..52.0 ||
                            (mSharedPreferences.getInt("curWeek", 1) - 1).toDouble() in 1.0..22.0)
                        (mSharedPreferences.getInt("curWeek", 1) - 1).toString()
                    else
                        "1",
                    curId
            )
                    .enqueue(object : retrofit2.Callback<Array<DayModel>> {
                        override fun onFailure(call: Call<Array<DayModel>>, t: Throwable) {
                            Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_SHORT).show()
                            swipeRefreshLayout.isRefreshing = false
                            Log.w("mainError", t.message)
                        }

                        override fun onResponse(call: Call<Array<DayModel>>, response: Response<Array<DayModel>>) {
                            swipeRefreshLayout.isRefreshing = false

                            Log.w("mainSucces", Gson().toJson(response.body()))
                            if (response.isSuccessful && response.body() != null) {
                                recyclerView.adapter = DayAdapter(response.body()!!, applicationContext)
                                recyclerView.layoutManager = LinearLayoutManager(applicationContext)

                                if ((mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() !in 35.0..52.0 &&
                                        (mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() !in 1.0..22.0)
                                    mSharedPreferences.edit().putInt("curWeek", mSharedPreferences.getInt("curWeek", 0) - 1).apply()
                            } else
                                Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_SHORT).show()
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

        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)

            val subMenu = menuItem.subMenu

            if (subMenu != null && subMenu.size() != 0)
                for (j in 0 until subMenu.size()) {
                    val subItem = subMenu.getItem(i)
                    applyFontToMenuItem(subItem)
                }

            applyFontToMenuItem(menuItem)
        }
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.nav_diary -> drawer_layout.closeDrawers()

            R.id.nav_table -> {
                val intent = Intent(this, TableActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }

            R.id.nav_results -> {
                val intent = Intent(this, ResultActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION)
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
                intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION)

                startActivity(intent)
            }


            else -> {
            }
        }
        return true
    }

    fun setPerson() {
        runOnUiThread {
            navigation_view.removeHeaderView(navigation_view.getHeaderView(0))
            val header = navigation_view.inflateHeaderView(R.layout.nav_header)

            val textViewName = header.textViewName
            val buttonChange = header.textViewChangeAccount
            val parentName = navigation_view.textViewParentNameMain

            val user = Gson().fromJson(
                    if (mSharedPreferences.contains("name")) mSharedPreferences.getString("name", "[]") else "[]",
                    NameModel::class.java
            )

            if (isParent) {
                textViewName.text = user.child_ids!![prefId].replace("\"", "")
                buttonChange.setOnClickListener {
                    AlertDialog.Builder(this)
                            .setTitle("Выберите аккаунт:")
                            .setItems(user.child_ids, { dialog, which ->
                                drawer_layout.closeDrawers()
                                prefId = which
                                textViewName.text = user.child_ids[prefId].replace("\"", "")
                                mSharedPreferences.edit().putInt("prefId", which).apply()
                                recyclerView.adapter = null
                                initRecyclerView()
                            }).create().show()

                }
                buttonChange.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                parentName.text = "Родитель: " + prepareParentName(user.name!!)
            } else {
               textViewName.text = user.name!!.replace("\"", "")
               buttonChange.visibility = View.GONE
               parentName.visibility = View.GONE
            }
        }
    }

    fun applyFontToMenuItem(mi: MenuItem) {
        val font = Typeface.createFromAsset(assets, "segoe_ui_light.ttf")
        val mNewName = SpannableString(mi.title)
        mNewName.setSpan(CustomTypefaceSpan("", font), 0, mNewName.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        mi.title = mNewName
    }

    fun deleteAccount() {
        mSharedPreferences.edit().clear().apply()
        recyclerView.adapter = null
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("type", "first")

        startActivity(intent)
    }


    /*
    * getNames()
    * void
    * Gets names from server
    * */

    fun getNames() {
        if (isParent) {
            mServerAPI.getName(
                    login,
                    password,
                    mSharedPreferences.getString("ids", "[]"),
                    "test",
                    "multiply"
            ).enqueue(
                    object : Callback<NameModel> {
                        override fun onFailure(call: Call<NameModel>, t: Throwable) {

                        }

                        override fun onResponse(call: Call<NameModel>, response: Response<NameModel>) {
                            mSharedPreferences.edit().putString("name", Gson().toJson(response.body())).apply()
                            setPerson()
                        }
                    }
            )
        } else {

            mServerAPI.getName(
                    login,
                    password,
                    ids[0],
                    "test",
                    "one"
            ).enqueue(
                    object : Callback<NameModel> {
                        override fun onFailure(call: Call<NameModel>, t: Throwable) {

                        }

                        override fun onResponse(call: Call<NameModel>, response: Response<NameModel>) {
                            mSharedPreferences.edit().putString("name", Gson().toJson(response.body())).apply()
                            setPerson()
                        }
                    }
            )
        }
    }


    fun initRecyclerView() {
        var curId = ""
        if(isParent)
            curId = ids[prefId]
        else
            curId = ids[0]

        swipeRefreshLayout.isRefreshing = true

        mServerAPI.getDiary(
                login,
                password,
                mSharedPreferences.getInt("curWeek", 1).toString(),
                curId
        )
                .enqueue(object : retrofit2.Callback<Array<DayModel>> {
                    override fun onFailure(call: Call<Array<DayModel>>, t: Throwable) {
                        Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_SHORT).show()
                        swipeRefreshLayout.isRefreshing = false
                        Log.w("mainError", t.message)
                    }

                    override fun onResponse(call: Call<Array<DayModel>>, response: Response<Array<DayModel>>) {

                        swipeRefreshLayout.isRefreshing = false

                        Log.w("mainSucces", Gson().toJson(response.body()))
                        if (response.body() != null) {
                            recyclerView.adapter = DayAdapter(response.body()!!, applicationContext)
                            recyclerView.layoutManager = LinearLayoutManager(applicationContext)

                            if ((mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() !in 35.0..52.0 &&
                                    (mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() !in 1.0..22.0)
                                mSharedPreferences.edit().putString("saved", Gson().toJson(response.body())).apply()
                        } else
                            Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_SHORT).show()
                    }

                })
    }

    fun printHeaders(headers: Headers): String {
        var result = String()
        for (i in 0 until headers.size())
            result += headers.name(i) + " " + headers.value(i) + "\n"

        return result
    }

    fun prepareParentName(name: String):String{
        var length = 0
        loop@ for (i in name.iterator())
            when (i) {
                '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> break@loop
                else -> length++
            }

        return name.substring(0, length)
    }

    class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

            outRect.bottom = space
        }
    }

}
