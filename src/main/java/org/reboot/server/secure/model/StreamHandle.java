package org.reboot.server.secure.model;

import java.io.InputStream;
import java.io.OutputStream;

public class StreamHandle {

  private InputStream inputStream;

  private OutputStream outputStream;

  public StreamHandle(InputStream inputStream, OutputStream outputStream) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public OutputStream getOutputStream() {
    return outputStream;
  }
}
