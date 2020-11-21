package com.github.faucamp.simplertmp.packets

import com.github.faucamp.simplertmp.io.ChunkStreamInfo

/**
 * Video data packet
 *
 * @author francois
 */
class Video : ContentData {

  constructor(header: RtmpHeader) : super(header)

  constructor() : super(
      RtmpHeader(RtmpHeader.ChunkType.TYPE_0_FULL, ChunkStreamInfo.RTMP_CID_VIDEO.toInt(),
          RtmpHeader.MessageType.VIDEO))

  override fun toString(): String {
    return "RTMP Video"
  }
}