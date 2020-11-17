package com.github.faucamp.simplertmp.io

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author francois
 */
internal class RtmpSessionInfo {
  /** The (total) number of bytes read for this window (resets to 0 if the agreed-upon RTMP window acknowledgement size is reached)  */
  private var windowBytesRead = 0

  /** The window acknowledgement size for this RTMP session, in bytes; default to max to avoid unnecessary "Acknowledgment" messages from being sent  */
  var acknowledgementWindowSize = Int.MAX_VALUE
    private set

  /** Used internally to store the total number of bytes read (used when sending Acknowledgement messages)  */
  private var totalBytesRead = 0

  /** Default chunk size is 128 bytes  */
  var rxChunkSize = 128
  var txChunkSize = 128
  private val chunkChannels: MutableMap<Int, ChunkStreamInfo> = HashMap()
  private val invokedMethods: MutableMap<Int, String> = ConcurrentHashMap()

  fun getChunkStreamInfo(chunkStreamId: Int): ChunkStreamInfo {
    var chunkStreamInfo = chunkChannels[chunkStreamId]
    if (chunkStreamInfo == null) {
      chunkStreamInfo = ChunkStreamInfo()
      chunkChannels[chunkStreamId] = chunkStreamInfo
    }
    return chunkStreamInfo
  }

  fun takeInvokedCommand(transactionId: Int): String? {
    return invokedMethods.remove(transactionId)
  }

  fun addInvokedCommand(transactionId: Int, commandName: String): String? {
    return invokedMethods.put(transactionId, commandName)
  }

  fun setAcknowledgmentWindowSize(acknowledgementWindowSize: Int) {
    this.acknowledgementWindowSize = acknowledgementWindowSize
  }

  fun reset() {
    windowBytesRead = 0
    acknowledgementWindowSize = Int.MAX_VALUE
    totalBytesRead = 0
    rxChunkSize = 128
    txChunkSize = 128
    chunkChannels.clear()
    invokedMethods.clear()
  }
}