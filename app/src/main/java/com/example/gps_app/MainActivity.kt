package com.example.gps_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        val calculatorButton = findViewById<Button>(R.id.tocalculator)
        calculatorButton.setOnClickListener {
            val intent = Intent(this, Calculator::class.java)
            startActivity(intent)
        }
        val mp3playerButton = findViewById<Button>(R.id.tomp3player)
        mp3playerButton.setOnClickListener {
            val intent = Intent(this, mp3player::class.java)
            startActivity(intent)
        }
        val gpsbutton = findViewById<Button>(R.id.togps)
        gpsbutton.setOnClickListener {
            val intent = Intent(this, GPS_coord::class.java)
            startActivity(intent)
        }

    }
}