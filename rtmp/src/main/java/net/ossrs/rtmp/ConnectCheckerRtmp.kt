package net.ossrs.rtmp

/**
 * Created by pedro on 25/01/17.
 */
interface ConnectCheckerRtmp {
  fun onConnectionSuccessRtmp()
  fun onConnectionFailedRtmp(reason: String)
  fun onNewBitrateRtmp(bitrate: Long)
  fun onDisconnectRtmp()
  fun onAuthErrorRtmp()
  fun onAuthSuccessRtmp()
}