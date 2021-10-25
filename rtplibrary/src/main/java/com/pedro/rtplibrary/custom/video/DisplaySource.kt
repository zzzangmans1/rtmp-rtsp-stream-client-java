package com.pedro.rtplibrary.custom.video

import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.view.Surface
import androidx.annotation.RequiresApi

/**
 * Created by pedro on 18/10/21.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class DisplaySource(private val mediaProjection: MediaProjection): VideoSource {

  private var width: Int = 640
  private var height: Int = 480
  private var fps: Int = 30
  private var surfaceTexture: SurfaceTexture? = null
  private var virtualDisplay: VirtualDisplay? = null
  private var running = false

  override fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {
    this.surfaceTexture = surfaceTexture
    if (running) {
      virtualDisplay?.surface = Surface(surfaceTexture)
    }
  }

  override fun setVideoInfo(width: Int, height: Int, fps: Int) {
    this.width = width
    this.height = height
    this.fps = fps
  }

  override fun prepare() {

  }

  override fun start() {
    virtualDisplay = mediaProjection.createVirtualDisplay("Stream Display", width, height, 320, 0,
      Surface(surfaceTexture), null, null)
    running = true
  }

  override fun stop() {
    mediaProjection.stop()
    running = false
  }

  override fun isRunning(): Boolean {
    return running
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