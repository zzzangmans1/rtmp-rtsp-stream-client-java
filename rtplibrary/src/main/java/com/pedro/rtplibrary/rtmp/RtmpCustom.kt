package com.pedro.rtplibrary.rtmp

import android.content.Context
import android.media.MediaCodec
import android.os.Build
import androidx.annotation.RequiresApi
import com.pedro.rtmp.flv.video.ProfileIop
import com.pedro.rtmp.rtmp.RtmpClient
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.custom.CustomBase
import com.pedro.rtplibrary.custom.audio.AudioSource
import com.pedro.rtplibrary.custom.video.VideoSource
import com.pedro.rtplibrary.view.OpenGlView
import java.lang.RuntimeException
import java.nio.ByteBuffer

/**
 * Created by pedro on 25/10/21.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class RtmpCustom: CustomBase {

  private val rtmpClient: RtmpClient

  constructor(openGlView: OpenGlView, videoSource: VideoSource, audioSource: AudioSource,
    connectCheckerRtmp: ConnectCheckerRtmp): super(openGlView, videoSource, audioSource) {
    rtmpClient = RtmpClient(connectCheckerRtmp)
  }

  constructor(context: Context, videoSource: VideoSource, audioSource: AudioSource,
    connectCheckerRtmp: ConnectCheckerRtmp) : super(context, videoSource, audioSource) {
    rtmpClient = RtmpClient(connectCheckerRtmp)
  }

  /**
   * H264 profile.
   *
   * @param profileIop Could be ProfileIop.BASELINE or ProfileIop.CONSTRAINED
   */
  fun setProfileIop(profileIop: ProfileIop?) {
    rtmpClient.setProfileIop(profileIop!!)
  }

  @Throws(RuntimeException::class)
  override fun resizeCache(newSize: Int) {
    rtmpClient.resizeCache(newSize)
  }

  override fun getCacheSize(): Int {
    return rtmpClient.cacheSize
  }

  override fun getSentAudioFrames(): Long {
    return rtmpClient.sentAudioFrames
  }

  override fun getSentVideoFrames(): Long {
    return rtmpClient.sentVideoFrames
  }

  override fun getDroppedAudioFrames(): Long {
    return rtmpClient.droppedAudioFrames
  }

  override fun getDroppedVideoFrames(): Long {
    return rtmpClient.droppedVideoFrames
  }

  override fun resetSentAudioFrames() {
    rtmpClient.resetSentAudioFrames()
  }

  override fun resetSentVideoFrames() {
    rtmpClient.resetSentVideoFrames()
  }

  override fun resetDroppedAudioFrames() {
    rtmpClient.resetDroppedAudioFrames()
  }

  override fun resetDroppedVideoFrames() {
    rtmpClient.resetDroppedVideoFrames()
  }

  override fun setAuthorization(user: String?, password: String?) {
    rtmpClient.setAuthorization(user, password)
  }

  /**
   * Some Livestream hosts use Akamai auth that requires RTMP packets to be sent with increasing
   * timestamp order regardless of packet type.
   * Necessary with Servers like Dacast.
   * More info here:
   * https://learn.akamai.com/en-us/webhelp/media-services-live/media-services-live-encoder-compatibility-testing-and-qualification-guide-v4.0/GUID-F941C88B-9128-4BF4-A81B-C2E5CFD35BBF.html
   */
  fun forceAkamaiTs(enabled: Boolean) {
    rtmpClient.forceAkamaiTs(enabled)
  }

  /**
   * Must be called before start stream.
   *
   * Default value 128
   * Range value: 1 to 16777215.
   *
   * The most common values example: 128, 4096, 65535
   *
   * @param chunkSize packet's chunk size send to server
   */
  fun setWriteChunkSize(chunkSize: Int) {
    if (!streaming) {
      rtmpClient.setWriteChunkSize(chunkSize)
    }
  }

  override fun setReTries(reTries: Int) {
    rtmpClient.setReTries(reTries)
  }

  override fun shouldRetry(reason: String?): Boolean {
    return rtmpClient.shouldRetry(reason!!)
  }

  override fun reConnect(delay: Long, backupUrl: String?) {
    rtmpClient.reConnect(delay, backupUrl)
  }

  override fun hasCongestion(): Boolean {
    return rtmpClient.hasCongestion()
  }

  override fun setLogs(enable: Boolean) {
    rtmpClient.setLogs(enable)
  }

  override fun prepareAudioRtp(isStereo: Boolean, sampleRate: Int) {
    rtmpClient.setAudioInfo(sampleRate, isStereo)
  }

  override fun startStreamRtp(url: String?) {
    rtmpClient.connect(url)
  }

  override fun stopStreamRtp() {
    rtmpClient.disconnect()
  }

  override fun onSpsPpsVpsRtp(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?) {
    rtmpClient.setVideoInfo(sps, pps, vps)
  }

  override fun getH264DataRtp(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    rtmpClient.sendVideo(h264Buffer, info)
  }

  override fun getAacDataRtp(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    rtmpClient.sendAudio(aacBuffer, info)
  }
}