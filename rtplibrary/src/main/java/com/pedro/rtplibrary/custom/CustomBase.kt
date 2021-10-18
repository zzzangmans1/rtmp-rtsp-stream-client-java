package com.pedro.rtplibrary.custom

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import androidx.annotation.RequiresApi
import com.pedro.encoder.audio.AudioEncoder
import com.pedro.encoder.audio.GetAacData
import com.pedro.encoder.video.GetVideoData
import com.pedro.encoder.video.VideoEncoder
import com.pedro.rtplibrary.custom.audio.AudioSource
import com.pedro.rtplibrary.custom.audio.NoAudioSource
import com.pedro.rtplibrary.custom.video.NoVideoSource
import com.pedro.rtplibrary.custom.video.VideoSource
import com.pedro.rtplibrary.view.GlInterface
import com.pedro.rtplibrary.view.OffScreenGlThread
import com.pedro.rtplibrary.view.OpenGlView
import java.nio.ByteBuffer

/**
 * Created by pedro on 18/10/21.
 * Customizable base to support Change Video and Audio source on fly.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class CustomBase: GetVideoData, GetAacData {

  private val videoEncoder = VideoEncoder(this)
  private val audioEncoder = AudioEncoder(this)
  private var videoSource: VideoSource = NoVideoSource()
  private var audioSource: AudioSource = NoAudioSource()
  private var glInterface: GlInterface? = null

  constructor(openGlView: OpenGlView, videoSource: VideoSource, audioSource: AudioSource) {
    this.glInterface = openGlView
    this.videoSource = videoSource
    this.audioSource = audioSource
  }

  constructor(context: Context, videoSource: VideoSource, audioSource: AudioSource) {
    glInterface = OffScreenGlThread(context)
    this.videoSource = videoSource
    this.audioSource = audioSource
  }

  fun changeVideoSource(videoSource: VideoSource) {
    var shouldStart = false
    if (this.videoSource.isRunning()) {
      shouldStart = true
      this.videoSource.stop()
    }
    if (videoSource.isRunning()) {
      videoSource.stop()
    }
    this.videoSource = videoSource
    if (shouldStart) {
      this.videoSource.prepare()
      this.videoSource.start()
    }
  }

  fun changeAudioSource(audioSource: AudioSource) {
    var shouldStart = false
    if (this.audioSource.isRunning()) {
      shouldStart = true
      this.audioSource.stop()
    }
    if (audioSource.isRunning()) {
      audioSource.stop()
    }
    this.audioSource = audioSource
    if (shouldStart) {
      this.audioSource.prepare()
      this.audioSource.start()
    }
  }

  override fun onSpsPpsVps(sps: ByteBuffer?, pps: ByteBuffer?, vps: ByteBuffer?) {
    TODO("Not yet implemented")
  }

  override fun getVideoData(h264Buffer: ByteBuffer?, info: MediaCodec.BufferInfo?) {
    TODO("Not yet implemented")
  }

  override fun onVideoFormat(mediaFormat: MediaFormat?) {
    TODO("Not yet implemented")
  }

  override fun getAacData(aacBuffer: ByteBuffer?, info: MediaCodec.BufferInfo?) {
    TODO("Not yet implemented")
  }

  override fun onAudioFormat(mediaFormat: MediaFormat?) {
    TODO("Not yet implemented")
  }
}