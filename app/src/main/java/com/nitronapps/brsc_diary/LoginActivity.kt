package com.nitronapps.brsc_diary

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nitronapps.brsc_diary.Data.APP_VERSION
import com.nitronapps.brsc_diary.Data.SERVER_ADRESS
import com.nitronapps.brsc_diary.Models.UserModel
import com.nitronapps.brsc_diary.Others.IBRSC

import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class LoginActivity : AppCompatActivity() {
    val APP_SETTINGS = "account"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val mSharedPreferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)

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

        buttonLogIn.setOnClickListener {
            progressBarLogIn.visibility = View.VISIBLE
            buttonLogIn.visibility = View.INVISIBLE

            if (getCurrentFocus() != null)
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
            
            
            serverApi.getId(editTextLogin.text.toString(), editTextPassword.text.toString(), APP_VERSION).enqueue(
                    object : Callback<UserModel> {
                        override fun onFailure(call: Call<UserModel>, t: Throwable) {
                            Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                            Log.w("login", t.message)
                            progressBarLogIn.visibility = View.INVISIBLE
                            buttonLogIn.visibility = View.VISIBLE
                        }

                        override fun onResponse(call: Call<UserModel>, response: retrofit2.Response<UserModel>) {
                            if (response.body() != null) {

                                    if (response.body()!!.id == null) {
                                        mSharedPreferences.edit().putString("ids", Gson().toJson(response.body()!!.child_ids!!)).apply()
                                        mSharedPreferences.edit().putString("parentId", response.body()!!.parent_id.toString()).apply()
                                        mSharedPreferences.edit().putBoolean("isParent", true).apply()
                                        mSharedPreferences.edit().putInt("prefId", 0).apply()
                                    } else {
                                        val arrayListIds = ArrayList<String>()
                                        arrayListIds.add(response.body()!!.id.toString())
                                        mSharedPreferences.edit().putString("ids", Gson().toJson(arrayListIds)).apply()
                                        mSharedPreferences.edit().putBoolean("isParent", false).apply()
                                    }
                                    mSharedPreferences.edit().putBoolean("wasLogin", true).apply()

                                    mSharedPreferences.edit().putString("login", editTextLogin.text.toString()).apply()
                                    mSharedPreferences.edit().putString("password", editTextPassword.text.toString()).apply()
                                    startActivity(Intent(applicationContext, MainActivity::class.java))
                                } else
                                    Toast.makeText(applicationContext, resources.getString(R.string.error_login), Toast.LENGTH_SHORT).show()


                            progressBarLogIn.visibility = View.INVISIBLE
                            buttonLogIn.visibility = View.VISIBLE
                        }
                    }
            )
        }
    }

    override fun onBackPressed() {}

}



