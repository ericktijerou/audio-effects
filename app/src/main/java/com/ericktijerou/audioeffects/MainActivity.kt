package com.ericktijerou.audioeffects

import android.Manifest
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ericktijerou.audioeffects.library.AudioFile
import com.ericktijerou.audioeffects.library.FFMpegCallback
import com.ericktijerou.audioeffects.library.FFMpegMediaConverter
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var player: MediaPlayer? = null
    private val LOG_TAG = "AudioRecordTest"
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private var fileName = ""
    private var playerBackground: MediaPlayer? = null
    private var playerEffects: MediaPlayer? = null
    private var backgroundMusic = ""
    private var recorder: MediaRecorder? = null
    private var countDownTimer: CountDownTimer? = null
    private var outFile = ""

    var btRecord: Button? = null
    var ivNext: ImageView? = null
    var tvTimer: TextView? = null


    private fun startT() {
        player = MediaPlayer()
        try {
            player?.setDataSource(outFile)
            player?.prepare()
            player?.start()
            countDownTimer = object : CountDownTimer(31000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    tvTimer?.text = (millisUntilFinished / 1000).toString() + ""
                    if (tvTimer?.getText() === "1") {
                        Handler().postDelayed({
                            if (!isFinishing()) {
                                btRecord?.performClick()
                                tvTimer?.setText(R.string._30)
                            }
                        }, 1000)
                    }
                }

                override fun onFinish() {}
            }
            countDownTimer?.start()
        } catch (e: IOException) {
        }
    }

    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ivNext = findViewById(R.id.ivNext)
        tvTimer = findViewById(R.id.tvTimer)
        btRecord = findViewById(R.id.btRecord)
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        btRecord?.setOnClickListener {
            val btRecord = findViewById<Button>(R.id.btRecord)
            if (btRecord.text === getString(R.string.record)) {
                if (!backgroundMusic.isEmpty()) {
                    playBackgroundMusic(backgroundMusic)
                }
                startRecording()
            } else {
                btRecord.setText(R.string.record)
                countDownTimer?.cancel()
                stopRecording()
            }
        }
        findViewById<ImageView>(R.id.ivNext)?.setOnClickListener {
            startActivity(Intent(this, EffectsActivity::class.java).apply {
                putExtra(
                    "fileName",
                    fileName
                )
            })
        }
    }

    override fun onStop() {
        super.onStop()
        if (recorder != null) {
            recorder?.release()
            recorder = null
        }
        if (playerBackground != null) {
            playerBackground?.release()
        }
        if (playerEffects != null) {
            playerEffects?.release()
        }
        if (countDownTimer != null) {
            countDownTimer?.cancel()
        }
        playerBackground = null
        playerEffects = null
    }

    fun stopRecording() {
        ivNext?.visibility = View.VISIBLE
        if (playerEffects != null) {
            playerEffects?.stop()
        }
        if (playerBackground != null) {
            playerBackground?.stop()
        }
        if (recorder != null) {
            recorder?.stop()
            recorder?.release()
            recorder = null
        }
    }

    private fun playEffects(fileName: String) {
        if (playerEffects != null) {
            playerEffects?.stop()
            playerEffects?.release()
        }
        playerEffects = MediaPlayer()
        try {
            val descriptor = assets.openFd(fileName)
            makeAudioStreamMaxVolume()
            playerEffects?.setDataSource(
                descriptor.fileDescriptor,
                descriptor.startOffset,
                descriptor.length
            )
            playerEffects?.prepare()
            playerEffects?.start()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
    }

    private fun playBackgroundMusic(fileName: String) {
        if (playerBackground != null) {
            playerBackground?.stop()
            playerBackground?.release()
        }
        playerBackground = MediaPlayer()
        try {
            val descriptor = assets.openFd(fileName)
            makeAudioStreamMaxVolume()
            playerBackground?.setDataSource(
                descriptor.fileDescriptor,
                descriptor.startOffset,
                descriptor.length
            )
            playerBackground?.prepare()
            playerBackground?.start()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
    }


    fun startRecording() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == -1 || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == -1 || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == -1
        ) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
            return
        }
        ivNext?.visibility = View.GONE
        btRecord?.setText(R.string.stop)
        tvTimer?.setText(R.string._30)
        countDownTimer = object : CountDownTimer(31000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer?.text = (millisUntilFinished / 1000).toString() + ""
                if (tvTimer?.text === "1") {
                    Handler().postDelayed({
                        if (!isFinishing) {
                            btRecord?.performClick()
                            tvTimer?.setText(R.string._30)
                        }
                    }, 1000)
                }
            }

            override fun onFinish() {}
        }

        countDownTimer?.start()

        val dir = File(filesDir, "recordFiles").createDirIfNotExists()
        fileName = dir.absolutePath + "/audioRecord${System.currentTimeMillis()}.mp3"
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        recorder?.setOutputFile(fileName)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        try {
            recorder?.prepare()
            recorder?.start()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
    }

    private fun makeAudioStreamMaxVolume() {
        try {
            val mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            mAudioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

fun File.createDirIfNotExists() = apply {
    if (!exists()) {
        mkdir()
    }
}