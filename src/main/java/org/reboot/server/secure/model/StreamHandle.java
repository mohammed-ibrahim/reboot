package org.reboot.server.secure.model;

import java.io.InputStream;
import java.io.OutputStream;

public class StreamHandle {

  private InputStream inputStream;

  private OutputStream outputStream;

  private TraceContext traceContext;

  public StreamHandle(InputStream inputStream, OutputStream outputStream, TraceContext traceContext) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.traceContext = traceContext;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public OutputStream getOutputStream() {
    return outputStream;
  }

  public TraceContext getTraceContext() {
    return traceContext;
  }
}
