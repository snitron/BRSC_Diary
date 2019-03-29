package com.nitronapps.brsc_diary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.*
import androidx.core.view.GravityCompat
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nitronapps.brsc_diary.Data.*
import com.nitronapps.brsc_diary.Database.AppDatabase
import com.nitronapps.brsc_diary.Database.UserDB
import com.nitronapps.brsc_diary.Models.NameModel
import com.nitronapps.brsc_diary.Models.UserInfoModel
import com.nitronapps.brsc_diary.Others.CustomTypefaceSpan
import com.nitronapps.brsc_diary.Others.IBRSC
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_about.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_about.*
import kotlinx.android.synthetic.main.content_about.*
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
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AboutActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mServerAPI: IBRSC
    private var prefId = 0
    private val arrayDepartmentsType: Type = object : TypeToken<Array<Departments>>() {}.type
    private val callListDepartments = ArrayList<Call<Array<Departments>>>()
    private var user: NameModel? = null
    private var isParent = false
    private var login = ""
    private var password = ""
    private lateinit var deviceId: String
    private lateinit var tmpUser: UserDB
    private lateinit var appDatabase: AppDatabase
    private lateinit var tmpUserInfo: Array<UserInfoModel>
    private var departments: Array<Departments>? = null
    private lateinit var prefDepartment: Array<Departments>
    private lateinit var ids: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbarAbout)

        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)
        mSharedPreferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layoutAbout, toolbarAbout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layoutAbout.addDrawerListener(toggle)
        toggle.syncState()

        appDatabase = Room.databaseBuilder(this, AppDatabase::class.java, DATABASE_NAME).allowMainThreadQueries().build()


        isParent = mSharedPreferences.getBoolean("isParent", false)
        prefId = mSharedPreferences.getInt("prefId", 0)

        tmpUser = appDatabase.userDao().getUserById(0)

        tmpUserInfo = Gson().fromJson<Array<UserInfoModel>>(
                tmpUser.uid.decrypt(deviceId),
                object : TypeToken<Array<UserInfoModel>>() {}.type
        )

        login = tmpUser.login.decrypt(deviceId) /*mSharedPreferences.getString("login", "")!!*/
        password = tmpUser.password.decrypt(deviceId) /*mSharedPreferences.getString("password", "")!!*/

        ids = tmpUserInfo.getIds()

        prefDepartment = Gson().fromJson<Array<Departments>>(
                tmpUser.prefDepartment.decrypt(deviceId),
                arrayDepartmentsType
        )

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

        if (!mSharedPreferences.getBoolean("wasDepartments", false))
            initDepartments()
        else
            departments = Gson().fromJson<Array<Departments>>(
                    tmpUser.dids.decrypt(deviceId),
                    arrayDepartmentsType
            )

        if (isParent) {
            nav_view.menu.clear()
            nav_view.inflateMenu(R.menu.menu_parent)
            nav_view.textViewParentNameAbout.visibility = View.VISIBLE
        } else {
            nav_view.menu.clear()
            nav_view.inflateMenu(R.menu.menu_student)
            nav_view.textViewParentNameAbout.visibility = View.GONE
        }

        setPerson()

        nav_view.setNavigationItemSelectedListener(this)
        val menu = nav_view.menu

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)

            textViewPrivacyPolicy.text = Html.fromHtml("<a href=\"" + POLICY_ADRESS + "\">" + resources.getString(R.string.title_privacy_policy) + "</a>",Html.FROM_HTML_MODE_COMPACT)

        else
            textViewPrivacyPolicy.text = Html.fromHtml("<a href=\"" + POLICY_ADRESS + "\">" + resources.getString(R.string.title_privacy_policy) + "</a>")

        textViewPrivacyPolicy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(POLICY_ADRESS)))
        }

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
                            .setTitle(resources.getString(R.string.changeUser))
                            .setItems(user!!.child_ids!!.toTypedArray(), { dialog, which ->
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

            R.id.nav_setyear -> {
                if (departments == null) {
                    AlertDialog.Builder(this)
                            .setTitle(resources.getString(R.string.check_year))
                            .setMessage(resources.getString(R.string.loading))
                            .create()
                            .show()
                } else {
                    AlertDialog.Builder(this)
                            .setTitle(resources.getString(R.string.check_year))
                            .setItems(departments!!.getYears().toTypedArray()
                            ) { dialog, which ->
                                if (!prefDepartment.equals(departments!![which])) {
                                    prefDepartment[prefId] = departments!![which]
                                    appDatabase.userDao().setPrefDepartments(0, Gson().toJson(prefDepartment).encrypt(deviceId))
                                }
                            }.show()
                }
            }
            else -> {
            }
        }

        drawer_layoutAbout.closeDrawer(GravityCompat.START)
        return true
    }

    fun deleteAccount() {
        mSharedPreferences.edit().clear().apply()
        appDatabase.userDao().deleteAll(tmpUser)

        for (i in callListDepartments.iterator())
            if (!i.isCanceled && i.isExecuted)
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

    fun setPerson() {
        runOnUiThread {
            nav_view.removeHeaderView(nav_view.getHeaderView(0))
            val header = nav_view.inflateHeaderView(R.layout.nav_header)

            val textViewName = header.textViewName
            val parentName = nav_view.textViewParentNameAbout

            user = Gson().fromJson(
                    tmpUser.name.decrypt(deviceId),
                    NameModel::class.java
            )

            if (isParent) {
                textViewName.text = user?.child_ids!![prefId].replace("\"", "")

                parentName.text = resources.getString(R.string.parent_name) + " " + prepareParentName(user?.name!!)
            } else {
                textViewName.text = user?.name!!.replace("\"", "")
                parentName.visibility = View.GONE
            }
        }
    }
    fun initDepartments() {
        var curId = ""
        if (isParent)
            curId = ids[prefId]
        else
            curId = ids[0]

        callListDepartments.add(
                mServerAPI.getDiaryYears(
                        login,
                        password,
                        curId,
                        tmpUserInfo[prefId].rooId,
                        prefDepartment[prefId].departmentId,
                        tmpUserInfo[prefId].instituteId))

        callListDepartments.last().enqueue(object : retrofit2.Callback<Array<Departments>> {
            override fun onFailure(call: Call<Array<Departments>>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(applicationContext, resources.getString(R.string.error_load_departments), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call<Array<Departments>>, response: Response<Array<Departments>>) {
                Log.w("departmentsSuccess", Gson().toJson(response.body()))
                if (response.body() != null && response.body()!!.isNotEmpty()) {

                    departments = response.body()!!
                    appDatabase.userDao().setDepartments(0, Gson().toJson(departments).encrypt(deviceId))
                    mSharedPreferences.edit().putBoolean("wasDepartments", true).apply()
                }
            }

        })
    }
    fun prepareParentName(name: String): String {
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

    fun String.decrypt(password: String): String {
        val secretKeySpec = SecretKeySpec(password.toByteArray(), "AES")
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        for (i in 0 until charArray.size) {
            iv[i] = charArray[i].toByte()
        }
        val ivParameterSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

        val decryptedByteValue = cipher.doFinal(Base64.decode(this, Base64.DEFAULT))
        return String(decryptedByteValue)
    }

    private fun Array<Departments>.getYears(): ArrayList<String> {
        val result = ArrayList<String>()

        for (i in this)
            result.add(i.name)

        return result
    }

    fun String.encrypt(password: String): String {
        val secretKeySpec = SecretKeySpec(password.toByteArray(), "AES")
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        for (i in 0 until charArray.size) {
            iv[i] = charArray[i].toByte()
        }
        val ivParameterSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)

        val encryptedValue = cipher.doFinal(this.toByteArray())
        return Base64.encodeToString(encryptedValue, Base64.DEFAULT)
    }

    private fun Array<UserInfoModel>.getIds(): ArrayList<String> {
        val result = ArrayList<String>()

        for (i in this)
            result.add(i.userId)

        return result
    }
}

