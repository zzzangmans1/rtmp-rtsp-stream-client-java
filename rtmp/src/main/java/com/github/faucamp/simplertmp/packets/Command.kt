package com.github.faucamp.simplertmp.packets

import com.github.faucamp.simplertmp.amf.AmfNumber
import com.github.faucamp.simplertmp.amf.AmfNumber.Companion.writeNumberTo
import com.github.faucamp.simplertmp.amf.AmfString
import com.github.faucamp.simplertmp.amf.AmfString.Companion.writeStringTo
import com.github.faucamp.simplertmp.io.ChunkStreamInfo
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Encapsulates an command/"invoke" RTMP packet
 *
 * Invoke/command packet structure (AMF encoded):
 * (String) <commmand name>
 * (Number) <Transaction ID>
 * (Mixed) <Argument> ex. Null, String, Object: {key1:value1, key2:value2 ... }
 *
 * @author francois
</Argument></Transaction></commmand> */
class Command : VariableBodyRtmpPacket {

  var commandName: String = ""
  var transactionId = 0

  constructor(header: RtmpHeader) : super(header)

  constructor(commandName: String, transactionId: Int, channelInfo: ChunkStreamInfo) : super(
      RtmpHeader(
          if (channelInfo.canReusePrevHeaderTx(RtmpHeader.MessageType.COMMAND_AMF0))
            RtmpHeader.ChunkType.TYPE_1_RELATIVE_LARGE
          else
            RtmpHeader.ChunkType.TYPE_0_FULL, ChunkStreamInfo.RTMP_CID_OVER_CONNECTION.toInt(), RtmpHeader.MessageType.COMMAND_AMF0
      )) {
    this.commandName = commandName
    this.transactionId = transactionId
  }

  constructor(commandName: String, transactionId: Int) : super(
      RtmpHeader(RtmpHeader.ChunkType.TYPE_0_FULL, ChunkStreamInfo.RTMP_CID_OVER_CONNECTION.toInt(),
          RtmpHeader.MessageType.COMMAND_AMF0)) {
    this.commandName = commandName
    this.transactionId = transactionId
  }

  @Throws(IOException::class)
  override fun readBody(input: InputStream) {
    readVariableData(input, 0)
    val amfString = data.removeAt(0) as AmfString
    commandName = amfString.value
    transactionId = if (data.isNotEmpty() && data[0] is AmfNumber) {
      val amfNumber = data.removeAt(0) as AmfNumber
      amfNumber.value.toInt()
    } else {
      0
    }
  }

  @Throws(IOException::class)
  override fun writeBody(output: OutputStream) {
    writeStringTo(output, commandName, false)
    writeNumberTo(output, transactionId.toDouble())
    // Write body data
    writeVariableData(output)
  }

  override fun array(): ByteArray = byteArrayOf()

  override fun size(): Int = 0

  override fun toString(): String {
    return "RTMP Command (command: $commandName, transaction ID: $transactionId)"
  }
}