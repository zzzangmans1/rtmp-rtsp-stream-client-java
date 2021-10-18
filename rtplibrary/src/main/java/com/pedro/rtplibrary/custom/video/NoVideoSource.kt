package com.pedro.rtplibrary.custom.video

import android.graphics.SurfaceTexture


/**
 * Created by pedro on 18/10/21.
 */
class NoVideoSource: VideoSource {

  override fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {
  }

  override fun prepare() {
  }

  override fun start() {
  }

  override fun stop() {
  }

  override fun isRunning(): Boolean {
    return false
  }
}