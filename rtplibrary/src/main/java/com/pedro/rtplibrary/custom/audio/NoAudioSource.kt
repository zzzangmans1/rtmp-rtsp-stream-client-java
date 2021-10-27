package com.pedro.rtplibrary.custom.audio

import com.pedro.encoder.input.audio.GetMicrophoneData


/**
 * Created by pedro on 18/10/21.
 */
class NoAudioSource: AudioSource {

  private var running = false

  override fun setGetMicrophoneData(getMicrophoneData: GetMicrophoneData) {
  }

  override fun setAudioInfo(sampleRate: Int, isStereo: Boolean) {
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