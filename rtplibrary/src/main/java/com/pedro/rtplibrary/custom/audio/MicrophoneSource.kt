package com.pedro.rtplibrary.custom.audio

import com.pedro.encoder.input.audio.GetMicrophoneData
import com.pedro.encoder.input.audio.MicrophoneManager

/**
 * Created by pedro on 18/10/21.
 */
class MicrophoneSource: AudioSource {

  private var sampleRate = 32000
  private var isStereo = true
  private var callback: GetMicrophoneData? = null
  private var microphoneManager: MicrophoneManager? = null

  override fun setGetMicrophoneData(getMicrophoneData: GetMicrophoneData) {
    this.callback = getMicrophoneData
  }

  override fun setAudioInfo(sampleRate: Int, isStereo: Boolean) {
    this.sampleRate = sampleRate
    this.isStereo = isStereo
  }

  override fun prepare() {
    microphoneManager = MicrophoneManager(callback)
    microphoneManager?.createMicrophone(sampleRate, isStereo, false, false)
  }

  override fun start() {
    microphoneManager?.start()
  }

  override fun stop() {
    microphoneManager?.stop()
  }

  override fun isRunning(): Boolean {
    return microphoneManager?.isRunning ?: false
  }
}