package com.pedro.rtpstreamer.custom

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.custom.audio.AudioSource
import com.pedro.rtplibrary.custom.audio.InternalMicrophoneSource
import com.pedro.rtplibrary.custom.audio.MicrophoneSource
import com.pedro.rtplibrary.custom.audio.NoAudioSource
import com.pedro.rtplibrary.custom.video.*
import com.pedro.rtplibrary.rtmp.RtmpCustom
import com.pedro.rtplibrary.view.OpenGlView
import com.pedro.rtpstreamer.R

/**
 * Created by pedro on 25/10/21.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class CustomService: Service(), ConnectCheckerRtmp {

  companion object {
    const val DISPLAY_CODE = 1
    var instance: CustomService? = null
    var openGlView: OpenGlView? = null
    private const val TAG = "CustomService"
    private const val channelId = "rtpDisplayStreamChannel"
    private const val notifyId = 123456
  }

  private var rtmpCustom: RtmpCustom? = null
  private var videoSource: VideoSource = Camera1Source(this)
  private var audioSource: AudioSource = NoAudioSource()
  private val mainHandler = Handler(Looper.getMainLooper())
  private var notificationManager: NotificationManager? = null

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.i(TAG, "RTP service started")
    return START_STICKY
  }

  override fun onCreate() {
    super.onCreate()
    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH)
      notificationManager?.createNotificationChannel(channel)
    }
    keepAliveTrick()
    val view = openGlView
    rtmpCustom = if (view != null) {
      RtmpCustom(view, videoSource, audioSource, this)
    } else {
      RtmpCustom(this, videoSource, audioSource, this)
    }
    rtmpCustom?.startPreview()
    instance = this
  }

  private fun keepAliveTrick() {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
      val notification = NotificationCompat.Builder(this, channelId)
        .setOngoing(true)
        .setContentTitle("")
        .setContentText("").build()
      startForeground(1, notification)
    } else {
      startForeground(1, Notification())
    }
  }

  private fun showNotification(text: String) {
    val notification = NotificationCompat.Builder(this, channelId)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("RTP Display Stream")
      .setContentText(text).build()
    notificationManager?.notify(notifyId, notification)
  }

  override fun onDestroy() {
    super.onDestroy()
    rtmpCustom?.stopStream()
    rtmpCustom?.stopPreview()
    instance = null
  }

  fun changeAudioSource(activity: Activity) {
    if (audioSource is NoAudioSource) {
      audioSource = MicrophoneSource()
      rtmpCustom?.changeAudioSource(audioSource)
      Log.i(TAG, "using microphone")
    } else if (audioSource is MicrophoneSource) {
      Log.i(TAG, "ask internal microphone")
      val intent = InternalMicrophoneSource.askMediaProjection(this)
      activity.startActivityForResult(intent , DISPLAY_CODE)
    } else {
      Log.i(TAG, "using no audio")
      audioSource = NoAudioSource()
      rtmpCustom?.changeAudioSource(audioSource)
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  fun changeAudioSource(requestCode: Int, resultCode: Int, data: Intent?) {
    if (data != null && (requestCode == DISPLAY_CODE && resultCode == AppCompatActivity.RESULT_OK)) {
      val mediaProjection = InternalMicrophoneSource.getMediaProjection(this, resultCode, data)
      mediaProjection?.let {
        Log.i(TAG, "using internal microphone")
        audioSource = InternalMicrophoneSource(mediaProjection)
        rtmpCustom?.changeAudioSource(audioSource)
      }
    }
  }

  fun changeVideoSource(activity: Activity) {
    if (videoSource is NoVideoSource) {
      Log.i(TAG, "using camera1")
      videoSource = Camera1Source(this)
      rtmpCustom?.changeVideoSource(videoSource)
    } else if (videoSource is Camera1Source){
      Log.i(TAG, "using camera2")
      videoSource = Camera2Source(this)
      rtmpCustom?.changeVideoSource(videoSource)
    } else if (videoSource is Camera2Source){
      Log.i(TAG, "ask display")
      val intent = DisplaySource.askMediaProjection(this)
      activity.startActivityForResult(intent , DISPLAY_CODE)
    } else {
      Log.i(TAG, "using no video")
      videoSource = NoVideoSource()
      rtmpCustom?.changeVideoSource(videoSource)
    }
  }

  fun changeVideoSource(requestCode: Int, resultCode: Int, data: Intent?) {
    if (data != null && (requestCode == DISPLAY_CODE && resultCode == AppCompatActivity.RESULT_OK)) {
      val mediaProjection = DisplaySource.getMediaProjection(this, resultCode, data)
      mediaProjection?.let {
        Log.i(TAG, "using display")
        videoSource = DisplaySource(mediaProjection)
        rtmpCustom?.changeVideoSource(videoSource)
      }
    }
  }

  fun toggleStartStream(url: String) {
    if (rtmpCustom?.streaming == true) {
      rtmpCustom?.stopStream()
    } else {
      if (rtmpCustom?.prepareVideo() == true && rtmpCustom?.prepareAudio() == true) {
        rtmpCustom?.startStream(url)
      }
    }
  }

  override fun onConnectionStartedRtmp(rtmpUrl: String) {

  }

  override fun onConnectionSuccessRtmp() {
    mainHandler.post {
      Toast.makeText(this, "Connection success", Toast.LENGTH_LONG).show()
    }
  }

  override fun onConnectionFailedRtmp(reason: String) {
    mainHandler.post {
      Toast.makeText(this, reason, Toast.LENGTH_LONG).show()
      rtmpCustom?.stopStream()
    }
  }

  override fun onNewBitrateRtmp(bitrate: Long) {

  }

  override fun onDisconnectRtmp() {
    mainHandler.post {
      Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show()
    }
  }

  override fun onAuthErrorRtmp() {

  }

  override fun onAuthSuccessRtmp() {

  }
}