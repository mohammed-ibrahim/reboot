package org.reboot.server.secure.model;

import java.net.Socket;

public class ManagedSocket {

  private Socket socket;

  private SocketState socketState;

  public ManagedSocket(Socket socket, SocketState socketState) {
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
}
