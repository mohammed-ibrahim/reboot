package org.reboot.server.secure.model;

import java.net.Socket;

public class InboundSocket {

  private Socket socket;

  public InboundSocket(Socket socket) {
    this.socket = socket;
  }

  public Socket getSocket() {
    return socket;
  }
}
