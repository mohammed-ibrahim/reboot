package org.reboot.server.secure.model;

public class FrameDetails {

  private FrameType frameType;

  private int frameLength;

  public FrameType getFrameType() {
    return frameType;
  }

  public void setFrameType(FrameType frameType) {
    this.frameType = frameType;
  }

  public int getFrameLength() {
    return frameLength;
  }

  public void setFrameLength(int frameLength) {
    this.frameLength = frameLength;
  }
}
