package org.reboot.server.secure.model;

public class StreamResponse {


  private ConnectionState connectionState;

  public StreamResponse(ConnectionState connectionState) {
    this.connectionState = connectionState;
  }

  public ConnectionState getConnectionState() {
    return connectionState;
  }
}
