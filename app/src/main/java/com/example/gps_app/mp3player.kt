package com.example.gps_app

import android.Manifest
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class mp3player : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private var trackIndex = 0
    private var tracki: MutableList<Pair<String, Uri>> = mutableListOf()
    private lateinit var songname: TextView
    private lateinit var musicbar: SeekBar
    private lateinit var volume: SeekBar
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mp3player)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                playMusic()
                Toast.makeText(this, "Разрешения получены", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Пожалуйста выдайте разрешение", Toast.LENGTH_LONG).show()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val playPauseButton = findViewById<Button>(R.id.playpause)
        val cycleonoff = findViewById<TextView>(R.id.onoffcycle)
        val stopButton = findViewById<Button>(R.id.stop)
        val nextButton = findViewById<Button>(R.id.next)
        val prevButton = findViewById<Button>(R.id.prev)
        val cycleButton = findViewById<Button>(R.id.cycle)
        val calculatorButton = findViewById<Button>(R.id.mainactivity)
        songname = findViewById(R.id.songname)
        musicbar = findViewById(R.id.musicbar)
        volume = findViewById(R.id.volume)

        mediaPlayer = MediaPlayer()

        playPauseButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.text = "Play"
            } else
                mediaPlayer.start()
                playPauseButton.text = "Pause"
                updateSeekBar()
        }

        cycleButton.setOnClickListener {
            if (mediaPlayer.isLooping) {
                mediaPlayer.isLooping = false
                cycleonoff.text = "Cycle Off"
            } else {
                mediaPlayer.isLooping = true
                cycleonoff.text = "Cycle On"
            }
        }

        stopButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                playPauseButton.text = "Play"
                musicbar.progress = 0
            }
        }

        nextButton.setOnClickListener {
            nextTrack()
        }

        prevButton.setOnClickListener {
            prevTrack()
        }

        calculatorButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun playMusic() {
        val musicPath = Environment.getExternalStorageDirectory().path + "/Music"
        val directory = File(musicPath)
        if (!directory.exists() || !directory.isDirectory) {
            return
        }
        directory.listFiles { file ->
            file.isFile && file.name.endsWith(".mp3", ignoreCase = true)
        }?.forEach { mp3File ->
            val title = mp3File.nameWithoutExtension
            val contentUri = Uri.fromFile(mp3File)
            tracki.add(Pair(title, contentUri))
        }
        if (tracki.isNotEmpty()) {
            trackIndex = 0
            songname.text = "Playing: ${tracki[0].first}"
        }
    }

    override fun onStart() {
        super.onStart()
        musicbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        volume.progress = 50
        volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volumemusic = progress / 100f
                mediaPlayer.setVolume(volumemusic, volumemusic)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


    private fun nextTrack() {
        if (tracki.isEmpty())
            return
        trackIndex = (trackIndex + 1) % tracki.size
        play()
    }


    private fun prevTrack() {
        if (tracki.isEmpty())
            return
        trackIndex = (trackIndex - 1 + tracki.size) % tracki.size
        play()
    }

    private fun play() {
        val currentTrack = tracki[trackIndex].second
        mediaPlayer.reset()
        mediaPlayer.setDataSource(applicationContext, currentTrack)
        mediaPlayer.prepare()
        mediaPlayer.start()
        musicbar.max = mediaPlayer.duration
        songname.text = "Playing: ${tracki[trackIndex].first}"
        updateSeekBar()
    }

    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    musicbar.progress = mediaPlayer.currentPosition
                    handler.postDelayed(this, 10)
                }
            }
        }, 10)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}