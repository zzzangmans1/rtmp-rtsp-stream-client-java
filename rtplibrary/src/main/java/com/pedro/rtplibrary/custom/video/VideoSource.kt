package com.pedro.rtplibrary.custom.video

import android.graphics.SurfaceTexture

/**
 * Created by pedro on 18/10/21.
 */
interface VideoSource {

  fun setSurfaceTexture(surfaceTexture: SurfaceTexture)

  fun setVideoInfo(width: Int, height: Int, fps: Int)

  fun prepare()

  fun start()

  fun stop()

  fun isRunning(): Boolean
}