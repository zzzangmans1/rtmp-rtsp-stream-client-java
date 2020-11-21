package com.github.faucamp.simplertmp.packets

import com.github.faucamp.simplertmp.io.ChunkStreamInfo

/**
 * Audio data packet
 *
 * @author francois
 */
class Audio : ContentData {

  constructor(header: RtmpHeader) : super(header)

  constructor() : super(RtmpHeader(RtmpHeader.ChunkType.TYPE_0_FULL, ChunkStreamInfo.RTMP_CID_AUDIO.toInt(),
      RtmpHeader.MessageType.AUDIO))

  override fun toString(): String {
    return "RTMP Audio"
  }
}