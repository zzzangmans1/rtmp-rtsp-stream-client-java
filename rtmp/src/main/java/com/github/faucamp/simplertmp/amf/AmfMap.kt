package com.github.faucamp.simplertmp.amf

import com.github.faucamp.simplertmp.Util
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * AMF map; that is, an "object"-like structure of key/value pairs, but with
 * an array-like size indicator at the start (which is seemingly always 0)
 *
 * @author francois
 */
internal class AmfMap : AmfObject() {

  @Throws(IOException::class)
  override fun writeTo(output: OutputStream) {
    // Begin the map/object/array/whatever exactly this is
    output.write(AmfType.ECMA_MAP.value.toInt())

    // Write the "array size"
    Util.writeUnsignedInt32(output, properties.size)

    // Write key/value pairs in this object
    for ((key, value) in properties) {
      // The key must be a STRING type, and thus the "type-definition" byte is implied (not included in message)
      AmfString.writeStringTo(output, key, true)
      value.writeTo(output)
    }

    // End the object
    output.write(OBJECT_END_MARKER)
  }

  @Throws(IOException::class)
  override fun readFrom(input: InputStream) {
    // Skip data type byte (we assume it's already read)
    val length = Util.readUnsignedInt32(input) // Seems this is always 0
    super.readFrom(input)
    mSize += 4 // Add the bytes read for parsing the array size (length)
  }

//  override fun getSize(): Int {
//    if (size == -1) {
//      size = super.getSize()
//      size += 4 // array length bytes
//    }
//    return size
//  }
}