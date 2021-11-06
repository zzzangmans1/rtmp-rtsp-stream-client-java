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
import com.pedro.encoder.utils.CodecUtil
import com.pedro.encoder.video.FormatVideoEncoder
import com.pedro.encoder.video.GetVideoData
import com.pedro.encoder.video.VideoEncoder
import com.pedro.rtplibrary.custom.audio.AudioSource
import com.pedro.rtplibrary.custom.audio.NoAudioSource
import com.pedro.rtplibrary.custom.video.DisplaySource
import com.pedro.rtplibrary.custom.video.NoVideoSource
import com.pedro.rtplibrary.custom.video.VideoSource
import com.pedro.rtplibrary.util.FpsListener
import com.pedro.rtplibrary.util.RecordController
import com.pedro.rtplibrary.view.GlInterface
import com.pedro.rtplibrary.view.OffScreenGlThread
import com.pedro.rtplibrary.view.OpenGlView
import java.io.IOException
import java.lang.RuntimeException
import java.nio.ByteBuffer

/**
 * Created by pedro on 18/10/21.
 * Customizable base to support Change Video and Audio source on fly.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
abstract class CustomBase: GetVideoData, GetAacData, GetMicrophoneData {

  private val videoEncoder by lazy {  VideoEncoder(this) }
  private val audioEncoder by lazy { AudioEncoder(this) }
  private var videoSource: VideoSource = NoVideoSource()
  private var audioSource: AudioSource = NoAudioSource()
  private val context: Context
  private var glInterface: GlInterface
  var streaming = false
  var onPreview = false
  private val recordController = RecordController()
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
      glInterface.removeMediaCodecSurface()
      glInterface.stop()
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
    glInterface.surfaceTexture?.let {
      videoSource.setSurfaceTexture(it)
    }
    if (streaming) {
      prepareGlInterface()
      glInterface.surfaceTexture?.let {
        videoSource.setSurfaceTexture(it)
      }
      glInterface.addMediaCodecSurface(videoEncoder.inputSurface)
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
    audioSource.setGetMicrophoneData(this)
    audioSource.setAudioInfo(audioEncoder.sampleRate, audioEncoder.isStereo)
    if (shouldStart) {
      audioSource.prepare()
      audioSource.start()
    }
    this.audioSource = audioSource
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
    return audioEncoder.prepareAudioEncoder(bitrate, sampleRate, isStereo, 4096)
  }

  fun startStream(url: String) {
    if (!streaming) {
      onPreview = true
      streaming = true
      if (!recordController.isRunning) {
        startEncoders()
      } else {
        requestKeyFrame()
      }
      startStreamRtp(url)
    }
  }

  fun stopStream() {
    if (streaming) {
      streaming = false
      stopStreamRtp()
    }
    if (!recordController.isRecording) {
      glInterface.removeMediaCodecSurface()
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
      glInterface.stop()
      videoSource.stop()
      onPreview = false
    }
  }

    /**
   * @param callback get fps while record or stream
   */
  fun setFpsListener(callback: FpsListener.Callback?) {
    fpsListener.setCallback(callback)
  }

  private fun startEncoders() {
    videoEncoder.start()
    audioEncoder.start()
    glInterface.removeMediaCodecSurface()
    glInterface.stop()
    prepareGlInterface()
    // Link videoEncoder to videoSource to start produce video frames
    glInterface.addMediaCodecSurface(videoEncoder.inputSurface)
    if (!onPreview) videoSource.start()
    audioSource.start()
  }

  // Link videoSource to preview and start preview
  private fun prepareGlInterface() {
    glInterface.init()
    val rotation = videoEncoder.rotation
    if (rotation == 90 || rotation == 270) {
      glInterface.setEncoderSize(videoEncoder.height, videoEncoder.width)
    } else {
      glInterface.setEncoderSize(videoEncoder.width, videoEncoder.height)
    }
    glInterface.setRotation(rotation)
    glInterface.start()
    var shouldReset = videoSource.isRunning()
    // Display mode can't be reset because you need ask permissions again
    if (videoSource is DisplaySource) shouldReset = false
    if (shouldReset) videoSource.stop()
    glInterface.surfaceTexture?.let {
      videoSource.setSurfaceTexture(it)
    }
    if (shouldReset) {
      videoSource.prepare()
      videoSource.start()
    }
  }

  /**
   * Starts recording an MP4 video. Needs to be called while streaming.
   *
   * @param path Where file will be saved.
   * @throws IOException If initialized before a stream.
   */
  @Throws(IOException::class)
  fun startRecord(path: String, listener: RecordController.Listener? = null) {
    recordController.startRecord(path, listener)
    if (!streaming) {
      startEncoders()
    } else if (videoEncoder.isRunning) {
      requestKeyFrame()
    }
  }

  /**
   * Stop record MP4 video started with @startRecord. If you don't call it file will be unreadable.
   */
  fun stopRecord() {
    recordController.stopRecord()
    if (!streaming) stopStream()
  }

  fun replaceView(context: Context) {
    replaceGlInterface(OffScreenGlThread(context))
  }

  fun replaceView(openGlView: OpenGlView) {
    replaceGlInterface(openGlView)
  }

  /**
   * Replace glInterface used on fly. Ignored if you use SurfaceView or TextureView
   */
  private fun replaceGlInterface(glInterface: GlInterface) {
    if (streaming || isRecording() || onPreview) {
      videoSource.stop()
      this.glInterface.removeMediaCodecSurface()
      this.glInterface.stop()
      this.glInterface = glInterface
      this.glInterface.init()
      val isPortrait = CameraHelper.isPortrait(context)
      if (isPortrait) {
        this.glInterface.setEncoderSize(videoEncoder.height, videoEncoder.width)
      } else {
        this.glInterface.setEncoderSize(videoEncoder.width, videoEncoder.height)
      }
      this.glInterface.setRotation(0)
      this.glInterface.start()
      if (streaming || isRecording()) {
        this.glInterface.addMediaCodecSurface(videoEncoder.inputSurface)
      }
      videoSource.setSurfaceTexture(glInterface.surfaceTexture)
      videoSource.prepare()
      videoSource.start()
    } else {
      this.glInterface = glInterface
      this.glInterface.init()
    }
  }

  /**
   * @param forceVideo force type codec used. FIRST_COMPATIBLE_FOUND, SOFTWARE, HARDWARE
   * @param forceAudio force type codec used. FIRST_COMPATIBLE_FOUND, SOFTWARE, HARDWARE
   */
  fun setForce(forceVideo: CodecUtil.Force?, forceAudio: CodecUtil.Force?) {
    videoEncoder.setForce(forceVideo)
    audioEncoder.setForce(forceAudio)
  }

  protected fun requestKeyFrame() {
    videoEncoder.requestKeyframe()
  }

  /**
   * Get record state.
   *
   * @return true if recording, false if not recoding.
   */
  fun isRecording(): Boolean {
    return recordController.isRunning
  }

  /**
   * Retries to connect with the given delay. You can pass an optional backupUrl
   * if you'd like to connect to your backup server instead of the original one.
   * Given backupUrl replaces the original one.
   */
  @JvmOverloads
  fun reTry(delay: Long, reason: String?, backupUrl: String? = null): Boolean {
    val result = shouldRetry(reason)
    if (result) {
      requestKeyFrame()
      reConnect(delay, backupUrl)
    }
    return result
  }

  /**
   * @param user auth.
   * @param password auth.
   */
  abstract fun setAuthorization(user: String?, password: String?)

  protected abstract fun shouldRetry(reason: String?): Boolean

  abstract fun setReTries(reTries: Int)

  protected abstract fun reConnect(delay: Long, backupUrl: String?)

  //cache control
  abstract fun hasCongestion(): Boolean

  @Throws(RuntimeException::class)
  abstract fun resizeCache(newSize: Int)

  abstract fun getCacheSize(): Int

  abstract fun getSentAudioFrames(): Long

  abstract fun getSentVideoFrames(): Long

  abstract fun getDroppedAudioFrames(): Long

  abstract fun getDroppedVideoFrames(): Long

  abstract fun resetSentAudioFrames()

  abstract fun resetSentVideoFrames()

  abstract fun resetDroppedAudioFrames()

  abstract fun resetDroppedVideoFrames()

  abstract fun setLogs(enable: Boolean)

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
    recordController.recordVideo(h264Buffer, info)
    if (streaming) getH264DataRtp(h264Buffer, info)
  }

  override fun onVideoFormat(mediaFormat: MediaFormat?) {
    recordController.setVideoFormat(mediaFormat)
  }

  override fun getAacData(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    recordController.recordAudio(aacBuffer, info)
    if (streaming) getAacDataRtp(aacBuffer, info)
  }

  override fun onAudioFormat(mediaFormat: MediaFormat?) {
    recordController.setAudioFormat(mediaFormat)
  }
}