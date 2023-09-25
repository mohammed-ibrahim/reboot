package org.reboot.server.secure.model;

import java.net.Socket;

public class SessionHandle {

  private Socket source;

  private Socket destination;

  public SessionHandle(Socket source, Socket destination) {
    this.source = source;
    this.destination = destination;
  }

  public Socket getSource() {
    return source;
  }

  public void setSource(Socket source) {
    this.source = source;
  }

  public Socket getDestination() {
    return destination;
  }

  public void setDestination(Socket destination) {
    this.destination = destination;
  }
}
