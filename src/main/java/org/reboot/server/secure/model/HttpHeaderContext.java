package org.reboot.server.secure.model;

public class HttpHeaderContext {

  private boolean isContentLength;

  private int contentLength;

  private boolean isChunkedPacket;

  public boolean hasBody() {
    return this.isContentLength || this.isChunkedPacket;
  }

  public boolean isContentLength() {
    return isContentLength;
  }

  public void setContentLength(boolean contentLength) {
    isContentLength = contentLength;
  }

  public int getContentLength() {
    return contentLength;
  }

  public void setContentLength(int contentLength) {
    this.contentLength = contentLength;
  }

  public boolean isChunkedPacket() {
    return isChunkedPacket;
  }

  public void setChunkedPacket(boolean chunkedPacket) {
    isChunkedPacket = chunkedPacket;
  }
}
