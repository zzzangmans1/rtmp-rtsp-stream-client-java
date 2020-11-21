/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.faucamp.simplertmp.amf

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * @author francois
 */
internal class AmfNull : AmfData {

  companion object {
    @JvmStatic
    @Throws(IOException::class)
    fun writeNullTo(out: OutputStream) {
      out.write(AmfType.NULL.value.toInt())
    }
  }

  @Throws(IOException::class)
  override fun writeTo(output: OutputStream) {
    output.write(AmfType.NULL.value.toInt())
  }

  override fun readFrom(input: InputStream) {
    // Skip data type byte (we assume it's already read)
  }

  override fun getSize(): Int = 1
}