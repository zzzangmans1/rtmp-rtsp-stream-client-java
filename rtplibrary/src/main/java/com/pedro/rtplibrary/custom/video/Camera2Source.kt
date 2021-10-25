package com.pedro.rtplibrary.custom.video

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Build
import androidx.annotation.RequiresApi
import com.pedro.encoder.input.video.Camera2ApiManager

/**
 * Created by pedro on 18/10/21.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class Camera2Source(private val context: Context): VideoSource {

  var camera2ApiManager: Camera2ApiManager? = null
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
    camera2ApiManager = Camera2ApiManager(context)
    camera2ApiManager?.prepareCamera(surfaceTexture, width, height, fps)
  }

  override fun start() {
    camera2ApiManager?.openLastCamera()
  }

  override fun stop() {
    camera2ApiManager?.closeCamera()
  }

  override fun isRunning(): Boolean {
    return camera2ApiManager?.isRunning ?: false
  }
}