package com.pedro.rtplibrary.custom.audio

import com.pedro.encoder.input.audio.GetMicrophoneData

/**
 * Created by pedro on 18/10/21.
 */
class NoAudioSource(microphoneData: GetMicrophoneData): AudioSource {

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