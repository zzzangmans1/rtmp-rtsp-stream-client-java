package com.pedro.rtplibrary.custom.audio

import com.pedro.encoder.input.audio.GetMicrophoneData

/**
 * Created by pedro on 18/10/21.
 */
interface AudioSource {

  fun setGetMicrophoneData(getMicrophoneData: GetMicrophoneData)

  fun setAudioInfo(sampleRate: Int, isStereo: Boolean)

  fun prepare()

  fun start()

  fun stop()

  fun isRunning(): Boolean
}