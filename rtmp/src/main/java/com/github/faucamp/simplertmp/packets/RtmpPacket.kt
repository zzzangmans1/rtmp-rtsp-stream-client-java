package com.github.faucamp.simplertmp.packets

import com.github.faucamp.simplertmp.io.ChunkStreamInfo
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * @author francois, leo
 */
abstract class RtmpPacket(var header: RtmpHeader) {

  @Throws(IOException::class)
  abstract fun readBody(input: InputStream)

  @Throws(IOException::class)
  protected abstract fun writeBody(output: OutputStream)

  protected abstract fun array(): ByteArray

  protected abstract fun size(): Int

  @Throws(IOException::class)
  fun writeTo(out: OutputStream, chunkSize: Int, chunkStreamInfo: ChunkStreamInfo) {
    val baos = ByteArrayOutputStream()
    writeBody(baos)
    val body = (this as? ContentData)?.array() ?: baos.toByteArray()
    var length = (this as? ContentData)?.size() ?: body.size
    header.packetLength = length
    // Write header for first chunk
    header.writeTo(out, RtmpHeader.ChunkType.TYPE_0_FULL, chunkStreamInfo)
    var pos = 0
    while (length > chunkSize) {
      // Write packet for chunk
      out.write(body, pos, chunkSize)
      length -= chunkSize
      pos += chunkSize
      // Write header for remain chunk
      header.writeTo(out, RtmpHeader.ChunkType.TYPE_3_RELATIVE_SINGLE_BYTE, chunkStreamInfo)
    }
    out.write(body, pos, length)
  }
}