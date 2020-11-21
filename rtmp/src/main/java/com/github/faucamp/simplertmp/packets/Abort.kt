package com.github.faucamp.simplertmp.packets

import com.github.faucamp.simplertmp.Util
import com.github.faucamp.simplertmp.io.ChunkStreamInfo
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * A "Abort" RTMP control message, received on chunk stream ID 2 (control channel)
 *
 * @author francois
 */
class Abort : RtmpPacket {
  /** @return the ID of the chunk stream to be aborted
   */
  /** Sets the ID of the chunk stream to be aborted  */
  var chunkStreamId = 0

  constructor(header: RtmpHeader) : super(header)

  constructor(chunkStreamId: Int) : super(RtmpHeader(RtmpHeader.ChunkType.TYPE_1_RELATIVE_LARGE,
      ChunkStreamInfo.RTMP_CID_PROTOCOL_CONTROL.toInt(), RtmpHeader.MessageType.SET_CHUNK_SIZE)) {
    this.chunkStreamId = chunkStreamId
  }

  @Throws(IOException::class)
  override fun readBody(input: InputStream) {
    // Value is received in the 4 bytes of the body
    chunkStreamId = Util.readUnsignedInt32(input)
  }

  override fun array(): ByteArray = byteArrayOf()

  override fun size(): Int = 0

  @Throws(IOException::class)
  override fun writeBody(output: OutputStream) {
    Util.writeUnsignedInt32(output, chunkStreamId)
  }
}