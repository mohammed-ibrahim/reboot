package org.reboot.server.secure.model;

public class StreamResponse {


  private ConnectionState connectionState;

  private int numHeaders;

  private int numHeaderBytes;

  private int numBodyBytes;

  public StreamResponse(ConnectionState connectionState, int numHeaders, int numHeaderBytes, int numBodyBytes) {
    this.connectionState = connectionState;
    this.numHeaders = numHeaders;
    this.numHeaderBytes = numHeaderBytes;
    this.numBodyBytes = numBodyBytes;
  }

  public ConnectionState getConnectionState() {
    return connectionState;
  }

  public int getNumHeaders() {
    return numHeaders;
  }

  public int getNumHeaderBytes() {
    return numHeaderBytes;
  }

  public int getNumBodyBytes() {
    return numBodyBytes;
  }
}
