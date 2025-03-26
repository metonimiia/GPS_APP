package com.example.gps_app

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class mp3player : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private var trackIndex = 0
    private val tracki = arrayOf(R.raw.test, R.raw.test1)
    private val trackNames = arrayOf("test", "test1")
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

        val playPauseButton = findViewById<Button>(R.id.playpause)
        val stopButton = findViewById<Button>(R.id.stop)
        val cycleButton = findViewById<Button>(R.id.cycle)
        val nextButton = findViewById<Button>(R.id.next)
        val prevButton = findViewById<Button>(R.id.prev)
        val calculatorButton = findViewById<Button>(R.id.calculator)
        songname = findViewById(R.id.songname)
        musicbar = findViewById(R.id.musicbar)
        volume = findViewById(R.id.volume)
        mediaPlayer = MediaPlayer.create(this, tracki[trackIndex])
        musicbar.max = mediaPlayer.duration
        trackName()
        playPauseButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.text = "Play"
            } else {
                mediaPlayer.start()
                playPauseButton.text = "Pause"
                updateSeekBar()
            }
        }

        stopButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.prepare()
                musicbar.progress = 0
                playPauseButton.text = "Play"
            }
        }

        cycleButton.setOnClickListener {
            mediaPlayer.isLooping = !mediaPlayer.isLooping
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

        volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volumemusic = progress / 100f
                mediaPlayer.setVolume(volumemusic, volumemusic)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }
    private fun trackName(){
        songname.text = "Playing: ${trackNames[trackIndex]}"
    }

    private fun nextTrack() {
        trackIndex = (trackIndex + 1) % tracki.size
        play()
    }

    private fun prevTrack() {
        trackIndex = (trackIndex - 1 + tracki.size) % tracki.size
        play()
    }

    private fun play() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        mediaPlayer = MediaPlayer.create(this, tracki[trackIndex])
        musicbar.max = mediaPlayer.duration
        mediaPlayer.start()
        trackName()
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
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}

