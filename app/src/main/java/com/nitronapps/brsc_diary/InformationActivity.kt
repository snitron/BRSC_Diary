package com.nitronapps.brsc_diary

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.gson.Gson
import com.mklimek.sslutilsandroid.SslUtils
import com.nitronapps.brsc_diary.Adapters.BalanceAdapter
import com.nitronapps.brsc_diary.Adapters.DayAdapter
import com.nitronapps.brsc_diary.Adapters.InfoAdapter
import com.nitronapps.brsc_diary.Data.DATABASE_NAME
import com.nitronapps.brsc_diary.Data.SERVER_ADRESS
import com.nitronapps.brsc_diary.Database.AppDatabase
import com.nitronapps.brsc_diary.Models.DayModel
import com.nitronapps.brsc_diary.Models.NameModel
import com.nitronapps.brsc_diary.Others.BalanceCall
import com.nitronapps.brsc_diary.Others.IBRSC
import kotlinx.android.synthetic.main.activity_information.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.item_info.*
import kotlinx.android.synthetic.main.item_info.textViewInfoName
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import android.widget.Toast.makeText as makeText1

class InformationActivity : AppCompatActivity() {
    val APP_SETTINGS = "account"
    lateinit var appDatabase: AppDatabase
    lateinit var progressBarInfo: ProgressBar
    lateinit var progressBarBalance: ProgressBar
    lateinit var recyclerViewInfo: RecyclerView
    lateinit var recyclerViewBalance: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)
        setSupportActionBar(toolbarInfo)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        appDatabase = Room.databaseBuilder(this, AppDatabase::class.java, DATABASE_NAME).fallbackToDestructiveMigration().allowMainThreadQueries().build()
        val mSharedPreferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)

        val isParent = mSharedPreferences.getBoolean("isParent", false)
        val prefId = mSharedPreferences.getInt("prefId", 0)

        val device_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)

        val tmpUser = appDatabase.userDao().getUserById(0)

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

        val serverApi = retrofit.create(IBRSC::class.java)

        val user = Gson().fromJson(
                tmpUser.name.decrypt(device_id),
                NameModel::class.java
        )

        textViewInfoName.text = if (isParent) user.child_ids!![prefId].replace("\"", "") else
            user.name!!.replace("\"", "")

        progressBarBalance = findViewById(R.id.progressBarBalance)
        progressBarInfo = findViewById(R.id.progressBarInfo)

        recyclerViewBalance = findViewById(R.id.recyclerViewBalance)
        recyclerViewInfo = findViewById(R.id.recyclerViewInfo)

        recyclerViewInfo.layoutManager = LinearLayoutManager(this)
        recyclerViewBalance.layoutManager = LinearLayoutManager(this)

        progressBarBalance.isEnabled = true
        progressBarInfo.isEnabled = true

        serverApi.getBalance(tmpUser.login.decrypt(device_id), tmpUser.password.decrypt(device_id)).enqueue(
                object : retrofit2.Callback<BalanceCall> {
                    override fun onResponse(call: Call<BalanceCall>, response: Response<BalanceCall>) {
                        runOnUiThread {
                            progressBarBalance.visibility = View.GONE
                            progressBarInfo.visibility = View.GONE

                            if(response.body() == null)
                                onBackPressed()

                            val result = response.body()!!

                            if(result.isChild){
                                recyclerViewBalance.adapter = BalanceAdapter(result.res)
                                recyclerViewInfo.adapter = InfoAdapter(result.info)
                            } else {
                                val name = user.child_ids!![prefId].replace("\"", "")
                                recyclerViewBalance.adapter = BalanceAdapter(ArrayList(result.res.filter { it.childName.equals(name) }))
                                recyclerViewInfo.adapter = InfoAdapter(result.info)
                            }

                        }

                    }

                    override fun onFailure(call: Call<BalanceCall>, t: Throwable) {
                        Toast.makeText(this@InformationActivity, "Ошибка соединения", Toast.LENGTH_LONG)
                    }
                }
        )

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
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
}
