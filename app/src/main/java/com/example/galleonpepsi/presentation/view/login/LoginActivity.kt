package com.example.galleonpepsi.presentation.view.login

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.galleonpepsi.MainActivity
import com.example.galleonpepsi.R
import com.example.galleonpepsi.databinding.ActivityLoginBinding

import com.example.galleonpepsi.data.User
import com.example.galleonpepsi.core.utils.CustomUtility

import com.example.galleonpepsi.core.utils.StaticTags
import com.example.galleonpepsi.data.login.LoginResponseBody
import com.example.galleonpepsi.presentation.view.login.viewmodel.LoginViewModel
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject

open class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel

    private val PERMISSIONS_LIST = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET
    )
    private val PERMISSION_ALL = 1
    lateinit var binding: ActivityLoginBinding
    var networkAvailable = false
    var sweetAlertDialog: SweetAlertDialog? = null
    var sharedPreferences: SharedPreferences? = null
    var user: User? = null
    var userid: String? = null
    lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        if(StaticTags.isBeta)
        {
            binding.isBeta.visibility = View.VISIBLE
        }

        checkPermission()

        //binding.textUserType.text = getString(R.string.user_type)

        ObjectAnimator.ofFloat(binding.logoLayout, "translationY", -220f).apply {
            duration = 1000
            start()
        }
        viewModel.isLoggedIn.observe(context as LoginActivity) { isLoggedIn ->
            if (isLoggedIn) {
                viewModel.updateSessionData()
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                runOnUiThread()
                {
                    binding.loginLayout.alpha = 0f
                    binding.loginLayout.visibility = View.VISIBLE

                    binding.loginLayout.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .setListener(null)
                }
                LoginButton()
            }
        }

        val timer: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {



                }
            }
        }
        timer.start()




    }

    private fun checkPermission() {
        if (!hasPermissions(this, *PERMISSIONS_LIST)) {
            Log.e("per", "error perm")
            ActivityCompat.requestPermissions(this, PERMISSIONS_LIST, PERMISSION_ALL)
        }
    }

    private fun LoginButton() {

        binding.btnlogin.setOnClickListener {
            networkAvailable = CustomUtility.haveNetworkConnection(this@LoginActivity)
            //checking for
            if (networkAvailable) {
                val id: String = binding.edtid.text.toString()
                userid = id
                val pass: String = binding.edtpass.text.toString()
                if ((id == "")) {
                    binding.edtid.error = "Mandatory Field!"
                }
                else if (pass == ""){
                    binding.edtpass.error = "Mandatory Field!"
                }
                else {

//                    sweetAlertDialog =
//                        SweetAlertDialog(this@LoginActivity, SweetAlertDialog.PROGRESS_TYPE)
//                    sweetAlertDialog!!.titleText = "Loading"
//                    sweetAlertDialog!!.show()
//                    sweetAlertDialog!!.setCancelable(false)
                    val sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                    sweetAlertDialog.titleText = "Loading"
                    sweetAlertDialog.setCancelable(false)
                    sweetAlertDialog.show()
                    val queue = Volley.newRequestQueue(this)
                    val url = StaticTags.BASE_URL + "login/login.php"

                    val sr: StringRequest = object : StringRequest(
                        Method.POST, url,
                        Response.Listener {
                            sweetAlertDialog.dismiss()
                            try {
                                Log.d("response:", it)

                                viewModel.updateSession(Gson().fromJson(it, LoginResponseBody::class.java))

                            } catch (e: JSONException) {
                                CustomUtility.showError(this, e.message, "Failed")
                            }

                        },
                        Response.ErrorListener {
                            sweetAlertDialog.dismiss()
                            CustomUtility.showError(this, "Network problem, try again", "Failed")
                        }) {
                        override fun getParams(): Map<String, String> {
                            val params: MutableMap<String, String> = HashMap()
                            params["LoginId"] = id
                            params["LoginPassword"] = pass

                            return params
                        }

                        @Throws(AuthFailureError::class)
                        override fun getHeaders(): Map<String, String> {
                            val params: MutableMap<String, String> = HashMap()
                            params["Content-Type"] = "application/x-www-form-urlencoded"
                            return params
                        }
                    }
                    queue.add(sr)
                }
            } else {
                CustomUtility.showError(
                    this@LoginActivity,
                    "Please Check your internet connection",
                    "Network Warning !!!"
                )
            }
        }
    }



    fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (permission?.let {
                            ActivityCompat.checkSelfPermission(
                                    context,
                                    it
                            )
                        } != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

}