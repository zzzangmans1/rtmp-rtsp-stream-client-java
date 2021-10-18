package com.pedro.rtplibrary.custom.video

import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.view.Surface
import androidx.annotation.RequiresApi

/**
 * Created by pedro on 18/10/21.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class DisplaySource(private val mediaProjection: MediaProjection, private val width: Int = 640,
  private val height: Int = 480, private val dpi: Int = 320, private var surfaceTexture: SurfaceTexture): VideoSource {

  private var running = false
  override fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {

  }

  override fun prepare() {

  }

  override fun start() {
    mediaProjection.createVirtualDisplay("Stream Display", width, height, dpi, 0, Surface(surfaceTexture), null, null)
    running = true
  }

  override fun stop() {
    mediaProjection.stop()
    running = false
  }

  override fun isRunning(): Boolean {
    return running
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