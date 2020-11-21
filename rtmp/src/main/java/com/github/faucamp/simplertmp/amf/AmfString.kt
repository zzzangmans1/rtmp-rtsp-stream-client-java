package com.github.faucamp.simplertmp.amf

import android.util.Log
import com.github.faucamp.simplertmp.Util
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException

/**
 * @author francois
 */
class AmfString @JvmOverloads constructor(var value: String = "", var isKey: Boolean = false) : AmfData {

  private var size = -1

  @Throws(IOException::class)
  override fun writeTo(output: OutputStream) {
    // Strings are ASCII encoded
    val byteValue = value.toByteArray(charset("ASCII"))
    // Write the STRING data type definition (except if this String is used as a key)
    if (!isKey) {
      output.write(AmfType.STRING.value.toInt())
    }
    // Write 2 bytes indicating string length
    Util.writeUnsignedInt16(output, byteValue.size)
    // Write string
    output.write(byteValue)
  }

  @Throws(IOException::class)
  override fun readFrom(input: InputStream) {
    // Skip data type byte (we assume it's already read)
    val length = Util.readUnsignedInt16(input)
    size = 3 + length // 1 + 2 + length
    // Read string value
    val byteValue = ByteArray(length)
    Util.readBytesUntilFull(input, byteValue)
    value = String(byteValue, charset("ASCII"))
  }

  override fun getSize(): Int {
    if (size == -1) {
      size = try {
        (if (isKey) 0 else 1) + 2 + value.toByteArray(charset("ASCII")).size
      } catch (ex: UnsupportedEncodingException) {
        Log.e(TAG, "AmfString.getSize(): caught exception", ex)
        throw RuntimeException(ex)
      }
    }
    return size
  }

  companion object {

    private const val TAG = "AmfString"

    @Throws(IOException::class)
    fun readStringFrom(input: InputStream, isKey: Boolean): String {
      if (!isKey) {
        // Read past the data type byte
        input.read()
      }
      val length = Util.readUnsignedInt16(input)
      // Read string value
      val byteValue = ByteArray(length)
      Util.readBytesUntilFull(input, byteValue)
      return String(byteValue, charset("ASCII"))
    }

    @Throws(IOException::class)
    fun writeStringTo(output: OutputStream, string: String, isKey: Boolean) {
      // Strings are ASCII encoded
      val byteValue = string.toByteArray(charset("ASCII"))
      // Write the STRING data type definition (except if this String is used as a key)
      if (!isKey) {
        output.write(AmfType.STRING.value.toInt())
      }
      // Write 2 bytes indicating string length
      Util.writeUnsignedInt16(output, byteValue.size)
      // Write string
      output.write(byteValue)
    }

    /** @return the byte size of the resulting AMF string of the specified value
     */
    fun sizeOf(string: String, isKey: Boolean): Int {
      return try {
        (if (isKey) 0 else 1) + 2 + string.toByteArray(charset("ASCII")).size
      } catch (ex: UnsupportedEncodingException) {
        Log.e(TAG, "AmfString.SizeOf(): caught exception", ex)
        throw RuntimeException(ex)
      }
    }
  }
}