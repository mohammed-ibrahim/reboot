package org.reboot.server.secure.h2;

import org.reboot.server.secure.model.FrameDetails;
import org.reboot.server.secure.model.FrameType;

import java.nio.ByteBuffer;

public class FrameDecoder {

  public FrameDetails getFrameDetails(byte[] data) {
    int packetLength = getPacketLength(data);
    FrameType frameType = getFrameType(data[3]);
    int streamIdentifier = getStreamIdentifier(data);
    FrameDetails frameDetails = new FrameDetails();
    frameDetails.setFrameLength(packetLength);
    return frameDetails;
  }

  public int getStreamIdentifier(byte[] data) {

    int part1 = ((maskMsbToZero(data[5])& 0XFF) << 24);
    int part2 = ((data[6]& 0XFF) << 16);
    int part3 = ((data[7] & 0XFF) << 8);
    int part4 = data[8] & 0XFF;

    return part1 + part2 + part3 + part4;
  }

  public byte maskMsbToZero(byte b) {
    return (byte) (b & 0X7F);
  }

  private FrameType getFrameType(byte frameTypeByte) {

    int frameTypeInt = (int)frameTypeByte;

    switch (frameTypeInt) {
      case 0:
        return FrameType.DATA;

      case 1:
        return FrameType.HEADERS;

      case 2:
        return FrameType.PRIORITY;

      case 3:
        return FrameType.RST_STREAM;

      case 4:
        return FrameType.SETTINGS;

      case 5:
        return FrameType.PUSH_PROMISE;

      case 6:
        return FrameType.PING;

      case 7:
        return FrameType.GOAWAY;

      case 8:
        return FrameType.WINDOW_UPDATE;

      case 9:
        return FrameType.CONTINUATION;

      case 10:
        return FrameType.ALTSVC;

      case 12:
        return FrameType.ORIGIN;

    }

    return FrameType.UNKNOWN;
  }

  public int getPacketLength(byte [] data) {
    int value = 0;
    for (int i=0; i<3; i++) {
      value = (value << 8) + (data[i] & 0xFF);
    }
    return value;
  }
}
