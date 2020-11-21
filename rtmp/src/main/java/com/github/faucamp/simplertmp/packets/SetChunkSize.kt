package com.github.faucamp.simplertmp.packets

import com.github.faucamp.simplertmp.Util
import com.github.faucamp.simplertmp.io.ChunkStreamInfo
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * A "Set chunk size" RTMP message, received on chunk stream ID 2 (control channel)
 *
 * @author francois
 */
class SetChunkSize : RtmpPacket {

  var chunkSize = 0

  constructor(header: RtmpHeader) : super(header)

  constructor(chunkSize: Int) : super(RtmpHeader(RtmpHeader.ChunkType.TYPE_1_RELATIVE_LARGE,
      ChunkStreamInfo.RTMP_CID_PROTOCOL_CONTROL.toInt(), RtmpHeader.MessageType.SET_CHUNK_SIZE)) {
    this.chunkSize = chunkSize
  }

  @Throws(IOException::class)
  override fun readBody(input: InputStream) {
    // Value is received in the 4 bytes of the body
    chunkSize = Util.readUnsignedInt32(input)
  }

  @Throws(IOException::class)
  override fun writeBody(output: OutputStream) {
    Util.writeUnsignedInt32(output, chunkSize)
  }

  override fun array(): ByteArray = byteArrayOf()

  override fun size(): Int = 0
}