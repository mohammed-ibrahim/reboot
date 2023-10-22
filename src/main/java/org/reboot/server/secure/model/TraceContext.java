package org.reboot.server.secure.model;

import java.io.OutputStream;

public class TraceContext {

  private OutputStream outputStream;

  public TraceContext(OutputStream requestStream) {
    this.outputStream = requestStream;
  }

  public OutputStream getOutputStream() {
    return outputStream;
  }
}
