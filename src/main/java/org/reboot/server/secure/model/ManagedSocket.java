package org.reboot.server.secure.model;

import java.net.Socket;

public class ManagedSocket {

  private String connectionId;

  private Socket socket;

  private SocketState socketState;

  public ManagedSocket(String connectionId, Socket socket, SocketState socketState) {
    this.connectionId = connectionId;
    this.socket = socket;
    this.socketState = socketState;
  }

  public Socket getSocket() {
    return socket;
  }

  public SocketState getSocketState() {
    return socketState;
  }

  public void setSocketState(SocketState socketState) {
    this.socketState = socketState;
  }

  public String getConnectionId() {
    return connectionId;
  }
}
