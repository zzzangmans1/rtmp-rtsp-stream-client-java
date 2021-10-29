package com.pedro.encoder;

/**
 * Created by pedro on 29/10/21.
 */
public interface EncoderErrorCallback {
  void onEncoderError(BaseEncoder baseEncoder, Exception e);
}
