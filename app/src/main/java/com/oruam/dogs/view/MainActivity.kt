package com.oruam.dogs.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.oruam.dogs.R
import com.oruam.dogs.databinding.ActivityMainBinding
import com.oruam.dogs.util.PERMISSION_SEND_SMS

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

//        Setup of Action bar with nav controller to get the return button
//        navController = Navigation.findNavController(this, R.id.fragment)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }

    fun checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.SEND_SMS
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Send SMS Permission")
                    .setMessage("This app requires access to send an SMS.")
                    .setPositiveButton("Ask me") { dialog, which ->
                        requestSmsPermission()
                    }
                    .setNegativeButton("No") { dialog, which ->
                        notifyDetailFragment(false)
                    }
                    .show()
            } else {
                requestSmsPermission()
            }
        } else {
            notifyDetailFragment(true)
        }
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.SEND_SMS),
            PERMISSION_SEND_SMS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_SEND_SMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    notifyDetailFragment(true)
                } else {
                    notifyDetailFragment(false)
                }
            }
        }
    }

    private fun notifyDetailFragment(permission: Boolean) {
        val activityFragment = supportFragmentManager.primaryNavigationFragment
        if (activityFragment is DetailFragment) {
            (activityFragment as DetailFragment).onPermissionResult(permission)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}