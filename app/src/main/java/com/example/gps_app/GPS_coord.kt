package com.example.gps_app

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.*
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
import org.zeromq.SocketType
import org.zeromq.ZContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager

class GPS_coord : AppCompatActivity() {

    private lateinit var current_time: TextView
    private lateinit var longit: TextView
    private lateinit var lat: TextView
    private lateinit var alti: TextView
    private lateinit var jsonOutput: TextView
    private lateinit var logView: TextView
    private lateinit var handler: Handler
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var lteInfoView: TextView
    private lateinit var myFusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_coord)
        current_time = findViewById(R.id.realtime)
        longit = findViewById(R.id.longitude)
        lat = findViewById(R.id.latitude)
        alti = findViewById(R.id.altitude)
        jsonOutput = findViewById(R.id.json_output)
        logView = findViewById(R.id.tvSockets)
        lteInfoView = findViewById(R.id.lte_info)

        handler = Handler(Looper.getMainLooper())
        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        jsonOutput.text = readJsonFile()

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions[ACCESS_FINE_LOCATION] == true && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || permissions[ACCESS_BACKGROUND_LOCATION] == true)
                ) {
                    Toast.makeText(this, "Разрешения получены", Toast.LENGTH_LONG).show()
                    getLocation()
                } else {
                    Toast.makeText(this, "Пожалуйста, предоставьте разрешения", Toast.LENGTH_LONG).show()
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                arrayOf(ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION)
            )
        } else {
            getLocation()
        }
        findViewById<Button>(R.id.tomain).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun appendLog(text: String) {
        runOnUiThread {
            logView.append(text + "\n")
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ActivityCompat.checkSelfPermission(this, ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun getLocation() {
        val locationRunnable = object : Runnable {
            override fun run() {
                getLastLocation()
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(locationRunnable)
    }

    private fun getLastLocation() {
        if (!checkPermissions()) {
            Toast.makeText(this, "Нет разрешений на геолокацию", Toast.LENGTH_SHORT).show()
            return
        }

        if (isLocationEnabled()) {
            myFusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                    val timestamp = System.currentTimeMillis()
                    val times = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                    current_time.text = "Время: $times"
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val altitude = location.altitude
                        val speed = location.speed
                        val accuracy = location.accuracy
                        lat.text = "Широта: %.5f".format(latitude)
                        longit.text = "Долгота: %.5f".format(longitude)
                        alti.text = "Высота: %.3f м".format(altitude)
                        var lteInfoJson: JSONObject? = null

                        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            val allCellInfo = telephonyManager.allCellInfo
                            for (cellInfo in allCellInfo) {
                                if (cellInfo is CellInfoLte) {
                                    val cellIdentity = cellInfo.cellIdentity
                                    val cellSignal = cellInfo.cellSignalStrength
                                    lteInfoJson = JSONObject().apply {
                                        put("MCC", cellIdentity.mcc)
                                        put("MNC", cellIdentity.mnc)
                                        put("PCI", cellIdentity.pci)
                                        put("TAC", cellIdentity.tac)
                                        put("RSRP", cellSignal.rsrp)
                                        put("RSRQ", cellSignal.rsrq)
                                        put("RSSI", cellSignal.rssi)
                                        put("RSSNR", cellSignal.rssnr)
                                        put("TimingAdvance", cellSignal.timingAdvance)
                                    }
                                    runOnUiThread {
                                        lteInfoView.text = "LTE:\nMCC: ${cellIdentity.mcc}\nMNC: ${cellIdentity.mnc}\nPCI: ${cellIdentity.pci}\nTAC: ${cellIdentity.tac}\nRSSI: ${cellSignal.rssi} дБм\nRSRP: ${cellSignal.rsrp} дБм\nRSRQ: ${cellSignal.rsrq} дБ\nRSSNR: ${cellSignal.rssnr}"
                                    }
                                    break
                                }
                            }
                        }
                        val jsonToSend = JSONObject().apply {
                            put("Время", times)
                            put("Широта", latitude)
                            put("Долгота", longitude)
                            put("Высота", altitude)
                            put("Скорость (м/с)", speed)
                            put("Точность (м)", accuracy)
                            val lteJson = JSONObject()
                            if (lteInfoJson != null) {
                                for (key in lteInfoJson.keys()) {
                                    lteJson.put(key, lteInfoJson.get(key))
                                }
                            }
                            put("LTE", lteJson)
                        }
                        saveLocationJson(jsonToSend)
                        jsonOutput.text = readJsonFile()
                        sendToServer()
                    } else {
                        Toast.makeText(this, "Нет данных о местоположении", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun saveLocationJson(data: JSONObject) {
        val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(downloadsDir, "locationGPS.json")
        val jsonArray: JSONArray = if (file.exists()) {
            try {
                JSONArray(file.readText())
            } catch (e: Exception) {
                JSONArray()
            }
        } else {
            JSONArray()
        }
        jsonArray.put(data)
        file.writeText(jsonArray.toString(4))
    }

    private fun readJsonFile(): String {
        val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(downloadsDir, "locationGPS.json")
        return if (file.exists())
            file.readText()
        else
            "Файл locationGPS.json не найден."
    }

    private fun sendToServer() {
        Thread {
            val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(downloadsDir, "locationGPS.json")

            if (!file.exists()) {
                appendLog("[Клиент] Нет данных для отправки")
                return@Thread
            }
            val jsonArray: JSONArray = try {
                JSONArray(file.readText())
            } catch (e: Exception) {
                appendLog("[Ошибка] Ошибка чтения JSON: ${e.localizedMessage}")
                return@Thread
            }
            if (jsonArray.length() == 0) {
                appendLog("[Клиент] JSON пустой, отправлять нечего")
                return@Thread
            }
            val serverAddr = "tcp://10.161.151.149:2222"
            try {
                ZContext().use { context ->
                    context.createSocket(SocketType.REQ).use { socket ->
                        socket.connect(serverAddr)
                        socket.receiveTimeOut = 5000
                        val successfullySentIndices = mutableListOf<Int>()
                        for (i in 0 until jsonArray.length()) {
                            val jsonData =
                                jsonArray.getJSONObject(i).toString()
                            socket.send(jsonData.toByteArray(Charsets.UTF_8))
                            appendLog("[Клиент] Отправил: $jsonData")
                            val reply = socket.recv(0)
                            if (reply != null) {
                                val repStr =
                                    String(reply, Charsets.UTF_8)
                                appendLog("[Сервер] Ответ: $repStr")
                                successfullySentIndices.add(i)
                            } else {
                                appendLog("[Клиент] Нет ответа от сервера (timeout)")
                                break
                            }
                        }
                        if (successfullySentIndices.isNotEmpty()) {
                            val newJsonArray = JSONArray()
                            for (i in 0 until jsonArray.length()) {
                                if (!successfullySentIndices.contains(i)) {
                                    newJsonArray.put(
                                        jsonArray.getJSONObject(i)
                                    )
                                }
                            }
                            file.writeText(newJsonArray.toString(4))
                            appendLog("[Клиент] Удалены успешно отправленные данные")
                        }
                    }
                }
            } catch (e: Exception) {
                appendLog("[Ошибка] Ошибка отправки: ${e.localizedMessage}")
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
