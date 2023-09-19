package org.reboot.server.secure.reader;

import java.util.List;
import java.util.Optional;

public class PacketHead {

  private List<String> headLines;

  private String encoding;

  private Integer contentLength;

  private boolean isChunked;

  public List<String> getHeadLines() {
    return headLines;
  }

  public void setHeadLines(List<String> headLines) {
    this.headLines = headLines;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public Integer getContentLength() {
    return contentLength;
  }

  public void setContentLength(Integer contentLength) {
    this.contentLength = contentLength;
  }

  public boolean isChunked() {
    return isChunked;
  }

  public void setChunked(boolean chunked) {
    isChunked = chunked;
  }
}
