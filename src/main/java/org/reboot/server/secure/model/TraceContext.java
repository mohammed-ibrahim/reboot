package org.reboot.server.secure.model;

import java.io.OutputStream;

public class TraceContext {

  private OutputStream outputStream;

  public TraceContext(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public OutputStream getOutputStream() {
    return outputStream;
  }
}
