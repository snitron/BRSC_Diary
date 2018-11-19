package com.nitronapps.brsc_diary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.nitronapps.brsc_diary.Data.APP_SETTINGS
import com.nitronapps.brsc_diary.Models.PersonModel
import com.nitronapps.brsc_diary.Others.CustomTypefaceSpan
import com.nitronapps.brsc_diary.Others.IBRSC
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_about.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AboutActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var mSharedPreferences:SharedPreferences
    lateinit var mServerAPI: IBRSC
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbarAbout)

        mSharedPreferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layoutAbout, toolbarAbout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layoutAbout.addDrawerListener(toggle)
        toggle.syncState()
        setPerson()

        nav_view.setNavigationItemSelectedListener(this)
        val menu = nav_view.menu

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


            else ->{}
        }

        drawer_layoutAbout.closeDrawer(GravityCompat.START)
        return true
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

    fun setPerson() {
        if (mSharedPreferences.contains("wasLogin") || mSharedPreferences.getBoolean("wasLogin", false)) {
            if (mSharedPreferences.contains("wasPersonGot") && mSharedPreferences.getBoolean("wasPersonGot", false)) {
                nav_view.removeHeaderView(nav_view.getHeaderView(0))
                val view = nav_view.inflateHeaderView(R.layout.nav_header)
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

}

