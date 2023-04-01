package com.example.galleonpepsi.domain.reposotories

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.galleonpepsi.R
import com.example.galleonpepsi.core.MyApplication.Companion.APP_CONTEXT
import com.example.galleonpepsi.core.utils.CustomUtility
import com.example.galleonpepsi.core.utils.StaticTags
import com.example.galleonpepsi.data.api.LoginApiInterface
import com.example.galleonpepsi.data.login.LoginResponseBody
import com.example.galleonpepsi.data.login.SessionData
import com.example.galleonpepsi.ui.posmactivity.responses.outletlistapiresponse.OutletListResponseBody
import com.google.android.gms.common.api.ApiException
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRepository(private val application: Application) {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(StaticTags.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val loginApi: LoginApiInterface = retrofit.create(LoginApiInterface::class.java)


    private val sharedPreferences = application.getSharedPreferences("user", Context.MODE_PRIVATE)
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("logged_in", false)
    }

    suspend fun login(username: String, password: String): SessionData? {

        try {
            val response = loginApi.login(username, password)
        }
        catch (e: Exception)
        {
            Log.d("afadss",e.message.toString())
        }

       /* if (response.success) {
            // Store the session data locally
            val sessionData = response.sessionData

            saveUserSession(sessionData)

            return sessionData
        } else {
            sharedPreferences.edit().putBoolean("logged_in", false).apply()
            CustomUtility.showError(application,response.message,"Failed")
        }*/
        return null
    }

    fun saveUserSession(sessionData: SessionData) {
        val sharedPref = application.getSharedPreferences("user", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            with(sharedPref.edit()) {
                putString(KEY_USER_ID, sessionData.user_id)
                putString(KEY_USER_NAME, sessionData.user_name)
                putString(KEY_FULL_NAME, sessionData.full_name)
                putString(KEY_USER_TYPE_ID, sessionData.user_type_id)
                putString(KEY_PICTURE_NAME, sessionData.picture_name)
                putString(KEY_USER_TYPE_NAME, sessionData.user_type_name)
                putString(KEY_DISTRICT_ID, (sessionData.district_id))
                putString(KEY_DISTRICT_NAME, sessionData.district_name)
                putString(KEY_ZONE_ID, sessionData.zone_id)
                putString(KEY_ZONE_NAME, sessionData.zone_name)
                putString(KEY_EMPLOYEE_ID, sessionData.employee_id)
                putString(KEY_EMPLOYEE_TYPE_ID, sessionData.employee_type_id)
                putString(KEY_EMPLOYEE_POSITION_ID, sessionData.employee_position_id)
                putBoolean("logged_in", true)
                apply()
            }
        }
    }

    fun getUserSession(): SessionData? {
        val sharedPref = application.getSharedPreferences("user", Context.MODE_PRIVATE)
        val userId = sharedPref?.getString(KEY_USER_ID, null)
        val userName = sharedPref?.getString(KEY_USER_NAME, null)
        val fullName = sharedPref?.getString(KEY_FULL_NAME, null)
        val userTypeId = sharedPref.getString(KEY_USER_TYPE_ID, null)
        val pictureName = sharedPref.getString(KEY_PICTURE_NAME, null)
        val userTypeName = sharedPref.getString(KEY_USER_TYPE_NAME, null)
        val districtId = sharedPref.getString(KEY_DISTRICT_ID, null)
        val districtName = sharedPref.getString(KEY_DISTRICT_NAME, null)
        val zoneId = sharedPref.getString(KEY_ZONE_ID, "null")
        val zoneName = sharedPref.getString(KEY_ZONE_NAME, "null")
        val employeeId = sharedPref.getString(KEY_EMPLOYEE_ID, null)
        val employeeTypeId = sharedPref.getString(KEY_EMPLOYEE_TYPE_ID, null)
        val employeePositionId = sharedPref.getString(KEY_EMPLOYEE_POSITION_ID, null)
        Log.d("dfa", userId!!+ userName!!)
        return if (userId != null && userName != null && fullName != null) {
            SessionData(
                user_id = userId,
               user_name =  userName,
               full_name =  fullName,
               user_type_id =  userTypeId,
              picture_name =   (if(pictureName.isNullOrEmpty()) "" else pictureName),
               user_type_name =  (if(userTypeName.isNullOrEmpty()) "" else userTypeName),
              district_id =   (if(districtId.isNullOrEmpty()) "" else districtId),
               district_name =  (if(districtName.isNullOrEmpty()) "" else districtName),
               zone_id =  (if(zoneId.isNullOrEmpty()) "" else zoneId),
               zone_name =  (if(zoneName.isNullOrEmpty()) "" else zoneName),
               employee_id =  employeeId,
               employee_type_id =  employeeTypeId,
               employee_position_id =  employeePositionId
            )
        } else {
            null
        }
    }

    fun clearSessionData()
    {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_USER_TYPE_ID = "user_type_id"
        private const val KEY_PICTURE_NAME = "picture_name"
        private const val KEY_USER_TYPE_NAME = "user_type_name"
        private const val KEY_DISTRICT_ID = "district_id"
        private const val KEY_DISTRICT_NAME = "district_name"
        private const val KEY_ZONE_ID = "zone_id"
        private const val KEY_ZONE_NAME = "zone_name"
        private const val KEY_EMPLOYEE_ID = "employee_id"
        private const val KEY_EMPLOYEE_TYPE_ID = "employee_type_id"
        private const val KEY_EMPLOYEE_POSITION_ID = "employee_position_id"
    }
}
