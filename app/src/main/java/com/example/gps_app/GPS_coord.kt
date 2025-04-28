package com.example.gps_app

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class GPS_coord : AppCompatActivity() {
    private lateinit var current_time: TextView
    private lateinit var longit: TextView
    private lateinit var lat: TextView
    private lateinit var alti: TextView
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    private lateinit var handler: Handler
    private lateinit var myFusedLocationProviderClient: FusedLocationProviderClient
    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION= 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gps_coord)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        current_time = findViewById(R.id.realtime)
        longit = findViewById(R.id.longitude)
        lat = findViewById(R.id.latitude)
        alti = findViewById(R.id.altitude)
        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[ACCESS_FINE_LOCATION] == true && permissions[ACCESS_BACKGROUND_LOCATION] == true) {
                Toast.makeText(this, "Разрешения получены", Toast.LENGTH_LONG).show()
                getLocation()
            } else {
                Toast.makeText(this, "Пожалуйста, предоставьте разрешения", Toast.LENGTH_LONG).show()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION))
        }



        handler = Handler(Looper.getMainLooper())
        startTimeUpdates()

    }

    override fun onResume() {
        super.onResume()
        val mainbutton = findViewById<Button>(R.id.tomain)
        mainbutton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        getLocation()
    }

    private fun startTimeUpdates() {
        val timeUpdateRunnable = object : Runnable {
            override fun run() {
                CurTime()
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1))
            }
        }
        handler.post(timeUpdateRunnable)
    }

    private fun CurTime() {
        val currentTime = dateFormat.format(Date())
        current_time.text = "Время: $currentTime"
    }

    private fun checkPermissions(): Boolean{
        if( ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED )
        {
            return true
        } else {
            return false
        }
    }
    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                myFusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        lat.text = "Широта: ${location.latitude}"
                        longit.text = "Долгота: ${location.longitude}"
                        alti.text = "Высота: ${location.altitude} м"

                    } else {
                        Toast.makeText(this, "Нет данных о местоположении", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Пожалуйста, включите геолокацию", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Нет разрешений", Toast.LENGTH_SHORT).show()
        }
    }
}

