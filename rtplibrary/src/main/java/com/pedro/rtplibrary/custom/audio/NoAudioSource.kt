package com.pedro.rtplibrary.custom.audio


/**
 * Created by pedro on 18/10/21.
 */
class NoAudioSource: AudioSource {

  private var running = false

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