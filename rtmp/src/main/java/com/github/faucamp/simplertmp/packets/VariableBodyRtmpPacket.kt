package com.github.faucamp.simplertmp.packets

import com.github.faucamp.simplertmp.amf.*
import com.github.faucamp.simplertmp.amf.AmfDecoder.readFrom
import com.github.faucamp.simplertmp.amf.AmfNull.Companion.writeNullTo
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.collections.ArrayList

/**
 * RTMP packet with a "variable" body structure (i.e. the structure of the
 * body depends on some other state/parameter in the packet.
 *
 * Examples of this type of packet are Command and Data; this abstract class
 * exists mostly for code re-use.
 *
 * @author francois
 */
abstract class VariableBodyRtmpPacket(header: RtmpHeader) : RtmpPacket(header) {

  val data = ArrayList<AmfData>()

  fun addData(string: String) {
    addData(AmfString(string))
  }

  fun addData(number: Double) {
    addData(AmfNumber(number))
  }

  fun addData(bool: Boolean) {
    addData(AmfBoolean(bool))
  }

  fun addData(dataItem: AmfData?) {
    var item = dataItem
    if (item == null) {
      item = AmfNull()
    }
    data.add(item)
  }

  @Throws(IOException::class)
  protected fun readVariableData(input: InputStream, bytesAlreadyRead: Int) {
    // ...now read in arguments (if any)
    var readed = bytesAlreadyRead
    do {
      val dataItem = readFrom(input)
      addData(dataItem)
      readed += dataItem.getSize()
    } while (readed < header.packetLength)
  }

  @Throws(IOException::class)
  protected fun writeVariableData(output: OutputStream) {
    if (data.isNotEmpty()) {
      for (dataItem in data) {
        dataItem.writeTo(output)
      }
    } else {
      // Write a null
      writeNullTo(output)
    }
  }
}