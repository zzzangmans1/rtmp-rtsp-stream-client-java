package com.github.faucamp.simplertmp.amf

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * @author francois
 */
internal class AmfBoolean @JvmOverloads constructor(var isValue: Boolean = false) : AmfData {

  companion object {
    // Skip data type byte (we assume it's already read)
    @Throws(IOException::class)
    fun readBooleanFrom(input: InputStream): Boolean = input.read() == 0x01
  }

  @Throws(IOException::class)
  override fun writeTo(output: OutputStream) {
    output.write(AmfType.BOOLEAN.value.toInt())
    output.write(if (isValue) 0x01 else 0x00)
  }

  @Throws(IOException::class)
  override fun readFrom(input: InputStream) {
    isValue = input.read() == 0x01
  }

  override fun getSize(): Int = 2
}