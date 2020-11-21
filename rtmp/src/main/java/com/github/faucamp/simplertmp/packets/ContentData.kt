package com.github.faucamp.simplertmp.packets

import com.github.faucamp.simplertmp.Util
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Content (audio/video) data packet base
 *
 * @author francois
 */
abstract class ContentData(header: RtmpHeader) : RtmpPacket(header) {

  var data: ByteArray = byteArrayOf()
    protected set
  protected var size = 0

  fun setData(data: ByteArray, size: Int) {
    this.data = data
    this.size = size
  }

  @Throws(IOException::class)
  override fun readBody(input: InputStream) {
    data = ByteArray(header.packetLength)
    Util.readBytesUntilFull(input, data)
  }

  /**
   * Method is public for content (audio/video)
   * Write this packet body without chunking;
   * useful for dumping audio/video streams
   */
  override fun writeBody(output: OutputStream) {}

  public override fun array(): ByteArray = data

  public override fun size(): Int = size
}