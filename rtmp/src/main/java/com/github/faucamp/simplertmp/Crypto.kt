package com.github.faucamp.simplertmp

import android.util.Log
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Some helper utilities for SHA256, mostly (used during handshake)
 * This is separated in order to be more easily replaced on platforms that
 * do not have the javax.crypto.* and/or java.security.* packages
 *
 * This implementation is directly inspired by the RTMPHandshake class of the
 * Red5  Open Source Flash Server project
 *
 * @author francois
 */
class Crypto {

  companion object {
    private const val TAG = "Crypto"
  }

  private var hmacSHA256: Mac? = null

  init {
    try {
      hmacSHA256 = Mac.getInstance("HmacSHA256")
    } catch (e: SecurityException) {
      Log.e(TAG, "Security exception when getting HMAC", e)
    } catch (e: NoSuchAlgorithmException) {
      Log.e(TAG, "HMAC SHA256 does not exist")
    }
  }

  /**
   * Calculates an HMAC SHA256 hash using a default key length.
   *
   * @return hmac hashed bytes
   */
  fun calculateHmacSHA256(input: ByteArray?, key: ByteArray?): ByteArray? {
    var output: ByteArray? = null
    try {
      hmacSHA256?.init(SecretKeySpec(key, "HmacSHA256"))
      output = hmacSHA256?.doFinal(input)
    } catch (e: InvalidKeyException) {
      Log.e(TAG, "Invalid key", e)
    }
    return output
  }

  /**
   * Calculates an HMAC SHA256 hash using a set key length.
   *
   * @return hmac hashed bytes
   */
  fun calculateHmacSHA256(input: ByteArray?, key: ByteArray?, length: Int): ByteArray? {
    var output: ByteArray? = null
    try {
      hmacSHA256?.init(SecretKeySpec(key, 0, length, "HmacSHA256"))
      output = hmacSHA256?.doFinal(input)
    } catch (e: InvalidKeyException) {
      Log.e(TAG, "Invalid key", e)
    }
    return output
  }
}