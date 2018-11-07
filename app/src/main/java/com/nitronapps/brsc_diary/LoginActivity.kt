package com.nitronapps.brsc_diary

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.nitronapps.brsc_diary.Others.IBRSC

import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    val SERVER_ADRESS = "https://brsc-diary-server.herokuapp.com/web/"
    val APP_SETTINGS = "account"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val okhttp = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(SERVER_ADRESS)
                .client(okhttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val serverApi = retrofit.create(IBRSC::class.java)


        val sp = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)



        buttonLogIn.setOnClickListener {
            progressBarLogIn.visibility = View.VISIBLE
            buttonLogIn.visibility = View.INVISIBLE

            if (getCurrentFocus() != null)
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

            serverApi.getId(editTexlLogin.text.toString(), editTextPassword.text.toString()).enqueue(
                    object : Callback<String> {
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                            progressBarLogIn.visibility = View.INVISIBLE
                            buttonLogIn.visibility = View.VISIBLE
                        }

                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (!response.body().equals("") && response.body() != null) {
                                Log.w("login", response.body().toString())
                                sp.edit().putBoolean("wasLogin", true).apply()
                                sp.edit().putString("id", response.body()).apply()
                                sp.edit().putString("login", editTexlLogin.text.toString()).apply()
                                sp.edit().putString("password", editTextPassword.text.toString()).apply()

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

    override fun onBackPressed() {

    }

}



