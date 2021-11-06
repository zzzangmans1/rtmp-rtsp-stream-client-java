package com.pedro.rtplibrary.custom.audio

import com.pedro.encoder.input.audio.GetMicrophoneData
import com.pedro.encoder.input.decoder.*

/**
 * Created by pedro on 18/10/21.
 */
class FromFileAudioSource(private val path: String, private val loopMode: Boolean = false):
  AudioSource, AudioDecoderInterface, LoopFileInterface {

  private var callback: GetMicrophoneData? = null
  private var audioDecoder: AudioDecoder? = null
  private var running = false

  override fun setGetMicrophoneData(getMicrophoneData: GetMicrophoneData) {
    callback = getMicrophoneData
  }

  override fun setAudioInfo(sampleRate: Int, isStereo: Boolean) {

  }

  override fun prepare() {
    audioDecoder = AudioDecoder(callback, this, this)
    audioDecoder?.setLoopMode(loopMode)
    audioDecoder?.initExtractor(path)
    audioDecoder?.prepareAudio()
  }

  override fun start() {
    audioDecoder?.start()
    running = true
  }

  override fun stop() {
    audioDecoder?.stop()
    running = false
  }

  override fun isRunning(): Boolean {
    return running
  }

  override fun onAudioDecoderFinished() {
  }

  override fun onReset(isVideo: Boolean) {
    stop()
    prepare()
    start()
  }
}