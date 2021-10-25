package com.pedro.rtplibrary.custom.video

import android.graphics.SurfaceTexture
import android.view.Surface
import com.pedro.encoder.input.decoder.LoopFileInterface
import com.pedro.encoder.input.decoder.VideoDecoder
import com.pedro.encoder.input.decoder.VideoDecoderInterface

/**
 * Created by pedro on 18/10/21.
 */
class FromFileVideoSource(private val path: String, private var surfaceTexture: SurfaceTexture,
  private val loopMode: Boolean = false):
  VideoSource, VideoDecoderInterface, LoopFileInterface {

  private val videoDecoder = VideoDecoder(this, this)
  private var running = false

  override fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {
    this.surfaceTexture = surfaceTexture
  }

  override fun setVideoInfo(width: Int, height: Int, fps: Int) {
    TODO("Not yet implemented")
  }

  override fun prepare() {
    videoDecoder.setLoopMode(loopMode)
    videoDecoder.initExtractor(path)
    videoDecoder.prepareVideo(Surface(surfaceTexture))
  }

  override fun start() {
    videoDecoder.start()
    running = true
  }

  override fun stop() {
    videoDecoder.stop()
    running = false
  }

  override fun isRunning(): Boolean {
    return running
  }

  override fun onVideoDecoderFinished() {

  }

  override fun onReset(isVideo: Boolean) {
    stop()
    prepare()
    start()
  }
}