package org.reboot.server.secure.model;

public class HttpHeaderContext {

  private boolean isContentLength;

  private int contentLength;

  private boolean isChunkedPacket;

  private HttpConnection httpConnection;

  private int numHeadersRead;

  private int totalNumberOfBytesReadForHeaders;

  public HttpHeaderContext() {
    this.httpConnection = HttpConnection.NOT_SPECIFIED;
  }

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

  public HttpConnection getHttpConnection() {
    return httpConnection;
  }

  public void setHttpConnection(HttpConnection httpConnection) {
    this.httpConnection = httpConnection;
  }

  public int getNumHeadersRead() {
    return numHeadersRead;
  }

  public void setNumHeadersRead(int numHeadersRead) {
    this.numHeadersRead = numHeadersRead;
  }

  public int getTotalNumberOfBytesReadForHeaders() {
    return totalNumberOfBytesReadForHeaders;
  }

  public void setTotalNumberOfBytesReadForHeaders(int totalNumberOfBytesReadForHeaders) {
    this.totalNumberOfBytesReadForHeaders = totalNumberOfBytesReadForHeaders;
  }
}
