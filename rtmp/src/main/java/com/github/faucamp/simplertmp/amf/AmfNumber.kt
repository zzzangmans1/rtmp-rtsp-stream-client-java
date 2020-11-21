package com.github.faucamp.simplertmp.amf

import com.github.faucamp.simplertmp.Util
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * AMF0 Number data type
 *
 * @author francois
 */
internal class AmfNumber @JvmOverloads constructor(var value: Double = 0.0) : AmfData {

  companion object {
    /** Size of an AMF number, in bytes (including type bit)  */
    const val SIZE = 9
    @JvmStatic
    @Throws(IOException::class)
    fun readNumberFrom(input: InputStream): Double {
      // Skip data type byte
      input.read()
      return Util.readDouble(input)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun writeNumberTo(out: OutputStream, number: Double) {
      out.write(AmfType.NUMBER.value.toInt())
      Util.writeDouble(out, number)
    }
  }

  @Throws(IOException::class)
  override fun writeTo(output: OutputStream) {
    output.write(AmfType.NUMBER.value.toInt())
    Util.writeDouble(output, value)
  }

  @Throws(IOException::class)
  override fun readFrom(input: InputStream) {
    // Skip data type byte (we assume it's already read)
    value = Util.readDouble(input)
  }

  override fun getSize(): Int = SIZE
}