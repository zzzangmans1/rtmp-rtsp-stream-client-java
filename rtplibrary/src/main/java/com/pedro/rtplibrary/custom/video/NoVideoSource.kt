package com.pedro.rtplibrary.custom.video

import android.graphics.SurfaceTexture


/**
 * Created by pedro on 18/10/21.
 */
class NoVideoSource: VideoSource {

  private var running = false

  override fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {
  }

  override fun setVideoInfo(width: Int, height: Int, fps: Int) {

  }

  override fun prepare() {
  }

  override fun start() {
    running = true
  }

  override fun stop() {
    running = false
  }

  override fun isRunning(): Boolean {
    return running
  }
}