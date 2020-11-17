package com.github.faucamp.simplertmp

import com.github.faucamp.simplertmp.io.RtmpConnection
import net.ossrs.rtmp.ConnectCheckerRtmp

/**
 * Srs implementation of an RTMP publisher
 *
 * @author francois, leoma, pedro
 */
internal class DefaultRtmpPublisher(connectCheckerRtmp: ConnectCheckerRtmp?) : RtmpPublisher {

  private val rtmpConnection: RtmpConnection = RtmpConnection(connectCheckerRtmp)

  override fun connect(url: String?): Boolean {
    return rtmpConnection.connect(url)
  }

  override fun publish(publishType: String?): Boolean {
    return rtmpConnection.publish(publishType)
  }

  override fun close() {
    rtmpConnection.close()
  }

  override fun publishVideoData(data: ByteArray?, size: Int, dts: Int) {
    rtmpConnection.publishVideoData(data, size, dts)
  }

  override fun publishAudioData(data: ByteArray?, size: Int, dts: Int) {
    rtmpConnection.publishAudioData(data, size, dts)
  }

  override fun setVideoResolution(width: Int, height: Int) {
    rtmpConnection.setVideoResolution(width, height)
  }

  override fun setAuthorization(user: String?, password: String?) {
    rtmpConnection.setAuthorization(user, password)
  }

  override fun setLogs(enable: Boolean) {
    rtmpConnection.setLogs(enable)
  }
}