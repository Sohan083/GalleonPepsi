package com.example.galleonpepsi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.galleonpepsi.data.User
import com.example.galleonpepsi.data.User.Companion.user
import com.example.galleonpepsi.presentation.view.login.LoginActivity

import com.example.galleonpepsi.core.utils.CustomUtility
import com.example.galleonpepsi.core.utils.GPSLocation
import com.example.galleonpepsi.core.utils.StaticTags.Companion.GPS_REQUEST
import com.example.galleonpepsi.core.utils.StaticTags.Companion.LOCATION_REQUEST
import com.example.galleonpepsi.presentation.view.login.viewmodel.LoginViewModel

import com.google.android.material.navigation.NavigationView
import java.util.*

class MainActivity : AppCompatActivity() {

    private var isGPSEnabled: Boolean = false
    //private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: LoginViewModel
    private var gpsLocation: GPSLocation? = null
    private lateinit var navController: NavController
    var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)



        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Outlet Registration"

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val appBarConfiguration = AppBarConfiguration(topLevelDestinationIds = setOf(
            R.id.outletRegistrationFragment,
           // R.id.outletListFragment,
//            R.id.dashboardFragment,
         //   R.id.posmActivityFragment,
            R.id.attendanceFragment,
            R.id.logoutFragment,
        ),drawerLayout)
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)
        val navigationView =
            findViewById<NavigationView>(R.id.nav_view)
        navigationView?.setupWithNavController(navController)

        menuNav = navigationView.menu

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        viewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                viewModel.updateSessionData()

            } else {
                val s = SweetAlertDialog(applicationContext,SweetAlertDialog.WARNING_TYPE)
                s.titleText = "Session Out"
                s.setConfirmButton("Login", SweetAlertDialog.OnSweetClickListener {
                    finish()
                    startActivity(Intent(applicationContext, LoginActivity::class.java))
                })
                s.setCancelable(false)
                s.show()
            }
        }
        val navHeaderMainView = navigationView.getHeaderView(0)
        viewModel.userData.observe(this) {
            navHeaderMainView.findViewById<TextView>(R.id.name).text = it.full_name
            if(it.zone_name != "null")
            navHeaderMainView.findViewById<TextView>(R.id.location).text = it.zone_name
        }

        user = User.instance
        if (user == null) {
            val sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            sweetAlertDialog.setTitle("No user found please login")
            sweetAlertDialog.setConfirmButton("Ok") {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }




        //ask for gps
        CustomUtility.haveGpsEnabled(this)




        gpsLocation = GPSLocation(this)
        gpsLocation!!.GPS_Start()
        gpsLocation!!.setLocationChangedListener(object : GPSLocation.LocationChangedListener {
            override fun locationChangeCallback(lat: String?, lon: String?, acc: String?) {
                presentLat = lat
                presentLon = lon
                presentAcc = acc
            }

        })
    }



    companion object {
        var presentLat: String? = null; var presentLon: String? = null; var presentAcc:String? = null
        var retailUserName: String? = null; var retailPassword: String? = null
        var menuNav: Menu? = null
        var isInActivity = false
    }


    override fun onBackPressed() {
        if (Objects.requireNonNull(navController.currentDestination)!!.id  == R.id.attendanceFragment) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to go activity", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
        else if(Objects.requireNonNull(navController.currentDestination)!!.id == R.id.pictureFragment) {
            finish()
            startActivity(intent)
        }
        else if (Objects.requireNonNull(navController.currentDestination)?.id == R.id.posmActivityFragment) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to return back", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
        else {
            super.onBackPressed()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST) {
                isGPSEnabled = true
                //invokeLocationAction()
            }
        }
    }
    /*

    private fun invokeLocationAction() {
        when {


            //isPermissionsGranted() -> startLocationUpdate()

            shouldShowRequestPermissionRationale() -> latLong.text = getString(R.string.permission_request)

            else -> ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST
            )
        }
    }

     */
/*
    private fun startLocationUpdate() {
        locationViewModel.getLocationData().observe(this, Observer {
            latLong.text =  getString(R.string.latLong, it.longitude, it.latitude)
        })
    }

 */

    private fun isPermissionsGranted() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowRequestPermissionRationale() =
        ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) && ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST -> {
                //invokeLocationAction()
            }
        }
    }
}