package com.github.faucamp.simplertmp.amf

import java.io.IOException
import java.io.InputStream

/**
 * @author francois
 */
internal object AmfDecoder {

  @JvmStatic
  @Throws(IOException::class)
  fun readFrom(input: InputStream): AmfData {
    val amfTypeByte = input.read().toByte()
    val amfType = AmfType.valueOf(amfTypeByte)
    val amfData: AmfData
    amfData = if (amfType != null) {
      when (amfType) {
        AmfType.NUMBER -> AmfNumber()
        AmfType.BOOLEAN -> AmfBoolean()
        AmfType.STRING -> AmfString()
        AmfType.OBJECT -> AmfObject()
        AmfType.NULL -> return AmfNull()
        AmfType.UNDEFINED -> return AmfUndefined()
        AmfType.ECMA_MAP -> AmfMap()
        AmfType.STRICT_ARRAY -> AmfArray()
        else -> throw IOException("Unknown/unimplemented AMF data type: $amfType")
      }
    } else {
      //If you can see -1 here it is because server close connection before library can read AMF packet.
      throw IOException("Unknown AMF data type: $amfTypeByte")
    }
    amfData.readFrom(input)
    return amfData
  }
}