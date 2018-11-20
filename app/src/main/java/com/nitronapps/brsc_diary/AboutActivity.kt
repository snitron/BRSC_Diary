package com.nitronapps.brsc_diary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nitronapps.brsc_diary.Data.APP_SETTINGS
import com.nitronapps.brsc_diary.Data.APP_VERSION
import com.nitronapps.brsc_diary.Data.SERVER_ADRESS
import com.nitronapps.brsc_diary.Models.NameModel
import com.nitronapps.brsc_diary.Models.PersonModel
import com.nitronapps.brsc_diary.Others.CustomTypefaceSpan
import com.nitronapps.brsc_diary.Others.IBRSC
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_about.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.android.synthetic.main.activity_result.view.*
import kotlinx.android.synthetic.main.app_bar_about.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_result.*
import kotlinx.android.synthetic.main.nav_header.view.*
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit

class AboutActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mServerAPI: IBRSC
    private var prefId = 0
    private val arrayListType: Type = object : TypeToken<ArrayList<String>>() {}.type
    private val callListNames = ArrayList<Call<NameModel>>()
    private var user: NameModel? = null
    private var login = ""
    private var password = ""
    private lateinit var ids: ArrayList<String>
    private var isParent = false
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbarAbout)

        mSharedPreferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layoutAbout, toolbarAbout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layoutAbout.addDrawerListener(toggle)
        toggle.syncState()

        mSharedPreferences = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)

        isParent = mSharedPreferences.getBoolean("isParent", false)
        prefId = mSharedPreferences.getInt("prefId", 0)

        login = mSharedPreferences.getString("login", "")!!

        password = mSharedPreferences.getString("password", "")!!

        ids = Gson().fromJson<ArrayList<String>>(
                mSharedPreferences.getString("ids", "[]"),
                arrayListType
        )

        count = mSharedPreferences.getInt("count", 0)

        val okhttp = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(SERVER_ADRESS)
                .client(okhttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        mServerAPI = retrofit.create(IBRSC::class.java)


        if (isParent) {
            nav_view.menu.clear()
            nav_view.inflateMenu(R.menu.menu_parent)
            nav_view.textViewParentNameAbout.visibility = View.VISIBLE
        } else {
            nav_view.menu.clear()
            nav_view.inflateMenu(R.menu.menu_student)
            nav_view.textViewParentNameAbout.visibility = View.GONE
        }

        if(!mSharedPreferences.contains("name"))
            getNames()
        else
            setPerson()

        nav_view.setNavigationItemSelectedListener(this)
        val menu = nav_view.menu

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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_diary -> startActivity(Intent(this, MainActivity::class.java))
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

            R.id.nav_about -> drawer_layoutAbout.closeDrawers()


            if (isParent) R.id.nav_children else -1 -> {
                if (user != null) {
                    AlertDialog.Builder(this)
                            .setTitle("Выберите аккаунт:")
                            .setItems(user!!.child_ids, { dialog, which ->
                                val name = Gson().fromJson(
                                        mSharedPreferences.getString("name", "[]"),
                                        NameModel::class.java
                                )
                                drawer_layout.closeDrawers()
                                prefId = which
                                mSharedPreferences.edit().putInt("prefId", which).apply()
                                setPersonName(name?.child_ids!![prefId])
                            }).create().show()
                } else
                    Toast.makeText(this, resources.getString(R.string.loading), Toast.LENGTH_LONG).show()
            }

            else -> {
            }
        }

        drawer_layoutAbout.closeDrawer(GravityCompat.START)
        return true
    }

    fun deleteAccount() {
        mSharedPreferences.edit().clear().apply()

        for (i in callListNames.iterator())
            if(!i.isCanceled && i.isExecuted)
                i.cancel()

        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("type", "first")

        startActivity(intent)

    }

    fun applyFontToMenuItem(mi: MenuItem) {
        val font = Typeface.createFromAsset(assets, "segoe_ui_light.ttf")
        val mNewName = SpannableString(mi.title)
        mNewName.setSpan(CustomTypefaceSpan("", font), 0, mNewName.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        mi.title = mNewName
    }

    fun getNames() {
        if (isParent) {
            callListNames.add(
                    mServerAPI.getName(
                            login,
                            password,
                            mSharedPreferences.getString("ids", "[]"),
                            APP_VERSION,
                            "multiply"
                    ))
            callListNames.last().enqueue(
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
            callListNames.add(
                    mServerAPI.getName(
                            login,
                            password,
                            ids[0],
                            APP_VERSION,
                            "one"
                    ))
            callListNames.last().enqueue(
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

    fun setPerson() {
        runOnUiThread {
            nav_view.removeHeaderView(nav_view.getHeaderView(0))
            val header = nav_view.inflateHeaderView(R.layout.nav_header)

            val textViewName = header.textViewName
            val parentName = nav_view.textViewParentNameAbout

            user = Gson().fromJson(
                    if (mSharedPreferences.contains("name")) mSharedPreferences.getString("name", "[]") else "[]",
                    NameModel::class.java
            )

            if (isParent) {
                textViewName.text = user?.child_ids!![prefId].replace("\"", "")

                parentName.text = resources.getString(R.string.parent_name)  + " " +  prepareParentName(user?.name!!)
            } else {
                textViewName.text = user?.name!!.replace("\"", "")
                parentName.visibility = View.GONE
            }
        }
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


    fun setPersonName(name: String) {
        nav_view.removeHeaderView(nav_view.getHeaderView(0))
        val header = nav_view.inflateHeaderView(R.layout.nav_header)

        header.textViewName.text = name.replace("\"", "")
    }

}

