package com.github.faucamp.simplertmp.amf

import com.github.faucamp.simplertmp.Util
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * AMF Array
 *
 * @author francois
 */
internal class AmfArray : AmfData {

  private var items: MutableList<AmfData> = ArrayList()
  private var size = -1

  /** @return the amount of items in this the array
   */
  val length: Int
    get() = items.size

  override fun writeTo(output: OutputStream) {
    throw UnsupportedOperationException("Not supported yet.")
  }

  @Throws(IOException::class)
  override fun readFrom(input: InputStream) {
    // Skip data type byte (we assume it's already read)
    val length = Util.readUnsignedInt32(input)
    size = 5 // 1 + 4
    items = ArrayList(length)
    for (i in 0 until length) {
      val dataItem = AmfDecoder.readFrom(input)
      size += dataItem.getSize()
      items.add(dataItem)
    }
  }

  override fun getSize(): Int {
    if (size == -1) {
      size = 5 // 1 + 4
      for (dataItem in items) {
        size += dataItem.getSize()
      }
    }
    return size
  }

  private fun getItems(): MutableList<AmfData> {
    return items
  }

  fun addItem(dataItem: AmfData?) {
    getItems().add(this)
  }
}