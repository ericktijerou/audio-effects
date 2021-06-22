package com.ericktijerou.audioeffects

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.arthenica.ffmpegkit.FFmpegKit
import com.ericktijerou.audioeffects.library.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.IOException


class EffectsActivity : AppCompatActivity(), FFMpegCallback {
    private var fileName = ""
    private var fileNameNew = ""
    private var player: MediaPlayer? = null
    private var mediaConverter: FFMpegMediaConverter? = null
    private val tiEchoInGain by lazy { findViewById<TextInputLayout>(R.id.tiEchoInGain) }
    private val tiEchoOutGain by lazy { findViewById<TextInputLayout>(R.id.tiEchoOutGain) }
    private val tiEchoDelays by lazy { findViewById<TextInputLayout>(R.id.tiEchoDelays) }
    private val tiEchoDecays by lazy { findViewById<TextInputLayout>(R.id.tiEchoDecays) }
    private val etEchoInGain by lazy { findViewById<TextInputEditText>(R.id.etEchoInGain) }
    private val etEchoOutGain by lazy { findViewById<TextInputEditText>(R.id.etEchoOutGain) }
    private val etEchoDelays by lazy { findViewById<TextInputEditText>(R.id.etEchoDelays) }
    private val etEchoDecays by lazy { findViewById<TextInputEditText>(R.id.etEchoDecays) }
    private var isEchoInGainValid = true
    private var isEchoOutGainValid = true
    private var isEchoDelayValid = true
    private var isEchoDecaysValid = true
    private val btnEcho by lazy { findViewById<Button>(R.id.btnEcho) }
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_effects)
        mediaConverter = FFMpegMediaConverter(this)
        fileName = intent.getStringExtra("fileName").orEmpty()
        val dir = File(filesDir, "recordFiles").createDirIfNotExists()
        fileNameNew = dir.absolutePath + "/audioRecordNew.mp3"

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            onBackPressed()
        }
        findViewById<Button>(R.id.btnNormal).setOnClickListener {
            playNormal()
        }
        findViewById<Button>(R.id.btnReverb).setOnClickListener {
            playChipmunk()
        }
        btnEcho.setOnClickListener {
            playEcho()
        }
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
        etEchoInGain.addTextChangedListener {
            it?.run {
                isEchoInGainValid = (isNotEmpty() && (this.toString().toDouble() in 0.1..1.0)).also { isValid ->
                    if (isValid) {
                        tiEchoInGain.error = null
                    } else {
                        tiEchoInGain.error = "Value is out of range"
                    }
                }
            }
            updateEchoButton()
        }
        etEchoOutGain.addTextChangedListener {
            it?.run {
                isEchoOutGainValid = (isNotEmpty() && (this.toString().toDouble() in 0.1..1.0)).also { isValid ->
                    if (isValid) {
                        tiEchoOutGain.error = null
                    } else {
                        tiEchoOutGain.error = "Value is out of range"
                    }
                }
            }
            updateEchoButton()
        }
        etEchoDelays.addTextChangedListener {
            it?.run {
                isEchoDelayValid = (isNotEmpty() && (this.toString().toLong() in 1..90000)).also { isValid ->
                    if (isValid) {
                        tiEchoDelays.error = null
                    } else {
                        tiEchoDelays.error = "Value is out of range"
                    }
                }
            }
            updateEchoButton()
        }
        etEchoDecays.addTextChangedListener {
            it?.run {
                isEchoDecaysValid = (isNotEmpty() && (this.toString().toDouble() in 0.1..1.0)).also { isValid ->
                    if (isValid) {
                        tiEchoDecays.error = null
                    } else {
                        tiEchoDecays.error = "Value is out of range"
                    }
                }
            }
            updateEchoButton()
        }
    }

    private fun updateEchoButton() {
        btnEcho.isEnabled = isEchoInGainValid && isEchoOutGainValid && isEchoDelayValid && isEchoDecaysValid
    }
    private fun start() {
        player = MediaPlayer()
        try {
            player?.setDataSource(fileNameNew)
            player?.setVolume(1f, 1f)
            player?.prepare()
            player?.start()
        } catch (e: IOException) {
            Log.e(TAG, "prepare() failed")
        }
    }

    private fun playRadio() {
        showProgress()
        if (player != null) {
            player?.stop()
        }
        val options: Options = Options.Builder(Effects.RADIO, fileName, fileNameNew).build()
        mediaConverter?.execute(options)
    }

    private fun playChipmunk() {
        val dir = File(filesDir, "recordFiles").createDirIfNotExists()
        fileNameNew = dir.absolutePath + "/audioRecordReverb.aac"
        showProgress()
        if (player != null) {
            player?.stop()
        }
        val source = AudioFile(getFileFromAssets(this, "clap.mp3").getAbsolutePath(), 0)
        val temporarymp3 = createTempFile(
            suffix = ".mp3",
            directory = File(getExternalFilesDir(null)?.absolutePath.orEmpty()),
            prefix = System.currentTimeMillis().toString()
        )
        val commands = "-y -i $fileName -filter:a volume=0.0 ${temporarymp3.absolutePath}"
        FFmpegKit.execute(commands.split(" ").toTypedArray())
        val cmd1 = arrayOf(
            "-y",
            "-i",
            fileName,
            "-i",
            source.filePath,
            "-filter_complex",
            "[0] [1] afir=dry=10:wet=10 [reverb]; [0] [reverb] amix=inputs=2:weights=10 1",
            fileNameNew
        )
        mediaConverter?.execute(cmd1)
    }

    private fun playRobot() {
        showProgress()
        if (player != null) {
            player?.stop()
        }
        val options: Options = Options.Builder(Effects.ROBOT, fileName, fileNameNew).build()
        mediaConverter?.execute(options)
    }

    private fun playNormal() {
        val dir = File(filesDir, "recordFiles").createDirIfNotExists()
        fileNameNew = dir.absolutePath + "/audioRecordNormal.aac"
        showProgress()
        if (player != null) {
            player?.stop()
        }
        val options: Options =
            Options.Builder(Effects.NONE, fileName, fileNameNew).option3("-ac").build()
        mediaConverter?.execute(options)
    }

    private fun playEcho() {
        val dir = File(filesDir, "recordFiles").createDirIfNotExists()
        fileNameNew = dir.absolutePath + "/audioRecordEcho.aac"
        showProgress()
        if (player != null) {
            player?.stop()
        }

        val options: Options = Options.Builder(
            "aecho=${etEchoInGain.text}:${etEchoOutGain.text}:${etEchoDelays.text}:${etEchoDecays.text}",
            fileName,
            fileNameNew
        ).build()
        mediaConverter?.execute(options)
    }

    public override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    private fun showProgress() {
        findViewById<View>(R.id.progress_circular).visibility = View.VISIBLE
    }

    private fun hideProgress() {
        findViewById<View>(R.id.progress_circular).visibility = View.GONE
    }

    override fun onSuccess(commands: Array<String>) {
        hideProgress()
        start()
    }

    override fun onCancel(commands: Array<String>) {

    }

    override fun onFailed(commands: Array<String>, rc: Int, out: String?) {

    }

    companion object {
        const val TAG = "EffectsActivity"
    }
}
