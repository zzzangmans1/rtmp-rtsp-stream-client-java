package com.pedro.rtplibrary.rtmp

import android.content.Context
import android.media.MediaCodec
import android.os.Build
import androidx.annotation.RequiresApi
import com.pedro.rtmp.rtmp.RtmpClient
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.custom.CustomBase
import com.pedro.rtplibrary.custom.audio.AudioSource
import com.pedro.rtplibrary.custom.video.VideoSource
import com.pedro.rtplibrary.view.OpenGlView
import java.nio.ByteBuffer

/**
 * Created by pedro on 25/10/21.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class RtmpCustom: CustomBase {

  private var rtmpClient: RtmpClient? = null

  constructor(openGlView: OpenGlView, videoSource: VideoSource, audioSource: AudioSource,
    connectCheckerRtmp: ConnectCheckerRtmp): super(openGlView, videoSource, audioSource) {
    rtmpClient = RtmpClient(connectCheckerRtmp)
  }

  constructor(context: Context, videoSource: VideoSource, audioSource: AudioSource,
    connectCheckerRtmp: ConnectCheckerRtmp) : super(context, videoSource, audioSource) {
    rtmpClient = RtmpClient(connectCheckerRtmp)
  }

  override fun prepareAudioRtp(isStereo: Boolean, sampleRate: Int) {
    rtmpClient?.setAudioInfo(sampleRate, isStereo)
  }

  override fun startStreamRtp(url: String?) {
    rtmpClient?.connect(url)
  }

  override fun stopStreamRtp() {
    rtmpClient?.disconnect()
  }

  override fun onSpsPpsVpsRtp(sps: ByteBuffer, pps: ByteBuffer, vps: ByteBuffer?) {
    rtmpClient?.setVideoInfo(sps, pps, vps)
  }

  override fun getH264DataRtp(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    rtmpClient?.sendVideo(h264Buffer, info)
  }

  override fun getAacDataRtp(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
    rtmpClient?.sendAudio(aacBuffer, info)
  }
}