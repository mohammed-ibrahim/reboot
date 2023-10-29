package org.reboot.server.secure.h2;

import org.reboot.server.secure.model.FrameDetails;
import org.reboot.server.secure.model.FrameType;

import java.nio.ByteBuffer;

public class FrameDecoder {

  public FrameDetails getFrameDetails(byte[] data) {
    int packetLength = getPacketLength(data);
    FrameType frameType = getFrameType(data[3]);
    String streamIdentifier = getStreamIdentifier(data);
    FrameDetails frameDetails = new FrameDetails();
    frameDetails.setFrameLength(packetLength);
    return frameDetails;
  }

  private String getStreamIdentifier(byte[] data) {
    return null;
  }

  private FrameType getFrameType(byte datum) {
    return null;
  }

  public int getPacketLength(byte [] data) {
    int value = 0;
    for (int i=0; i<3; i++) {
      value = (value << 8) + (data[i] & 0xFF);
    }
    return value;
  }
}
