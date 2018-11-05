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
import android.view.Menu
import android.view.MenuItem
import com.nitronapps.brsc_diary.Data.APP_SETTINGS
import com.nitronapps.brsc_diary.Others.CustomTypefaceSpan
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_about.*

class AboutActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var mSharedPreferences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbarAbout)

        mSharedPreferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layoutAbout, toolbarAbout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layoutAbout.addDrawerListener(toggle)
        toggle.syncState()

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
                intent.putExtra("type", "table")
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }

            R.id.nav_results -> {
                val intent = Intent(this, TableActivity::class.java)
                intent.putExtra("type", "results")
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

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

}
