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
class InternalMicrophoneSource(microphoneData: GetMicrophoneData, private val mediaProjection: MediaProjection, private val sampleRate: Int = 32000,
  private val isStereo: Boolean = true, private val echoCanceler: Boolean = false, private val noiseSuppressor: Boolean = false): AudioSource {

  private val microphoneManager = MicrophoneManager(microphoneData)

  override fun prepare() {
    val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
      .addMatchingUsage(AudioAttributes.USAGE_MEDIA).addMatchingUsage(AudioAttributes.USAGE_GAME)
      .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN).build()
    microphoneManager.createInternalMicrophone(config, sampleRate, isStereo, echoCanceler, noiseSuppressor)
  }

  override fun start() {
    microphoneManager.start()
  }

  override fun stop() {
    microphoneManager.stop()
  }

  override fun isRunning(): Boolean {
    return microphoneManager.isRunning
  }

  fun askMediaProjection(context: Context): Intent {
    val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    return mediaProjectionManager.createScreenCaptureIntent()
  }

  fun getMediaProjection(context: Context, resultCode: Int, resultData: Intent): MediaProjection? {
    val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    return mediaProjectionManager.getMediaProjection(resultCode, resultData)
  }
}