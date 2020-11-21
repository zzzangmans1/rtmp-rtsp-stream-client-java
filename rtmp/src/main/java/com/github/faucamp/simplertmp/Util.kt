package com.github.faucamp.simplertmp

import android.util.Base64
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Double.doubleToRawLongBits
import java.lang.Double.longBitsToDouble
import java.security.MessageDigest
import kotlin.experimental.and

/**
 * Misc utility method
 *
 * @author francois, pedro
 */
object Util {

  private const val HEXES = "0123456789ABCDEF"

  @Throws(IOException::class)
  fun writeUnsignedInt32(output: OutputStream, value: Int) {
    output.write((value ushr 24))
    output.write((value ushr 16))
    output.write((value ushr 8))
    output.write(value)
  }

  @Throws(IOException::class)
  fun readUnsignedInt32(input: InputStream): Int {
    return input.read() and 0xff shl 24 or (input.read() and 0xff shl 16) or (input.read() and 0xff shl 8) or (input.read() and 0xff)
  }

  @Throws(IOException::class)
  fun readUnsignedInt24(input: InputStream): Int {
    return input.read() and 0xff shl 16 or (input.read() and 0xff shl 8) or (input.read() and 0xff)
  }

  @Throws(IOException::class)
  fun readUnsignedInt16(input: InputStream): Int {
    return input.read() and 0xff shl 8 or (input.read() and 0xff)
  }

  @Throws(IOException::class)
  fun writeUnsignedInt24(output: OutputStream, value: Int) {
    output.write((value ushr 16))
    output.write((value ushr 8))
    output.write(value)
  }

  @Throws(IOException::class)
  fun writeUnsignedInt16(output: OutputStream, value: Int) {
    output.write((value ushr 8))
    output.write(value)
  }

  fun toUnsignedInt32(bytes: ByteArray): Int {
    return bytes[0].toInt() and 0xff shl 24 or (bytes[1]
        .toInt() and 0xff shl 16) or ((bytes[2].toInt()
        and 0xff) shl 8) or (bytes[3].toInt() and 0xff)
  }

  fun toUnsignedInt32LittleEndian(bytes: ByteArray): Int {
    return ((bytes[3] and 0xff.toByte()).toInt() shl 24
        or ((bytes[2] and 0xff.toByte()).toInt() shl 16)
        or ((bytes[1] and 0xff.toByte()).toInt() shl 8)
        or ((bytes[0] and 0xff.toByte()).toInt()))
  }

  @Throws(IOException::class)
  fun writeUnsignedInt32LittleEndian(out: OutputStream, value: Int) {
    out.write(value)
    out.write((value ushr 8))
    out.write((value ushr 16))
    out.write((value ushr 24))
  }

  fun toUnsignedInt24(bytes: ByteArray): Int {
    return (bytes[1] and 0xff.toByte()).toInt() shl 16 or ((bytes[2] and 0xff.toByte()).toInt() shl 8) or (bytes[3] and 0xff.toByte()).toInt()
  }

  fun toUnsignedInt16(bytes: ByteArray): Int {
    return (bytes[2] and 0xff.toByte()).toInt() shl 8 or (bytes[3] and 0xff.toByte()).toInt()
  }

  fun toHexString(raw: ByteArray?): String? {
    if (raw == null) {
      return null
    }
    val hex = StringBuilder(2 * raw.size)
    for (b in raw) {
      hex.append(HEXES[(b and 0xF0.toByte()).toInt() shr 4]).append(HEXES[(b and 0x0F.toByte()).toInt()])
    }
    return hex.toString()
  }

  fun toHexString(b: Byte): String {
    return StringBuilder().append(HEXES[(b and 0xF0.toByte()).toInt() shr 4]).append(HEXES[(b and 0x0F).toInt()]).toString()
  }

  /**
   * Reads bytes from the specified inputstream into the specified target buffer until it is filled up
   */
  @Throws(IOException::class)
  fun readBytesUntilFull(input: InputStream, targetBuffer: ByteArray) {
    var totalBytesRead = 0
    var read: Int
    val targetBytes = targetBuffer.size
    do {
      read = input.read(targetBuffer, totalBytesRead, targetBytes - totalBytesRead)
      totalBytesRead += if (read != -1) {
        read
      } else {
        throw IOException("Unexpected EOF reached before read buffer was filled")
      }
    } while (totalBytesRead < targetBytes)
  }

  fun toByteArray(d: Double): ByteArray {
    val l = doubleToRawLongBits(d)
    return byteArrayOf(
        (l shr 56 and 0xff).toByte(), (l shr 48 and 0xff).toByte(), (l shr 40 and 0xff).toByte(),
        (l shr 32 and 0xff).toByte(), (l shr 24 and 0xff).toByte(), (l shr 16 and 0xff).toByte(),
        (l shr 8 and 0xff).toByte(), (l and 0xff).toByte())
  }

  @Throws(IOException::class)
  fun unsignedInt32ToByteArray(value: Int): ByteArray {
    return byteArrayOf((value ushr 24).toByte(), (value ushr 16).toByte(), (value ushr 8).toByte(), value.toByte()
    )
  }

  @Throws(IOException::class)
  fun readDouble(input: InputStream): Double {
    val bits = ((input.read() and 0xff).toLong() shl 56
        or ((input.read() and 0xff).toLong() shl 48)
        or ((input.read() and 0xff).toLong() shl 40)
        or ((input.read() and 0xff).toLong() shl 32)
        or (input.read() and 0xff shl 24).toLong()
        or (input.read() and 0xff shl 16).toLong()
        or (input.read() and 0xff shl 8).toLong()
        or (input.read() and 0xff).toLong())
    return longBitsToDouble(bits)
  }

  @Throws(IOException::class)
  fun writeDouble(out: OutputStream, d: Double) {
    val l = doubleToRawLongBits(d)
    out.write(byteArrayOf(
        (l shr 56 and 0xff).toByte(), (l shr 48 and 0xff).toByte(), (l shr 40 and 0xff).toByte(),
        (l shr 32 and 0xff).toByte(), (l shr 24 and 0xff).toByte(), (l shr 16 and 0xff).toByte(),
        (l shr 8 and 0xff).toByte(), (l and 0xff).toByte()
    ))
  }

  @JvmStatic
  fun getSalt(description: String): String? {
    var salt: String? = null
    val data = description.split("&".toRegex()).toTypedArray()
    for (s in data) {
      if (s.contains("salt=")) {
        salt = s.substring(5)
        break
      }
    }
    return salt
  }

  @JvmStatic
  fun getChallenge(description: String): String? {
    var challenge: String? = null
    val data = description.split("&".toRegex()).toTypedArray()
    for (s in data) {
      if (s.contains("challenge=")) {
        challenge = s.substring(10)
        break
      }
    }
    return challenge
  }

  @JvmStatic
  fun getOpaque(description: String): String {
    var opaque = ""
    val data = description.split("&".toRegex()).toTypedArray()
    for (s in data) {
      if (s.contains("opaque=")) {
        opaque = s.substring(7)
        break
      }
    }
    return opaque
  }

  @JvmStatic
  fun stringToMD5BASE64(s: String): String? {
    return try {
      val md = MessageDigest.getInstance("MD5")
      md.update(s.toByteArray(charset("UTF-8")), 0, s.length)
      val md5hash = md.digest()
      Base64.encodeToString(md5hash, Base64.NO_WRAP)
    } catch (e: Exception) {
      null
    }
  }
}