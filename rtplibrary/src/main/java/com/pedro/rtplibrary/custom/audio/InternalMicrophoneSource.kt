package com.pedro.rtplibrary.custom.audio

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioPlaybackCaptureConfiguration
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.pedro.encoder.Frame
import com.pedro.encoder.input.audio.GetMicrophoneData
import com.pedro.encoder.input.audio.MicrophoneManager

/**
 * Created by pedro on 18/10/21.
 */
@RequiresApi(api = Build.VERSION_CODES.Q)
class InternalMicrophoneSource(private val mediaProjection: MediaProjection): AudioSource {

  private var sampleRate = 32000
  private var isStereo = true
  private var callback: GetMicrophoneData? = null
  private var microphoneManager: MicrophoneManager? = null

  override fun setGetMicrophoneData(getMicrophoneData: GetMicrophoneData) {
    this.callback = getMicrophoneData
  }

  override fun setAudioInfo(sampleRate: Int, isStereo: Boolean) {
    this.sampleRate = sampleRate
    this.isStereo = isStereo
  }

  override fun prepare() {
    microphoneManager = MicrophoneManager(callback)
    val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
      .addMatchingUsage(AudioAttributes.USAGE_MEDIA).addMatchingUsage(AudioAttributes.USAGE_GAME)
      .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN).build()
    microphoneManager?.createInternalMicrophone(config, sampleRate, isStereo, false, false)
  }

  override fun start() {
    microphoneManager?.start()
  }

  override fun stop() {
    microphoneManager?.stop()
  }

  override fun isRunning(): Boolean {
    return microphoneManager?.isRunning ?: false
  }

  companion object {
    fun askMediaProjection(context: Context): Intent {
      val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
      return mediaProjectionManager.createScreenCaptureIntent()
    }

    fun getMediaProjection(context: Context, resultCode: Int, resultData: Intent): MediaProjection? {
      val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
      return mediaProjectionManager.getMediaProjection(resultCode, resultData)
    }
  }
}