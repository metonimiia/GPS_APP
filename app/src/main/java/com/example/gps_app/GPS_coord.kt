package com.example.gps_app

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GPS_coord : AppCompatActivity() {
    private lateinit var current_time: TextView
    private lateinit var longit: TextView
    private lateinit var lat: TextView
    private lateinit var alti: TextView
    private lateinit var jsonOutput: TextView
    private lateinit var handler: Handler
    private lateinit var myFusedLocationProviderClient: FusedLocationProviderClient
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_coord)
        current_time = findViewById(R.id.realtime)
        longit = findViewById(R.id.longitude)
        lat = findViewById(R.id.latitude)
        alti = findViewById(R.id.altitude)
        jsonOutput = findViewById(R.id.json_output)
        handler = Handler(Looper.getMainLooper())
        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        jsonOutput.text = readJsonFile()

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[ACCESS_FINE_LOCATION] == true &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || permissions[ACCESS_BACKGROUND_LOCATION] == true)
                ) {
                    Toast.makeText(this, "Разрешения получены", Toast.LENGTH_LONG).show()
                    getLocation()
                } else {
                    Toast.makeText(this, "Пожалуйста, предоставьте разрешения", Toast.LENGTH_LONG).show()
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION))
        } else {
            getLocation()
        }

        startTimeUpdates()

        val mainButton = findViewById<Button>(R.id.tomain)
        mainButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun startTimeUpdates() {
        val timeUpdateRunnable = object : Runnable {
            override fun run() {
                updateTime()
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1))
            }
        }
        handler.post(timeUpdateRunnable)
    }

    private fun updateTime() {
        val currentTime = dateFormat.format(Date())
        current_time.text = "Время: $currentTime"
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ActivityCompat.checkSelfPermission(
                    this, ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun getLocation() {
        val locationRunnable = object : Runnable {
            override fun run() {
                getLastLocation()
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(locationRunnable)
    }

    private fun getLastLocation() {
        if (checkPermissions() && isLocationEnabled()) {
            myFusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val altitude = location.altitude
                    val times = dateFormat.format(Date())

                    lat.text = "Широта: %.5f".format(latitude)
                    longit.text = "Долгота: %.5f".format(longitude)
                    alti.text = "Высота: %.3f м".format(altitude)

                    saveLocationJson(latitude, longitude, altitude, times)
                    jsonOutput.text = readJsonFile()
                } else {
                    Toast.makeText(this, "Нет данных о местоположении", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Геолокация отключена или нет разрешений", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLocationJson(lat: Double, lon: Double, alt: Double, curtime: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "locationsGPS.json")
        val locationData = JSONObject().apply {
            put("Время", curtime)
            put("Широта", lat)
            put("Долгота", lon)
            put("Высота", alt)
        }

        val jsonLocation: JSONArray = if (file.exists()) {
            try {
                JSONArray(file.readText())
            } catch (e: Exception) {
                JSONArray()
            }
        } else {
            JSONArray()
        }
        jsonLocation.put(locationData)
        file.writeText(jsonLocation.toString(4))
    }

    private fun readJsonFile(): String {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "locationsGPS.json")
        return if (file.exists()) {
            file.readText()
        } else {
            "Файл locationsGPS.json не найден."
        }
    }
}
