package com.pedro.rtpstreamer.custom

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.pedro.rtplibrary.view.OpenGlView
import com.pedro.rtpstreamer.R

/**
 * Created by pedro on 25/10/21.
 *
 * Not well tested.
 *
 * Working:
 * - Video stream
 * - Start and stop preview
 * - Start and stop stream
 * - Change video source (except from file source)
 * Developing:
 * - Audio stream (current no audio)
 * - Change audio source
 * - Record local video
 * - Adapt video source resolution to fit preview/stream and rotation on fly
 *
 * Controls:
 *
 * Switch camera button to change video source. Automatically changed in this order:
 * No video, camera1, camera2, display.
 *
 * Start stream button to start/stop stream
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class CustomModeRtmp: AppCompatActivity(), SurfaceHolder.Callback {

  private lateinit var view: OpenGlView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_open_gl)
    view = findViewById(R.id.surfaceView)
    val etUrl = findViewById<EditText>(R.id.et_rtp_url)
    etUrl.setHint(R.string.hint_rtmp)
    val bStartStop = findViewById<Button>(R.id.b_start_stop)
    val bSwitch = findViewById<Button>(R.id.switch_camera)

    bStartStop.setOnClickListener {
      val url = etUrl.text.toString()
      CustomService.instance?.toggleStartStream(url)
    }
    bSwitch.setOnClickListener {
      CustomService.instance?.changeAudioSource(this)
    }
    CustomService.openGlView = view
    view.holder.addCallback(this)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    CustomService.instance?.changeAudioSource(requestCode, resultCode, data)
  }

  override fun surfaceCreated(holder: SurfaceHolder) {

  }

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    startService(Intent(this, CustomService::class.java))
  }

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    stopService(Intent(this, CustomService::class.java))
  }
}