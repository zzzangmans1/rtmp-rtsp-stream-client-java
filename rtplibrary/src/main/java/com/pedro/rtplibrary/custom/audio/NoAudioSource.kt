package com.pedro.rtplibrary.custom.audio

/**
 * Created by pedro on 18/10/21.
 */
class NoAudioSource: AudioSource {

  override fun prepare(sampleRate: Int, channels: Int) {
  }

  override fun start() {
  }

  override fun stop() {
  }

  override fun isRunning(): Boolean {
    return false
  }
}