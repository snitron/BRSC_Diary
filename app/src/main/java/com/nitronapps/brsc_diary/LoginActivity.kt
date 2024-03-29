package com.nitronapps.brsc_diary

import android.os.Bundle
import android.view.View

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.Html
import android.util.Base64
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.gson.Gson
import com.nitronapps.brsc_diary.Data.*
import com.nitronapps.brsc_diary.Database.AppDatabase
import com.nitronapps.brsc_diary.Database.UserDB
import com.nitronapps.brsc_diary.Models.NameModel
import com.nitronapps.brsc_diary.Models.UserInfoModel
import com.nitronapps.brsc_diary.Models.UserModel
import com.nitronapps.brsc_diary.Others.IBRSC

import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList


class LoginActivity : AppCompatActivity() {
    val APP_SETTINGS = "account"
    lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        appDatabase = Room.databaseBuilder(this, AppDatabase::class.java, DATABASE_NAME).fallbackToDestructiveMigration().allowMainThreadQueries().build()
        val mSharedPreferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
        val device_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)

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

        val serverApi = retrofit.create(IBRSC::class.java)

        textViewPrivacyPolicyCheckbox.text = Html.fromHtml(resources.getString(R.string.agree_privacy_policy))
        textViewUserDataCheckbox.text = Html.fromHtml(resources.getString(R.string.agree_data_policy))

        textViewPrivacyPolicyCheckbox.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(POLICY_ADRESS)))
        }
        textViewUserDataCheckbox.setOnClickListener { it ->
            AlertDialog.Builder(it.context)
                    .setTitle(resources.getString(R.string.using_data))
                    .setMessage(resources.getString(R.string.using_data_description))
                    .create()
                    .show()
        }


        buttonLogIn.setOnClickListener { it ->
            if (checkBoxPrivatePolicy.isChecked && checkBoxUserData.isChecked) {
                progressBarLogIn.visibility = View.VISIBLE
                buttonLogIn.visibility = View.INVISIBLE

                if (getCurrentFocus() != null)
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)


                serverApi.getId(editTextLogin.text.toString(), editTextPassword.text.toString()).enqueue(
                        object : Callback<UserModel> {
                            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                                Toast.makeText(applicationContext, resources.getString(R.string.error_login), Toast.LENGTH_SHORT).show()
                                Log.w("login", t.message)
                                progressBarLogIn.visibility = View.INVISIBLE
                                buttonLogIn.visibility = View.VISIBLE
                            }

                            override fun onResponse(call: Call<UserModel>, response: retrofit2.Response<UserModel>) {
                                try {
                                    if (response.body() != null) {
                                        var ids = ""
                                        val name: NameModel
                                        if (response.body()!!.isParent) {
                                            ids = Gson().toJson(response.body()!!.childIds)
                                            mSharedPreferences.edit().putBoolean("isParent", true).apply()
                                            mSharedPreferences.edit().putInt("prefId", 0).apply()
                                            val childNames = ArrayList<String>()

                                            for (i in response.body()!!.childIds)
                                                childNames.add(i.userName)

                                            name = NameModel(childNames, response.body()!!.parentName)

                                        } else {
                                            val arrayListIds = ArrayList<UserInfoModel>()
                                            arrayListIds.add(response.body()!!.childIds[0])
                                            ids = Gson().toJson(arrayListIds)
                                            mSharedPreferences.edit().putBoolean("isParent", false).apply()
                                            name = NameModel(null, response.body()!!.childIds[0].userName)
                                        }
                                        mSharedPreferences.edit().putBoolean("wasLogin", true).apply()
                                        mSharedPreferences.edit().putString("version", APP_VERSION).apply()
                                        mSharedPreferences.edit().putBoolean("wasDepartment", false).apply()
                                        /*   mSharedPreferences.edit().putString("login", editTextLogin.text.toString()).apply()
                                       mSharedPreferences.edit().putString("password", editTextPassword.text.toString()).apply()*/
                                        appDatabase.userDao().insertAll(UserDB(
                                                0,
                                                editTextLogin.text.toString().encrypt(device_id),
                                                editTextPassword.text.toString().encrypt(device_id),
                                                ids.encrypt(device_id),
                                                "".encrypt(device_id),
                                                Gson().toJson(name).encrypt(device_id),
                                                Gson().toJson(makeDepartments(response.body()!!)).encrypt(device_id)
                                        ))

                                        startActivity(Intent(applicationContext, MainActivity::class.java))
                                    } else
                                        Toast.makeText(applicationContext, resources.getString(R.string.error_login), Toast.LENGTH_SHORT).show()
                                    progressBarLogIn.visibility = View.INVISIBLE
                                    buttonLogIn.visibility = View.VISIBLE
                                    //   TODO("Operator 'in' is deprecated. Change it.")
                                } catch (e: Exception) {
                                    Toast.makeText(it.context, resources.getString(R.string.error_login), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                )
            } else
                Toast.makeText(it.context, resources.getString(R.string.continue_accept), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {}

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

    fun makeDepartments(users: UserModel): Array<Departments> {
        val result = ArrayList<Departments>()

        for (i in users.childIds)
            result.add(Departments(
                    "По умолчанию",
                    i.departmentId,
                    yearGetter(0).toString(),
                    yearGetter(1).toString()
            ))
        return result.toTypedArray();
    }

    fun yearGetter(FLAG: Int): Int {
        //FLAG 0: start
        //FLAG 1: end

        when (FLAG) {
            0 -> {
                if (GregorianCalendar.getInstance().get(GregorianCalendar.WEEK_OF_YEAR) in 1.0..22.0)
                    return GregorianCalendar.getInstance().get(GregorianCalendar.YEAR) - 1
                else if (GregorianCalendar.getInstance().get(GregorianCalendar.WEEK_OF_YEAR) in 35.0..52.0)
                    return GregorianCalendar.getInstance().get(GregorianCalendar.YEAR)
                else
                    return GregorianCalendar.getInstance().get(GregorianCalendar.YEAR)
            }

            1 -> {
                if (GregorianCalendar.getInstance().get(GregorianCalendar.WEEK_OF_YEAR) in 1.0..22.0)
                    return GregorianCalendar.getInstance().get(GregorianCalendar.YEAR)
                else if (GregorianCalendar.getInstance().get(GregorianCalendar.WEEK_OF_YEAR) in 35.0..52.0)
                    return GregorianCalendar.getInstance().get(GregorianCalendar.YEAR) + 1
                else
                    return GregorianCalendar.getInstance().get(GregorianCalendar.YEAR) + 1
            }
        }

        return GregorianCalendar.getInstance().get(GregorianCalendar.YEAR)
    }
}



