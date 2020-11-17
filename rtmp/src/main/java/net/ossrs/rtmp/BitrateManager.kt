package net.ossrs.rtmp

/**
 * Created by pedro on 10/07/19.
 *
 * Calculate video and audio bitrate per second
 */
internal class BitrateManager(private val connectCheckerRtmp: ConnectCheckerRtmp) {

  private var bitrate: Long = 0
  private var timeStamp = System.currentTimeMillis()

  @Synchronized
  fun calculateBitrate(size: Long) {
    bitrate += size
    val timeDiff = System.currentTimeMillis() - timeStamp
    if (timeDiff >= 1000) {
      connectCheckerRtmp.onNewBitrateRtmp((bitrate / (timeDiff / 1000f)).toLong())
      timeStamp = System.currentTimeMillis()
      bitrate = 0
    }
  }
}