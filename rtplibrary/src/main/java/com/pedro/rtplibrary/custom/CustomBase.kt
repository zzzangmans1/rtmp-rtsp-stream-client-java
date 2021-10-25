package com.pedro.rtplibrary.custom

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import androidx.annotation.RequiresApi
import com.pedro.encoder.Frame
import com.pedro.encoder.audio.AudioEncoder
import com.pedro.encoder.audio.GetAacData
import com.pedro.encoder.input.audio.GetMicrophoneData
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.encoder.video.FormatVideoEncoder
import com.pedro.encoder.video.GetVideoData
import com.pedro.encoder.video.VideoEncoder
import com.pedro.rtplibrary.custom.audio.AudioSource
import com.pedro.rtplibrary.custom.audio.NoAudioSource
import com.pedro.rtplibrary.custom.video.Camera2Source
import com.pedro.rtplibrary.custom.video.DisplaySource
import com.pedro.rtplibrary.custom.video.NoVideoSource
import com.pedro.rtplibrary.custom.video.VideoSource
import com.pedro.rtplibrary.util.FpsListener
import com.pedro.rtplibrary.util.RecordController
import com.pedro.rtplibrary.view.GlInterface
import com.pedro.rtplibrary.view.OffScreenGlThread
import com.pedro.rtplibrary.view.OpenGlView
import java.nio.ByteBuffer

/**
 * Created by pedro on 18/10/21.
 * Customizable base to support Change Video and Audio source on fly.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
abstract class CustomBase: GetVideoData, GetAacData, GetMicrophoneData {

  private val videoEncoder = VideoEncoder(this)
  private val audioEncoder = AudioEncoder(this)
  private var videoSource: VideoSource = NoVideoSource()
  private var audioSource: AudioSource = NoAudioSource()
  private val context: Context
  private var glInterface: GlInterface? = null
  var streaming = false
  var onPreview = false
  private var recordController: RecordController? = null
  private val fpsListener = FpsListener()

  constructor(openGlView: OpenGlView, videoSource: VideoSource, audioSource: AudioSource) {
    this.glInterface = openGlView
    this.videoSource = videoSource
    this.audioSource = audioSource
    context = openGlView.context
  }

  constructor(context: Context, videoSource: VideoSource, audioSource: AudioSource) {
    glInterface = OffScreenGlThread(context)
    this.videoSource = videoSource
    this.audioSource = audioSource
    this.context = context
  }

  fun changeVideoSource(videoSource: VideoSource) {
    if (streaming) {
      glInterface?.removeMediaCodecSurface()
      glInterface?.stop()
    }
    var shouldStart = false
    if (this.videoSource.isRunning()) {
      shouldStart = true
      this.videoSource.stop()
    }
    if (videoSource.isRunning()) {
      videoSource.stop()
    }
    videoSource.setVideoInfo(videoEncoder.width, videoEncoder.height, videoEncoder.fps)
    glInterface?.surfaceTexture?.let {
      videoSource.setSurfaceTexture(it)
    }
    if (streaming) {
      prepareGlInterface()
      glInterface?.surfaceTexture?.let {
        videoSource.setSurfaceTexture(it)
      }
      glInterface?.addMediaCodecSurface(videoEncoder.inputSurface)
    }
    if (shouldStart) {
      videoSource.prepare()
      videoSource.start()
    }
    this.videoSource = videoSource
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

  @JvmOverloads
  fun prepareVideo(width: Int = 640, height: Int = 480, fps: Int = 30, bitrate: Int = 1200 * 1000,
    rotation: Int = CameraHelper.getCameraOrientation(context), iFrameInterval: Int = 2): Boolean {
    videoSource.setVideoInfo(videoEncoder.width, videoEncoder.height, videoEncoder.fps)
    return videoEncoder.prepareVideoEncoder(width, height, fps, bitrate, rotation, iFrameInterval,
      FormatVideoEncoder.SURFACE)
  }

  @JvmOverloads
  fun prepareAudio(sampleRate: Int = 32000, isStereo: Boolean = true,
    bitrate: Int = 64 * 1000): Boolean {
    prepareAudioRtp(isStereo, sampleRate)
    return audioEncoder.prepareAudioEncoder(bitrate, sampleRate, isStereo, 0)
  }

  fun startStream(url: String) {
    if (!streaming) {
      videoEncoder.start()
      audioEncoder.start()
      glInterface?.removeMediaCodecSurface()
      glInterface?.stop()
      prepareGlInterface()
      // Link videoEncoder to videoSource to start produce video frames
      glInterface?.addMediaCodecSurface(videoEncoder.inputSurface)
      if (!onPreview) videoSource.start()
      audioSource.start()
      startStreamRtp(url)
      onPreview = true
      streaming = true
    }
  }

  fun stopStream() {
    if (streaming) {
      streaming = false
      stopStreamRtp()
      glInterface?.removeMediaCodecSurface()
      audioSource.stop()
      videoEncoder.stop()
      audioEncoder.stop()
    }
  }

  fun startPreview() {
    if (!videoSource.isRunning() && !streaming) {
      prepareGlInterface()
      videoSource.prepare()
      videoSource.start()
      onPreview = true
    }
  }

  fun stopPreview() {
    if (videoSource.isRunning() && !streaming) {
      glInterface?.stop()
      videoSource.stop()
      onPreview = false
    }
  }

  fun replaceGlInterface(glInterface: GlInterface) {
    this.glInterface = glInterface
  }

    /**
   * @param callback get fps while record or stream
   */
  fun setFpsListener(callback: FpsListener.Callback?) {
    fpsListener.setCallback(callback)
  }

  // Link videoSource to preview and start preview
  private fun prepareGlInterface() {
    glInterface?.init()
    val rotation = videoEncoder.rotation
    if (rotation == 90 || rotation == 270) {
      glInterface?.setEncoderSize(videoEncoder.height, videoEncoder.width)
    } else {
      glInterface?.setEncoderSize(videoEncoder.width, videoEncoder.height)
    }
    glInterface?.setRotation(rotation)
    glInterface?.start()
    var shouldReset = videoSource.isRunning()
    // Display mode can't be reset because you need ask permissions again
    if (videoSource is DisplaySource) shouldReset = false
    if (shouldReset) videoSource.stop()
    glInterface?.surfaceTexture?.let {
      videoSource.setSurfaceTexture(it)
    }
    if (shouldReset) {
      videoSource.prepare()
      videoSource.start()
    }
  }

  protected abstract fun prepareAudioRtp(isStereo: Boolean, sampleRate: Int)

  protected abstract fun startStreamRtp(url: String?)

  protected abstract fun stopStreamRtp()

  protected abstract fun onSpsPpsVpsRtp(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?)

  protected abstract fun getH264DataRtp(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo)

  protected abstract fun getAacDataRtp(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo)

  override fun inputPCMData(frame: Frame?) {
    audioEncoder.inputPCMData(frame)
  }

  override fun onSpsPpsVps(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?) {
    onSpsPpsVpsRtp(sps, pps, vps)
  }

  override fun getVideoData(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    fpsListener.calculateFps()
    if (streaming) getH264DataRtp(h264Buffer, info)
  }

  override fun onVideoFormat(mediaFormat: MediaFormat?) {
    recordController?.setVideoFormat(mediaFormat)
  }

  override fun getAacData(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    if (streaming) getAacDataRtp(aacBuffer, info)
  }

  override fun onAudioFormat(mediaFormat: MediaFormat?) {
    recordController?.setAudioFormat(mediaFormat)
  }
}