package com.pedro.rtplibrary.custom.video

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.Surface
import com.pedro.encoder.input.video.Camera1ApiManager

/**
 * Created by pedro on 18/10/21.
 */
class Camera1Source(private val context: Context): VideoSource {

  var camera1ApiManager: Camera1ApiManager? = null
  private var width: Int = 640
  private var height: Int = 480
  private var fps: Int = 30
  private var surfaceTexture: SurfaceTexture? = null

  override fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {
    this.surfaceTexture = surfaceTexture
  }

  override fun setVideoInfo(width: Int, height: Int, fps: Int) {
    this.width = width
    this.height = height
    this.fps = fps
  }

  override fun prepare() {
    camera1ApiManager = Camera1ApiManager(surfaceTexture, context)
  }

  override fun start() {
    camera1ApiManager?.setSurfaceTexture(surfaceTexture)
    camera1ApiManager?.start(width, height, fps)
  }

  override fun stop() {
    camera1ApiManager?.stop()
  }

  override fun isRunning(): Boolean {
    return camera1ApiManager?.isRunning ?: false
  }
}