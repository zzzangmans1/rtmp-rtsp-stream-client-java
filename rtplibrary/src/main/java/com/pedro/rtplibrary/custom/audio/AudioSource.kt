package com.pedro.rtplibrary.custom.audio

/**
 * Created by pedro on 18/10/21.
 */
interface AudioSource {

  fun prepare()

  fun start()

  fun stop()

  fun isRunning(): Boolean
}