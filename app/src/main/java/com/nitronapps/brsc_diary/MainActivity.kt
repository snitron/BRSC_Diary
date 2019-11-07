package com.nitronapps.brsc_diary


import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nitronapps.brsc_diary.Adapters.DayAdapter
import com.nitronapps.brsc_diary.Database.AppDatabase
import com.nitronapps.brsc_diary.Models.DayModel
import com.nitronapps.brsc_diary.Models.NameModel
import com.nitronapps.brsc_diary.Others.CustomTypefaceSpan
import com.nitronapps.brsc_diary.Others.IBRSC
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header.view.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import androidx.recyclerview.widget.RecyclerView
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.mklimek.sslutilsandroid.SslUtils
import com.nitronapps.brsc_diary.Data.*
import com.nitronapps.brsc_diary.Database.UserDB
import com.nitronapps.brsc_diary.Models.UserInfoModel


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var appDatabase: AppDatabase
    private lateinit var mServerAPI: IBRSC
    private lateinit var mOkHttpClient: OkHttpClient
    private var user: NameModel? = null
    private var prefId = 0
    private val arrayListType: Type = object : TypeToken<ArrayList<String>>() {}.type
    private val arrayDepartmentsType: Type = object : TypeToken<Array<Departments>>() {}.type
    private val callListDepartments = ArrayList<Call<Array<Departments>>>()
    private val callListDiary = ArrayList<Call<Array<DayModel>>>()
    private var login = ""
    private var password = ""
    private lateinit var ids: ArrayList<String>
    private var isParent = false
    private lateinit var deviceId: String
    private lateinit var tmpUser: UserDB
    private lateinit var tmpUserInfo: Array<UserInfoModel>
    private var departments: Array<Departments>? = null
    private lateinit var prefDepartment: Array<Departments>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (intent.getStringExtra("message") != null)
                AlertDialog.Builder(this)
                        .setTitle(resources.getString(R.string.notification))
                        .setMessage(intent.getStringExtra("message"))
                        .create()
                        .show()

        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)

        appDatabase = Room.databaseBuilder(this, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration().allowMainThreadQueries().build()

        mSharedPreferences = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)


        val cert = SslUtils.getSslContextForCertificateFile(this, "certificate.crt")

        mOkHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
                        .sslSocketFactory(cert.socketFactory)
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


        if (!mSharedPreferences.contains("wasLogin") || !mSharedPreferences.getBoolean("wasLogin", false)
                || !mSharedPreferences.contains("version") || !mSharedPreferences.getString("version", "").equals(APP_VERSION) ||
                appDatabase.userDao().getDataCount() == 0
        ) {
            mSharedPreferences.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("type", "first")
            startActivity(intent)
        } else {
            tmpUser = appDatabase.userDao().getUserById(0)

            login = tmpUser.login.decrypt(deviceId) /*mSharedPreferences.getString("login", "")!!*/
            password = tmpUser.password.decrypt(deviceId) /*mSharedPreferences.getString("password", "")!!*/

            tmpUserInfo = Gson().fromJson<Array<UserInfoModel>>(
                    tmpUser.uid.decrypt(deviceId),
                    object : TypeToken<Array<UserInfoModel>>() {}.type
            )

            ids = tmpUserInfo.getIds()

            prefDepartment = Gson().fromJson<Array<Departments>>(
                    tmpUser.prefDepartment.decrypt(deviceId),
                    arrayDepartmentsType
            )

            if (!mSharedPreferences.getBoolean("wasDepartments", false))
                initDepartments()
            else
                departments = Gson().fromJson<Array<Departments>>(
                        tmpUser.dids.decrypt(deviceId),
                        arrayDepartmentsType
                )

        }

        if (mSharedPreferences.contains("saved")) {
            val day = Gson().fromJson(mSharedPreferences.getString("saved", "[]"), Array<DayModel>::class.java)
            recyclerView.adapter = DayAdapter(day, applicationContext)
            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        }

        mSharedPreferences.edit().putInt("curWeek", GregorianCalendar().get(GregorianCalendar.WEEK_OF_YEAR)).apply()

        isParent = mSharedPreferences.getBoolean("isParent", false)
        prefId = mSharedPreferences.getInt("prefId", 0)

        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#2980b9"), Color.parseColor("#e74c3c"), Color.parseColor("#f1c40f"), Color.parseColor("#2ecc71"))
        swipeRefreshLayout.isRefreshing = true
        swipeRefreshLayout.setDistanceToTriggerSync(500)

        if (isParent) {
            navigation_view.menu.clear()
            navigation_view.inflateMenu(R.menu.menu_parent)
            navigation_view.textViewParentNameMain.visibility = View.VISIBLE
        } else {
            navigation_view.menu.clear()
            navigation_view.inflateMenu(R.menu.menu_student)
            navigation_view.textViewParentNameMain.visibility = View.GONE
        }


        swipeRefreshLayout.setOnRefreshListener {
            initRecyclerView()
        }

        recyclerView.addItemDecoration(SpacesItemDecoration(10))

        if (mSharedPreferences.contains("wasLogin") || mSharedPreferences.getBoolean("wasLogin", false)) {
            initRecyclerView()

            setPerson()
        }
        buttonNext.setOnClickListener {
            var curId = ""
            if (isParent)
                curId = ids[prefId]
            else
                curId = ids[0]

            swipeRefreshLayout.isRefreshing = true

            callListDiary.add(mServerAPI.getDiary(
                    login,
                    password,
                    if ((mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() in 35.0..52.0 ||
                            (mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() in 1.0..22.0)
                        (mSharedPreferences.getInt("curWeek", 1) + 1).toString()
                    else
                        "1",
                    curId,
                    tmpUserInfo[prefId].rooId,
                    prefDepartment[prefId].departmentId,
                    tmpUserInfo[prefId].instituteId,
                    prefDepartment[prefId].getRightYear()
            ))

            callListDiary.last().enqueue(object : retrofit2.Callback<Array<DayModel>> {
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
                        if ((mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() in 35.0..52.0 ||
                                (mSharedPreferences.getInt("curWeek", 1) + 1).toDouble() in 1.0..22.0)
                            mSharedPreferences.edit().putInt("curWeek", mSharedPreferences.getInt("curWeek", 0) + 1).apply()
                    } else
                        Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_SHORT).show()

                }

            })

        }

        buttonPrev.setOnClickListener {
            var curId = ""
            if (isParent)
                curId = ids[prefId]
            else
                curId = ids[0]

            swipeRefreshLayout.isRefreshing = true

            callListDiary.add(mServerAPI.getDiary(
                    login,
                    password,
                    if ((mSharedPreferences.getInt("curWeek", 1) - 1).toDouble() in 35.0..52.0 ||
                            (mSharedPreferences.getInt("curWeek", 1) - 1).toDouble() in 1.0..22.0)
                        (mSharedPreferences.getInt("curWeek", 1) - 1).toString()
                    else
                        "1",
                    curId,
                    tmpUserInfo[prefId].rooId,
                    prefDepartment[prefId].departmentId,
                    tmpUserInfo[prefId].instituteId,
                    prefDepartment[prefId].getRightYear()
            ))

            callListDiary.last().enqueue(object : retrofit2.Callback<Array<DayModel>> {
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

                        if ((mSharedPreferences.getInt("curWeek", 1) - 1).toDouble() in 35.0..52.0 ||
                                (mSharedPreferences.getInt("curWeek", 1) - 1).toDouble() in 1.0..22.0)
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

            if (isParent) R.id.nav_children else -1 -> {
                if (user != null) {
                    AlertDialog.Builder(this)
                            .setTitle(resources.getString(R.string.changeUser))
                            .setItems(user!!.child_ids!!.toTypedArray(), { dialog, which ->
                                val name = Gson().fromJson(
                                        tmpUser.name.decrypt(deviceId),
                                        NameModel::class.java
                                )
                                drawer_layout.closeDrawers()
                                prefId = which
                                mSharedPreferences.edit().putInt("prefId", which).apply()
                                setPersonName(name?.child_ids!![prefId])

                                recyclerView.adapter = null
                                departments = null
                                initDepartments()
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
                                    initRecyclerView()
                                }
                                drawer_layout.closeDrawers()
                            }.show()
                }
            }

            R.id.nav_balance -> {
                startActivity(Intent(this, InformationActivity::class.java))
            }

            else -> {
            }
        }
        return true
    }


    fun setPersonName(name: String) {
        navigation_view.removeHeaderView(navigation_view.getHeaderView(0))
        val header = navigation_view.inflateHeaderView(R.layout.nav_header)

        header.textViewName.text = name.replace("\"", "")
    }

    fun setPerson() {
        runOnUiThread {
            navigation_view.removeHeaderView(navigation_view.getHeaderView(0))
            val header = navigation_view.inflateHeaderView(R.layout.nav_header)

            val textViewName = header.textViewName
            val parentName = navigation_view.textViewParentNameMain



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

    fun applyFontToMenuItem(mi: MenuItem) {
        val font = Typeface.createFromAsset(assets, "segoe_ui_light.ttf")
        val mNewName = SpannableString(mi.title)
        mNewName.setSpan(CustomTypefaceSpan("", font), 0, mNewName.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        mi.title = mNewName
    }

    fun deleteAccount() {
        mSharedPreferences.edit().clear().apply()
        appDatabase.userDao().deleteAll(tmpUser)
        recyclerView.adapter = null

        for (i in callListDiary.iterator())
            if (!i.isCanceled && i.isExecuted)
                i.cancel()

        for (i in callListDepartments.iterator())
            if (!i.isCanceled && i.isExecuted)
                i.cancel()

        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("type", "first")

        startActivity(intent)
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

    fun initRecyclerView() {
        var curId = ""
        if (isParent)
            curId = ids[prefId]
        else
            curId = ids[0]

        val curPrefId = prefId

        swipeRefreshLayout.isRefreshing = true

        callListDiary.add(
                mServerAPI.getDiary(
                        login,
                        password,
                        mSharedPreferences.getInt("curWeek", 1).toString(),
                        curId,
                        tmpUserInfo[prefId].rooId,
                        prefDepartment[prefId].departmentId,
                        tmpUserInfo[prefId].instituteId,
                        prefDepartment[prefId].getRightYear()
                ))
        callListDiary.last().enqueue(object : retrofit2.Callback<Array<DayModel>> {
            override fun onFailure(call: Call<Array<DayModel>>, t: Throwable) {
                Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
                Log.w("mainError", t.message)
            }

            override fun onResponse(call: Call<Array<DayModel>>, response: Response<Array<DayModel>>) {
                runOnUiThread {
                    swipeRefreshLayout.isRefreshing = false
                }
                Log.w("mainSucces", Gson().toJson(response.body()))
                if (response.body() != null) {
                    if (curPrefId == prefId) {
                        recyclerView.adapter = DayAdapter(response.body()!!, applicationContext)
                        recyclerView.layoutManager = LinearLayoutManager(applicationContext)

                        mSharedPreferences.edit().putString("saved", Gson().toJson(response.body())).apply()
                    } else
                        runOnUiThread {
                            swipeRefreshLayout.isRefreshing = true
                        }
                } else
                    Toast.makeText(applicationContext, resources.getString(R.string.error_connection), Toast.LENGTH_SHORT).show()
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

    override fun onBackPressed() {

    }

    class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

            outRect.bottom = space
        }
    }

    private fun String.decrypt(password: String): String {
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

    private fun String.encrypt(password: String): String {
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

    private fun Array<Departments>.getYears(): ArrayList<String> {
        val result = ArrayList<String>()

        for (i in this)
            result.add(i.name)

        return result
    }
}
