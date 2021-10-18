package com.pedro.rtplibrary.custom.audio

import com.pedro.encoder.Frame
import com.pedro.encoder.input.audio.GetMicrophoneData
import com.pedro.encoder.input.audio.MicrophoneManager

/**
 * Created by pedro on 18/10/21.
 */
class MicrophoneSource(private val sampleRate: Int = 32000, private val isStereo: Boolean = true, private val echoCanceler: Boolean = false,
  private val noiseSuppressor: Boolean = false): AudioSource, GetMicrophoneData {

  private val microphoneManager = MicrophoneManager(this)

  override fun prepare() {
    microphoneManager.createMicrophone(sampleRate, isStereo, echoCanceler, noiseSuppressor)
  }

  override fun start() {
    microphoneManager.start()
  }

  override fun stop() {
    microphoneManager.stop()
  }

  override fun isRunning(): Boolean {
    return microphoneManager.isRunning
  }

  override fun inputPCMData(frame: Frame?) {

  }
}