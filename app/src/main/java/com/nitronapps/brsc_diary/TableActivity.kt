package com.nitronapps.brsc_diary

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mklimek.sslutilsandroid.SslUtils
import com.nitronapps.brsc_diary.Adapters.ResultsAdapter
import com.nitronapps.brsc_diary.Adapters.TableAdapter
import com.nitronapps.brsc_diary.Data.*
import com.nitronapps.brsc_diary.Database.AppDatabase
import com.nitronapps.brsc_diary.Database.UserDB
import com.nitronapps.brsc_diary.Models.*
import com.nitronapps.brsc_diary.Others.CustomTypefaceSpan
import com.nitronapps.brsc_diary.Others.IBRSC
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.activity_table.*
import kotlinx.android.synthetic.main.activity_table.view.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_table.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_result.*
import kotlinx.android.synthetic.main.content_table.*
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

class TableActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mServerAPI: IBRSC
    private lateinit var mOkHttpClient: OkHttpClient
    private var user: NameModel? = null
    private var prefId = 0
    private val arrayListType: Type = object : TypeToken<ArrayList<String>>() {}.type
    private val arrayDepartmentsType: Type = object : TypeToken<Array<Departments>>() {}.type
    private val callListDepartments = ArrayList<Call<Array<Departments>>>()
    private val callListTable = ArrayList<Call<Array<TableModel>>>()
    private var login = ""
    private var password = ""
    private lateinit var ids: ArrayList<String>
    private var isParent = false
    private var count = 0
    private lateinit var appDatabase: AppDatabase
    private lateinit var tmpUser: UserDB
    private lateinit var tmpUserInfo: Array<UserInfoModel>
    private lateinit var deviceId: String
    private var departments: Array<Departments>? = null
    private lateinit var prefDepartment: Array<Departments>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)
        setSupportActionBar(toolbar)

        swipeRefreshLayoutTable.setColorSchemeColors(Color.parseColor("#2980b9"), Color.parseColor("#e74c3c"), Color.parseColor("#f1c40f"), Color.parseColor("#2ecc71"))
        swipeRefreshLayoutTable.isRefreshing = true
        swipeRefreshLayoutTable.setDistanceToTriggerSync(500)

        val cert = SslUtils.getSslContextForCertificateFile(this, "certificate.crt")

        val okhttp = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
                .sslSocketFactory(cert.socketFactory)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(SERVER_ADRESS)
                .client(okhttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        mServerAPI = retrofit.create(IBRSC::class.java)



        mSharedPreferences = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)

        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)
        appDatabase = Room.databaseBuilder(this, AppDatabase::class.java, DATABASE_NAME).allowMainThreadQueries().build()

        isParent = mSharedPreferences.getBoolean("isParent", false)
        prefId = mSharedPreferences.getInt("prefId", 0)

        tmpUser = appDatabase.userDao().getUserById(0)

        tmpUserInfo = Gson().fromJson<Array<UserInfoModel>>(
                tmpUser.uid.decrypt(deviceId),
                object : TypeToken<Array<UserInfoModel>>() {}.type
        )
        prefDepartment = Gson().fromJson<Array<Departments>>(
                tmpUser.prefDepartment.decrypt(deviceId),
                arrayDepartmentsType
        )
        login = tmpUser.login.decrypt(deviceId) /*mSharedPreferences.getString("login", "")!!*/
        password = tmpUser.password.decrypt(deviceId) /*mSharedPreferences.getString("password", "")!!*/
        ids = tmpUserInfo.getIds()

        if (!mSharedPreferences.getBoolean("wasDepartments", false))
            initDepartments()
        else
            departments = Gson().fromJson<Array<Departments>>(
                    tmpUser.dids.decrypt(deviceId),
                    arrayDepartmentsType
            )




        count = mSharedPreferences.getInt("count", 0)

        if (isParent) {
            navigation_viewTable.menu.clear()
            navigation_viewTable.inflateMenu(R.menu.menu_parent)
            navigation_viewTable.textViewParentNameTable.visibility = View.VISIBLE
        } else {
            navigation_viewTable.menu.clear()
            navigation_viewTable.inflateMenu(R.menu.menu_student)
            navigation_viewTable.textViewParentNameTable.visibility = View.GONE
        }


        recyclerViewTable.layoutManager = LinearLayoutManager(this)

        initRecyclerView()

        setPerson()

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout_table, findViewById(R.id.toolbarTable), R.string.title_activity_login, R.string.title_activity_main2)
        drawer_layout_table.addDrawerListener(toggle)
        toggle.syncState()

        navigation_viewTable.setNavigationItemSelectedListener(this)

        val menu = navigation_viewTable.menu

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

            R.id.nav_table -> drawer_layout_table.closeDrawers()

            if (isParent) R.id.nav_children else -1 -> {
                if (user != null) {
                    AlertDialog.Builder(this)
                            .setTitle(resources.getString(R.string.changeUser))
                            .setItems(user!!.child_ids!!.toTypedArray(), { dialog, which ->
                                val name = Gson().fromJson(
                                        tmpUser.name.decrypt(deviceId),
                                        NameModel::class.java
                                )
                                drawer_layout_table.closeDrawers()
                                prefId = which
                                mSharedPreferences.edit().putInt("prefId", which).apply()
                                setPersonName(name?.child_ids!![prefId])
                                recyclerViewTable.adapter = null
                                initRecyclerView()
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
                                    recyclerViewTable.adapter = null
                                    initRecyclerView()
                                }
                                drawer_layout_table.closeDrawers()
                            }.show()
                }
            }
            else -> {
            }
        }

        return true
    }

    fun setPerson() {
        runOnUiThread {
            navigation_viewTable.removeHeaderView(navigation_viewTable.getHeaderView(0))
            val header = navigation_viewTable.inflateHeaderView(R.layout.nav_header)

            val textViewName = header.textViewName

            val parentName = navigation_viewTable.textViewParentNameTable

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

    fun initRecyclerView() {

        var curId = ""
        if (isParent)
            curId = ids[prefId]
        else
            curId = ids[0]


        val curPrefId = prefId
        swipeRefreshLayoutTable.isRefreshing = true


        callListTable.add(mServerAPI.getTable(login,
                password,
                curId,
                tmpUserInfo[prefId].rooId,
                prefDepartment[prefId].departmentId,
                tmpUserInfo[prefId].instituteId))
        callListTable.last().enqueue(
                object : Callback<Array<TableModel>> {
                    override fun onFailure(call: Call<Array<TableModel>>, t: Throwable) {
                        Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(call: Call<Array<TableModel>>, response: Response<Array<TableModel>>) {
                        swipeRefreshLayoutResult?.isRefreshing = false

                        if (response.body() != null) {
                            if (curPrefId == prefId) {
                                val list = LinkedList<Lesson>()

                                for (i in response.body()!!.iterator())
                                    list.add(i.getLesson())

                                list.add(Lesson("", LinkedList<TableMarks>()))

                                val adapter = TableAdapter(list)
                                recyclerViewTable.adapter = adapter
                                recyclerViewTable.addItemDecoration(MainActivity.SpacesItemDecoration(5))
                            } else
                                swipeRefreshLayoutTable.isRefreshing = true

                        } else
                            Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_LONG).show()

                        swipeRefreshLayoutTable.isRefreshing = false
                    }
                })


        swipeRefreshLayoutTable.setOnRefreshListener {

            if (isParent)
                curId = ids[prefId]
            else
                curId = ids[0]

            callListTable.add(mServerAPI.getTable(
                    login,
                    password,
                    curId,
                    tmpUserInfo[prefId].rooId,
                    tmpUserInfo[prefId].departmentId,
                    tmpUserInfo[prefId].instituteId
            ))
            callListTable.last().enqueue(
                    object : Callback<Array<TableModel>> {
                        override fun onFailure(call: Call<Array<TableModel>>, t: Throwable) {
                            Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_LONG).show()
                        }

                        override fun onResponse(call: Call<Array<TableModel>>, response: Response<Array<TableModel>>) {
                            if (response.body() != null) {
                                val list = LinkedList<Lesson>()

                                for (i in response.body()!!.iterator())
                                    list.add(i.getLesson())

                                val adapter = TableAdapter(list)
                                recyclerViewTable.adapter = adapter
                                recyclerViewTable.addItemDecoration(MainActivity.SpacesItemDecoration(5))
                                Log.w("table", "success")

                                swipeRefreshLayoutTable.isRefreshing = false
                            } else
                                Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_LONG).show()

                            swipeRefreshLayoutTable.isRefreshing = false
                        }
                    }
            )
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

    fun deleteAccount() {
        mSharedPreferences.edit().clear().apply()
        appDatabase.userDao().deleteAll(tmpUser)
        recyclerViewTable.adapter = null

        for (i in callListTable.iterator())
            if (!i.isCanceled && i.isExecuted)
                i.cancel()

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
        navigation_viewTable.removeHeaderView(navigation_viewTable.getHeaderView(0))
        val header = navigation_viewTable.inflateHeaderView(R.layout.nav_header)

        header.textViewName.text = name.replace("\"", "")
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

    fun Array<UserInfoModel>.getIds(): ArrayList<String> {
        val result = ArrayList<String>()

        for (i in this)
            result.add(i.userId)

        return result
    }

    private fun Array<Departments>.getYears(): ArrayList<String> {
        val result = ArrayList<String>()

        for (i in this)
            result.add(i.name)

        return result
    }


}
